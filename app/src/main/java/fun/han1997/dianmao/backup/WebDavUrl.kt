package `fun`.han1997.dianmao.backup

/**
 * Fixed backup file name used inside a WebDAV directory URL.
 */
const val WEBDAV_BACKUP_FILE_NAME = "dianmao-backup-latest.json"

/**
 * True when the configured URL points at a concrete backup file (legacy
 * shape) rather than a directory collection.
 */
internal fun isFileShapedUrl(url: String): Boolean {
    val pathOnly = url.trim().substringBefore('?')
    return pathOnly.lowercase().endsWith(".json")
}

/**
 * Resolves the user-configured WebDAV URL into the concrete backup file URL.
 *
 * Two supported shapes:
 * - A full file URL ending in ".json" (ignoring the query string) is used
 *   as-is, preserving the legacy behavior where the URL names the file.
 * - Any other URL is treated as a directory; a trailing slash is ensured and
 *   [WEBDAV_BACKUP_FILE_NAME] is appended.
 */
internal fun resolveBackupFileUrl(url: String, fileName: String = WEBDAV_BACKUP_FILE_NAME): String {
    val trimmed = url.trim()
    if (trimmed.isBlank()) return trimmed
    if (isFileShapedUrl(trimmed)) return trimmed
    val withoutQuery = trimmed.substringBefore('?')
    return withoutQuery.trimEnd('/') + "/" + fileName
}

/**
 * Returns the WebDAV collection URLs that must exist for a directory-shaped
 * URL to accept an upload, ordered from outermost to innermost. Each entry is
 * a full URL ending in "/". File-shaped or unparseable URLs yield an empty
 * list (nothing to create).
 */
internal fun webDavCollectionUrls(url: String): List<String> {
    val trimmed = url.trim()
    if (trimmed.isBlank() || isFileShapedUrl(trimmed)) return emptyList()
    val schemeEnd = trimmed.indexOf("://")
    if (schemeEnd <= 0) return emptyList()
    val scheme = trimmed.substring(0, schemeEnd)
    val rest = trimmed.substring(schemeEnd + 3)
    val pathStart = rest.indexOf('/')
    val authority = if (pathStart < 0) rest else rest.substring(0, pathStart)
    if (authority.isBlank()) return emptyList()
    val path = if (pathStart < 0) "" else rest.substring(pathStart).substringBefore('?')
    val segments = path.split('/').filter { it.isNotBlank() }
    if (segments.isEmpty()) return emptyList()
    var prefix = ""
    return segments.map { segment ->
        prefix += "/$segment"
        "$scheme://$authority$prefix/"
    }
}
