package `fun`.han1997.dianmao.data

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRepositoryCompletionTest {
    @Test
    fun silentDailyCompletionRecordsHistoryAndLaterNoteDoesNotCountAgain() = runBlocking {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val taskId = db.taskDao().insert(
                TaskEntity(
                    title = "Daily task",
                    quadrant = QuadrantType.ImportantUrgent,
                    dueLabel = null,
                    dueAtMillis = null,
                    useCalendar = false,
                    useAlarm = false,
                    repeatRule = RepeatRule.Daily,
                    completed = false,
                    completionNote = null,
                    createdAtMillis = 1L,
                    updatedAtMillis = 1L
                )
            )
            val repository = TaskRepository(db)
            val task = requireNotNull(repository.findTaskById(taskId))
            assertTrue(repository.completeSilently(task))

            val completed = db.taskDao().findById(taskId)!!
            assertEquals(1, completed.completionCount)
            assertEquals(1, db.taskCompletionDao().countForTask(taskId))
            assertEquals(null, db.taskCompletionDao().findCompletions(taskId).single().note)

            repository.saveCompletionNote(taskId, "Done", task.completionCount)

            val noted = db.taskDao().findById(taskId)!!
            assertEquals(1, noted.completionCount)
            assertEquals(1, db.taskCompletionDao().countForTask(taskId))
            assertEquals("Done", noted.completionNote)
        } finally {
            db.close()
        }
    }

    @Test
    fun silentAlarmCycleCompletionNoteUpdatesRecordedHistory() = runBlocking {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val dueAt = System.currentTimeMillis() + 60_000L
            val taskId = db.taskDao().insert(
                TaskEntity(
                    title = "Alarm-backed daily task",
                    quadrant = QuadrantType.ImportantUrgent,
                    dueLabel = "today",
                    dueAtMillis = dueAt,
                    useCalendar = false,
                    useAlarm = true,
                    repeatRule = RepeatRule.Daily,
                    completed = false,
                    completionNote = null,
                    createdAtMillis = 1L,
                    updatedAtMillis = 1L
                )
            )
            val repository = TaskRepository(db)
            val task = requireNotNull(repository.findTaskById(taskId))

            assertTrue(repository.completeSilently(task))
            repository.saveCompletionNote(taskId, "Done", task.completionCount)

            val updated = db.taskDao().findById(taskId)!!
            assertEquals(1, updated.completionCount)
            assertEquals(1, db.taskCompletionDao().countForTask(taskId))
            assertEquals("Done", db.taskCompletionDao().findCompletions(taskId).single().note)
        } finally {
            db.close()
        }
    }
}
