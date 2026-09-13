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
            showReviewPanel = true,
            isTaskPanelVisible = true,
            hasActivePriority = true,
            taskForDetail = true
        )

        assertEquals(InAppBackTarget.BackupPanel, target)
    }

    @Test
    fun archivedPanelTakesPriorityOverReviewPanelAndBelow() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = true,
            showReviewPanel = true,
            isTaskPanelVisible = true,
            hasActivePriority = true,
            taskForDetail = true
        )

        assertEquals(InAppBackTarget.ArchivedPanel, target)
    }

    @Test
    fun reviewPanelTakesPriorityOverTaskPanelAndBelow() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            showReviewPanel = true,
            isTaskPanelVisible = true,
            hasActivePriority = true,
            taskForDetail = true
        )

        assertEquals(InAppBackTarget.ReviewPanel, target)
    }

    @Test
    fun taskPanelTakesPriorityOverTaskDetailAndQuadrantDetail() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            showReviewPanel = false,
            isTaskPanelVisible = true,
            hasActivePriority = true,
            taskForDetail = true
        )

        assertEquals(InAppBackTarget.TaskPanel, target)
    }

    @Test
    fun taskDetailTakesPriorityOverQuadrantDetail() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            showReviewPanel = false,
            isTaskPanelVisible = false,
            hasActivePriority = true,
            taskForDetail = true
        )

        assertEquals(InAppBackTarget.TaskDetail, target)
    }

    @Test
    fun quadrantDetailHandlesBackWhenNoOverlayIsVisible() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            showReviewPanel = false,
            isTaskPanelVisible = false,
            hasActivePriority = true,
            taskForDetail = false
        )

        assertEquals(InAppBackTarget.QuadrantDetail, target)
    }

    @Test
    fun returnsNullWhenAppShouldFallBackToSystemBack() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            showReviewPanel = false,
            isTaskPanelVisible = false,
            hasActivePriority = false,
            taskForDetail = false
        )

        assertNull(target)
    }
}
