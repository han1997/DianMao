package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.data.canApplySilentCompletion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskStabilityModelsTest {
    @Test
    fun staleCompletionSnapshotCannotBeAppliedAfterAnotherCompletion() {
        val expected = task(id = 1, dueAtMillis = 10_000L, completionCount = 0)
        val current = expected.copy(completionCount = 1)

        assertFalse(canApplySilentCompletion(expected, current))
    }

    @Test
    fun editedReminderOrRepeatSettingsInvalidateTheSnapshot() {
        val expected = task(id = 1, dueAtMillis = 10_000L)

        assertFalse(canApplySilentCompletion(expected, expected.copy(useLocalReminder = true)))
        assertFalse(canApplySilentCompletion(expected, expected.copy(repeatRule = RepeatRule.Daily)))
    }

    @Test
    fun completedOrArchivedTaskCannotBeCompletedAgain() {
        val expected = task(id = 1, dueAtMillis = 10_000L)

        assertFalse(canApplySilentCompletion(expected, expected.copy(completed = true)))
        assertFalse(canApplySilentCompletion(expected, expected.copy(archived = true)))
    }

    @Test
    fun unchangedOpenTaskCanBeCompleted() {
        val expected = task(id = 1, dueAtMillis = 10_000L)

        assertTrue(canApplySilentCompletion(expected, expected))
    }

    @Test
    fun selectionDropsTasksThatAreNoLongerActive() {
        assertEquals(
            setOf(2L),
            reconcileSelectedTaskIds(setOf(1L, 2L), setOf(2L, 3L))
        )
    }

    private fun task(
        id: Long,
        dueAtMillis: Long?,
        completionCount: Int = 0
    ): TaskUiModel {
        return TaskUiModel(
            id = id,
            title = "Task $id",
            quadrant = TaskPriority.ImportantUrgent,
            dueLabel = dueAtMillis?.toString(),
            dueAtMillis = dueAtMillis,
            useCalendar = false,
            calendarEventId = null,
            useAlarm = dueAtMillis != null,
            repeatRule = RepeatRule.None,
            subTasks = emptyList(),
            completionCount = completionCount,
            lastCompletedAtMillis = null,
            completed = false,
            completionNote = null,
            archived = false
        )
    }
}
