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
            val connection = openConnection(settings, "PUT").apply {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            try {
                connection.outputStream.use { output ->
                    output.write(payload.toByteArray(StandardCharsets.UTF_8))
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
            val connection = openConnection(settings, "GET")
            try {
                connection.requireSuccess()
                connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun openConnection(settings: WebDavSettings, method: String): HttpURLConnection {
        val connection = (URL(settings.url.trim()).openConnection() as HttpURLConnection).apply {
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
