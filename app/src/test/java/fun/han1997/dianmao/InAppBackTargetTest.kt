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
            taskForDetail = true,
            showCalendarView = true
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
            taskForDetail = true,
            showCalendarView = true
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
            taskForDetail = true,
            showCalendarView = true
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
            taskForDetail = true,
            showCalendarView = true
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
            taskForDetail = true,
            showCalendarView = true
        )

        assertEquals(InAppBackTarget.TaskDetail, target)
    }

    @Test
    fun calendarViewTakesPriorityOverQuadrantDetail() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            showReviewPanel = false,
            isTaskPanelVisible = false,
            hasActivePriority = true,
            taskForDetail = false,
            showCalendarView = true
        )

        assertEquals(InAppBackTarget.CalendarView, target)
    }

    @Test
    fun taskPanelAndDetailTakePriorityOverCalendarView() {
        val withPanel = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            showReviewPanel = false,
            isTaskPanelVisible = true,
            hasActivePriority = false,
            taskForDetail = false,
            showCalendarView = true
        )
        assertEquals(InAppBackTarget.TaskPanel, withPanel)

        val withDetail = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            showReviewPanel = false,
            isTaskPanelVisible = false,
            hasActivePriority = false,
            taskForDetail = true,
            showCalendarView = true
        )
        assertEquals(InAppBackTarget.TaskDetail, withDetail)
    }

    @Test
    fun quadrantDetailHandlesBackWhenNoOverlayIsVisible() {
        val target = resolveInAppBackTarget(
            showBackupPanel = false,
            showArchivedPanel = false,
            showReviewPanel = false,
            isTaskPanelVisible = false,
            hasActivePriority = true,
            taskForDetail = false,
            showCalendarView = false
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
            taskForDetail = false,
            showCalendarView = false
        )

        assertNull(target)
    }
}
