package `fun`.han1997.dianmao

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InAppBackTargetTest {
    @Test
    fun backupPanelTakesPriorityOverTaskPanelAndDetail() {
        val target = resolveInAppBackTarget(
            showBackupPanel = true,
            showArchivedPanel = true,
            isTaskPanelVisible = true,
            hasActivePriority = true
        )

        assertEquals(InAppBackTarget.BackupPanel, target)
    }

    @Test
    fun archivedPanelTakesPriorityOverTaskPanelAndDetail() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = true,
            isTaskPanelVisible = true,
            hasActivePriority = true
        )

        assertEquals(InAppBackTarget.ArchivedPanel, target)
    }

    @Test
    fun taskPanelTakesPriorityOverQuadrantDetail() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            isTaskPanelVisible = true,
            hasActivePriority = true
        )

        assertEquals(InAppBackTarget.TaskPanel, target)
    }

    @Test
    fun quadrantDetailHandlesBackWhenNoOverlayIsVisible() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            isTaskPanelVisible = false,
            hasActivePriority = true
        )

        assertEquals(InAppBackTarget.QuadrantDetail, target)
    }

    @Test
    fun returnsNullWhenAppShouldFallBackToSystemBack() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            isTaskPanelVisible = false,
            hasActivePriority = false
        )

        assertNull(target)
    }
}
