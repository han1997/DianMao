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
            ensureDirectoryExists(settings)
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
            val fileUrl = resolveBackupFileUrl(settings.url)
            val connection = openConnection(settings, "GET", fileUrl)
            try {
                connection.requireSuccess()
                connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
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
            requestMethod = method
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
