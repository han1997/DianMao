package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.backup.encodeWebDavPathSegment
import `fun`.han1997.dianmao.backup.encodeWebDavUrl
import `fun`.han1997.dianmao.backup.isFileShapedUrl
import `fun`.han1997.dianmao.backup.looksLikeDianmaoBackup
import `fun`.han1997.dianmao.backup.propfindBodyIsCollection
import `fun`.han1997.dianmao.backup.resolveBackupFileUrl
import `fun`.han1997.dianmao.backup.webDavCollectionUrls
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebDavUrlTest {
    @Test
    fun encodesChineseAndSpaceSegments() {
        assertEquals(
            "%E5%A4%87%E4%BB%BD%20%E7%9B%AE%E5%BD%95",
            encodeWebDavPathSegment("备份 目录")
        )
    }

    @Test
    fun leavesSafeCharactersUntouched() {
        assertEquals(
            "dav-folder.v2_2026~",
            encodeWebDavPathSegment("dav-folder.v2_2026~")
        )
    }

    @Test
    fun preservesExistingPercentEscapes() {
        val encoded = "%E5%A4%87%E4%BB%BD"
        assertEquals(encoded, encodeWebDavPathSegment(encoded))
    }

    @Test
    fun encodesUtf8BytesAsUppercasePairs() {
        // 备 = E5 A4 87 in UTF-8
        assertEquals("%E5%A4%87", encodeWebDavPathSegment("备"))
    }

    @Test
    fun encodeWebDavUrlKeepsSchemeAndAuthority() {
        assertEquals(
            "https://dav.example.com:5006/%E5%A4%87%E4%BB%BD/%E4%BB%BB%E5%8A%A1",
            encodeWebDavUrl("https://dav.example.com:5006/备份/任务")
        )
    }

    @Test
    fun encodeWebDavUrlPassesAlreadyEncodedUrlThrough() {
        val url = "https://dav.example.com:5006/%E5%A4%87%E4%BB%BD/"
        assertEquals(url, encodeWebDavUrl(url))
    }

    @Test
    fun encodeWebDavUrlHandlesMalformedInput() {
        assertEquals("not-a-url", encodeWebDavUrl("not-a-url"))
        assertEquals("https://host", encodeWebDavUrl("https://host"))
    }

    @Test
    fun resolveAppendsEncodedFileNameToDirectoryUrl() {
        assertEquals(
            "https://dav.example.com/dianmao/%E5%A4%87%E4%BB%BD/dianmao-backup-latest.json",
            resolveBackupFileUrl("https://dav.example.com/dianmao/备份/")
        )
    }

    @Test
    fun resolveKeepsLegacyJsonFileUrl() {
        val url = "https://dav.example.com/dianmao/backup.json"
        assertEquals(url, resolveBackupFileUrl(url))
    }

    @Test
    fun resolveEncodesLegacyJsonFileUrlPath() {
        assertEquals(
            "https://dav.example.com/dianmao/%E5%A4%87%E4%BB%BD.json",
            resolveBackupFileUrl("https://dav.example.com/dianmao/备份.json")
        )
    }

    @Test
    fun collectionUrlsAreEncodedLevelByLevel() {
        assertEquals(
            listOf(
                "https://dav.example.com/dianmao/",
                "https://dav.example.com/dianmao/%E5%A4%87%E4%BB%BD%20%E7%9B%AE%E5%BD%95/"
            ),
            webDavCollectionUrls("https://dav.example.com/dianmao/备份 目录")
        )
    }

    @Test
    fun fileShapedUrlsYieldNoCollections() {
        assertTrue(webDavCollectionUrls("https://dav.example.com/backup.json").isEmpty())
        assertTrue(webDavCollectionUrls("https://dav.example.com").isEmpty())
    }

    @Test
    fun fileShapedDetectionStillWorks() {
        assertTrue(isFileShapedUrl("https://host/path/backup.JSON"))
        assertFalse(isFileShapedUrl("https://host/path/目录"))
    }

    @Test
    fun propfindDetectsCollectionWithDavPrefix() {
        val body = "<?xml version=\"1.0\"?><D:multistatus xmlns:D=\"DAV:\"><D:response>" +
            "<D:href>/webdav/DianMao/</D:href><D:propstat><D:prop>" +
            "<D:resourcetype><D:collection/></D:resourcetype>" +
            "</D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>"
        assertTrue(propfindBodyIsCollection(body))
    }

    @Test
    fun propfindDetectsFileWithEmptyResourcetype() {
        val body = "<?xml version=\"1.0\"?><D:multistatus xmlns:D=\"DAV:\"><D:response>" +
            "<D:href>/webdav/DianMao</D:href><D:propstat><D:prop>" +
            "<D:resourcetype></D:resourcetype><D:getcontentlength>15024</D:getcontentlength>" +
            "</D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>"
        assertFalse(propfindBodyIsCollection(body))
    }

    @Test
    fun propfindDetectsCollectionWithoutPrefix() {
        val body = "<multistatus xmlns=\"DAV:\"><response><href>/dav/dir/</href>" +
            "<propstat><prop><resourcetype><collection/></resourcetype>" +
            "</prop><status>HTTP/1.1 200 OK</status></propstat></response></multistatus>"
        assertTrue(propfindBodyIsCollection(body))
    }

    @Test
    fun propfindGarbageYieldsNotCollection() {
        assertFalse(propfindBodyIsCollection(""))
        assertFalse(propfindBodyIsCollection("Not Authorized"))
        assertFalse(propfindBodyIsCollection("<html><body>403</body></html>"))
    }

    @Test
    fun dianmaoBackupMarkerIsDetected() {
        val backup = "{\n  \"format\": \"dianmao.tasks\",\n  \"backupVersion\": 3\n}"
        assertTrue(looksLikeDianmaoBackup(backup))
    }

    @Test
    fun nonBackupBodiesAreRejected() {
        assertFalse(looksLikeDianmaoBackup("{\"format\":\"other.app\"}"))
        assertFalse(looksLikeDianmaoBackup(""))
        assertFalse(looksLikeDianmaoBackup("<html>404</html>"))
        assertFalse(looksLikeDianmaoBackup("dianmao.tasks without quotes"))
    }
}
