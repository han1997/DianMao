package `fun`.han1997.dianmao.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64

class WebDavBackupClient {
    suspend fun upload(settings: WebDavSettings, payload: String) {
        withContext(Dispatchers.IO) {
            require(settings.url.isNotBlank()) { "WebDAV URL is empty" }
            // The configured URL may name an existing FILE (e.g. a backup
            // written by a legacy app version) instead of a directory. PUT
            // into a file's "interior" fails on QNAP/Synology with 400, so
            // probe the remote type first. A legacy file is migrated once:
            // verified backup content -> DELETE the file, MKCOL the
            // directory, then upload into it. Anything else keeps directory
            // semantics.
            if (!isFileShapedUrl(settings.url)) {
                when (probeRemoteType(settings)) {
                    RemoteType.LEGACY_FILE -> migrateFileToDirectory(settings)
                    RemoteType.MISSING, RemoteType.COLLECTION -> ensureDirectoryExists(settings)
                }
            }
            val fileUrl = resolveBackupFileUrl(settings.url)
            val body = payload.toByteArray(StandardCharsets.UTF_8)
            val connection = openConnection(settings, "PUT", fileUrl).apply {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                // Fixed-length body: chunked transfer-encoding is rejected by
                // some NAS WebDAV firmwares (Synology/QNAP) with 400/411.
                setFixedLengthStreamingMode(body.size)
            }
            try {
                connection.outputStream.use { output ->
                    output.write(body)
                }
                connection.requireSuccess()
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun download(settings: WebDavSettings): String {
        return withContext(Dispatchers.IO) {
            require(settings.url.isNotBlank()) { "WebDAV URL is empty" }
            // Read-only fallback: when the configured URL names a legacy
            // backup FILE, GET that URL itself instead of the in-directory
            // file name. No migration happens on download.
            val targetIsFile = !isFileShapedUrl(settings.url) &&
                probeRemoteType(settings) == RemoteType.LEGACY_FILE
            val fileUrl = if (targetIsFile) settings.url.trim() else resolveBackupFileUrl(settings.url)
            val connection = openConnection(settings, "GET", fileUrl)
            try {
                connection.requireSuccess()
                connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
    }

    private enum class RemoteType { MISSING, COLLECTION, LEGACY_FILE }

    /**
     * Single PROPFIND Depth:0 probe classifying the configured URL.
     * 404 -> MISSING (directory semantics will create it); 2xx with a
     * collection resourcetype -> COLLECTION; 2xx otherwise -> LEGACY_FILE.
     * Any other status aborts with an IOException instead of guessing.
     */
    private fun probeRemoteType(settings: WebDavSettings): RemoteType {
        val connection = openConnection(settings, "PROPFIND", settings.url.trim()).apply {
            setRequestProperty("Depth", "0")
        }
        try {
            val code = connection.responseCode
            return when {
                code == 404 -> RemoteType.MISSING
                code !in 200..299 -> {
                    val message = runCatching {
                        connection.errorStream?.bufferedReader(StandardCharsets.UTF_8)
                            ?.use { it.readText() }
                    }.getOrNull().orEmpty()
                    throw IOException("WebDAV PROPFIND $code ${connection.responseMessage.orEmpty()} ${message.take(120)}".trim())
                }
                else -> {
                    val body = runCatching {
                        connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    }.getOrNull().orEmpty()
                    if (propfindBodyIsCollection(body)) RemoteType.COLLECTION else RemoteType.LEGACY_FILE
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * One-time migration of a legacy backup file into directory semantics:
     * download and verify the content is actually a DianMao backup, DELETE
     * the file, MKCOL the directory chain, and let the caller PUT the fresh
     * backup inside. A non-backup file occupying the URL is never deleted —
     * the upload aborts with an IOException instead.
     */
    private fun migrateFileToDirectory(settings: WebDavSettings) {
        val legacyContent = downloadLegacyFile(settings)
        if (!looksLikeDianmaoBackup(legacyContent)) {
            throw IOException(
                "WebDAV target is a file but not a DianMao backup; refusing to replace it"
            )
        }
        deleteResource(settings, settings.url.trim())
        ensureDirectoryExists(settings)
    }

    private fun downloadLegacyFile(settings: WebDavSettings): String {
        val connection = openConnection(settings, "GET", settings.url.trim())
        try {
            connection.requireSuccess()
            return connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun deleteResource(settings: WebDavSettings, targetUrl: String) {
        val connection = openConnection(settings, "DELETE", targetUrl)
        try {
            connection.requireSuccess()
        } finally {
            connection.disconnect()
        }
    }

    /**
     * PROPFIND Depth:0 probe of the configured URL. Returns true when the
     * resource exists (200/207). False means it does not exist (404) — the
     * caller then assumes directory semantics and lets MKCOL/PUT create it.
     * Any other status (401/403/5xx) surfaces as an IOException instead of
     * guessing semantics and writing to the wrong place.
     */
    private fun remoteResourceExists(settings: WebDavSettings): Boolean {
        val connection = openConnection(settings, "PROPFIND", settings.url.trim()).apply {
            setRequestProperty("Depth", "0")
        }
        try {
            return when (val code = connection.responseCode) {
                in 200..299 -> true
                404 -> false
                else -> {
                    val message = runCatching {
                        connection.errorStream?.bufferedReader(StandardCharsets.UTF_8)
                            ?.use { it.readText() }
                    }.getOrNull().orEmpty()
                    throw IOException("WebDAV PROPFIND $code ${connection.responseMessage.orEmpty()} ${message.take(120)}".trim())
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * HttpURLConnection validates the request method against a fixed
     * whitelist (OPTIONS/GET/HEAD/POST/PUT/DELETE/TRACE/PATCH) and throws
     * `ProtocolException` for WebDAV verbs like PROPFIND/MKCOL. Setting the
     * protected `method` field reflectively is the standard workaround.
     */
    private fun HttpURLConnection.setWebDavMethod(method: String) {
        val field = HttpURLConnection::class.java.getDeclaredField("method")
        field.isAccessible = true
        field.set(this, method)
    }

    /**
     * WebDAV directory support: when the configured URL is a directory (not a
     * ".json" file URL), try to create the collection chain with MKCOL before
     * uploading. MKCOL failures are deliberately non-fatal — many servers
     * pre-create the directory or forbid MKCOL — the subsequent PUT decides
     * the outcome through its own status code.
     */
    private fun ensureDirectoryExists(settings: WebDavSettings) {
        webDavCollectionUrls(settings.url).forEach { collectionUrl ->
            runCatching { mkcolCollection(settings, collectionUrl) }
        }
    }

    private fun mkcolCollection(settings: WebDavSettings, collectionUrl: String) {
        val connection = openConnection(settings, "MKCOL", collectionUrl)
        try {
            val code = connection.responseCode
            // 201 created, 405 already exists; any failure here is non-fatal
            // by design — the subsequent PUT reports the real outcome.
            if (code in 200..299 || code == 405) return
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(
        settings: WebDavSettings,
        method: String,
        targetUrl: String = settings.url.trim()
    ): HttpURLConnection {
        val connection = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
            // PROPFIND/MKCOL are outside HttpURLConnection's built-in method
            // whitelist; reflectively setting `method` avoids the local
            // ProtocolException "Expected one of [...] but was PROPFIND".
            runCatching { setWebDavMethod(method) }
                .onFailure { requestMethod = method }
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
        }
        if (settings.username.isNotBlank() || settings.password.isNotBlank()) {
            val token = "${settings.username}:${settings.password}"
            val encoded = Base64.getEncoder().encodeToString(token.toByteArray(StandardCharsets.UTF_8))
            connection.setRequestProperty("Authorization", "Basic $encoded")
        }
        return connection
    }

    private fun HttpURLConnection.requireSuccess() {
        val code = responseCode
        if (code !in 200..299) {
            val message = runCatching {
                errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
            }.getOrNull().orEmpty()
            throw IOException("WebDAV $code ${responseMessage.orEmpty()} ${message.take(120)}".trim())
        }
    }
}
