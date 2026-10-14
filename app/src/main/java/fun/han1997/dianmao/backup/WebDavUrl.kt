package `fun`.han1997.dianmao.backup

/**
 * Fixed backup file name used inside a WebDAV directory URL.
 */
const val WEBDAV_BACKUP_FILE_NAME = "dianmao-backup-latest.json"

/**
 * Safety-net snapshot written next to the backup before a destructive
 * WebDAV restore overwrites local data: if the restored backup turns out
 * wrong or corrupt, the user can restore this snapshot back.
 */
const val WEBDAV_PRE_RESTORE_SNAPSHOT_FILE_NAME = "dianmao-backup-before-restore.json"

/**
 * Safety-net snapshot written to the public Downloads collection before a
 * destructive *local file* restore. Same role as the WebDAV snapshot, but
 * local restores have no remote share, so it lands in Downloads where the
 * existing 本地恢复 file picker can select it back for a manual undo.
 */
const val LOCAL_PRE_RESTORE_SNAPSHOT_FILE_NAME = "dianmao-backup-before-restore.json"

/**
 * True when the configured URL points at a concrete backup file (legacy
 * shape) rather than a directory collection.
 */
internal fun isFileShapedUrl(url: String): Boolean {
    val pathOnly = url.trim().substringBefore('?')
    return pathOnly.lowercase().endsWith(".json")
}

/**
 * Percent-encodes one path segment for safe inclusion in an HTTP request
 * line. Synology/QNAP WebDAV servers reject request lines containing raw
 * non-ASCII bytes (e.g. Chinese directory names) or spaces with 400 Bad
 * Request. Characters that are legal in a path segment (unreserved marks and
 * existing percent-escapes) are preserved so already-encoded URLs are not
 * double-encoded.
 */
internal fun encodeWebDavPathSegment(segment: String): String {
    val safe = "abcdefghijklmnopqrstuvwxyz" +
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
        "0123456789" +
        "-._~!$&'()*+,;=:@%"
    val sb = StringBuilder(segment.length)
    for (byte in segment.toByteArray(Charsets.UTF_8)) {
        val b = byte.toInt() and 0xFF
        val ch = b.toChar()
        if (safe.indexOf(ch) >= 0 && b < 0x80) {
            sb.append(ch)
        } else {
            sb.append('%')
            sb.append(b.toString(16).uppercase().padStart(2, '0'))
        }
    }
    return sb.toString()
}

/**
 * Encodes the path portion of a raw user-configured WebDAV URL segment by
 * segment. Scheme and authority are left untouched; the query string is
 * dropped because backup URLs never need one. Already percent-encoded
 * sequences (e.g. "%E5%A4%87") survive untouched because '%' is a safe char
 * inside [encodeWebDavPathSegment].
 */
internal fun encodeWebDavUrl(url: String): String {
    val trimmed = url.trim()
    val schemeEnd = trimmed.indexOf("://")
    if (schemeEnd <= 0) return trimmed
    val scheme = trimmed.substring(0, schemeEnd)
    val rest = trimmed.substring(schemeEnd + 3)
    val pathStart = rest.indexOf('/')
    val authority = if (pathStart < 0) rest else rest.substring(0, pathStart)
    if (authority.isBlank()) return trimmed
    val path = if (pathStart < 0) "" else rest.substring(pathStart).substringBefore('?')
    if (path.isBlank()) return trimmed
    val encodedPath = path.split('/').joinToString("/") { segment ->
        if (segment.isEmpty()) "" else encodeWebDavPathSegment(segment)
    }
    return "$scheme://$authority$encodedPath"
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
    val resolved = if (isFileShapedUrl(trimmed)) {
        trimmed
    } else {
        val withoutQuery = trimmed.substringBefore('?')
        withoutQuery.trimEnd('/') + "/" + fileName
    }
    return encodeWebDavUrl(resolved)
}

/**
 * True when a WebDAV PROPFIND Depth:0 response body describes a collection
 * (directory) rather than a plain file. QNAP/Synology servers answer with a
 * 207 multistatus whose single `<D:response>` carries either
 * `<D:resourcetype><D:collection/></D:resourcetype>` (directory) or an empty
 * `<D:resourcetype></D:resourcetype>` (file). Matching on the
 * `resourcetype` element keeps the check independent of the `D:` prefix,
 * which some servers omit.
 */
internal fun propfindBodyIsCollection(body: String): Boolean {
    val open = Regex("<(?:[A-Za-z0-9_.-]+:)?resourcetype\\b[^>]*>", RegexOption.IGNORE_CASE)
    val match = open.find(body) ?: return false
    val closeTag = "</" + match.value.trimStart('<', '/').trimEnd('>') + ">"
    val closeIndex = body.indexOf(closeTag, match.range.last, ignoreCase = true)
    val inner = if (closeIndex >= 0) {
        body.substring(match.range.last + 1, closeIndex)
    } else {
        // Self-closing is never a collection; fall through to the inner scan.
        body.substring(match.range.last + 1, body.length.coerceAtMost(match.range.last + 200))
    }
    return inner.contains(Regex("<(?:[A-Za-z0-9_.-]+:)?collection\\b", RegexOption.IGNORE_CASE)) ||
        inner.contains("collection", ignoreCase = true)
}

/**
 * Cheap structural check that a remote body is a DianMao backup document
 * before it is deleted during the file-to-directory migration. The backup
 * codec always emits `"format": "dianmao.tasks"` near the top of the JSON;
 * whitespace and attribute order variations are tolerated by matching the
 * format token anywhere in the body.
 */
internal fun looksLikeDianmaoBackup(body: String): Boolean {
    return body.contains("\"dianmao.tasks\"")
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
        prefix += "/" + encodeWebDavPathSegment(segment)
        "$scheme://$authority$prefix/"
    }
}
