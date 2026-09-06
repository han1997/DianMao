package `fun`.han1997.dianmao.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY displayOrder ASC, createdAtMillis DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE archived = 0 ORDER BY displayOrder ASC, createdAtMillis DESC")
    fun observeActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE archived = 1 ORDER BY updatedAtMillis DESC")
    fun observeArchivedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countTasks(): Long

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY displayOrder ASC, createdAtMillis DESC")
    suspend fun findAllTasks(): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Query("UPDATE tasks SET displayOrder = :newOrder WHERE id = :taskId")
    suspend fun updateDisplayOrder(taskId: Long, newOrder: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET completionCount = :count WHERE id = :taskId")
    suspend fun updateCompletionCount(taskId: Long, count: Int)
}

@Dao
interface TaskCompletionDao {
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completedAtMillis DESC")
    fun observeCompletions(taskId: Long): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions WHERE id = :completionId LIMIT 1")
    suspend fun findById(completionId: Long): TaskCompletionEntity?

    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completedAtMillis DESC")
    suspend fun findCompletions(taskId: Long): List<TaskCompletionEntity>

    @Insert
    suspend fun insert(completion: TaskCompletionEntity): Long

    @Query("UPDATE task_completions SET note = :note WHERE id = :completionId")
    suspend fun updateNote(completionId: Long, note: String?)

    @Query("DELETE FROM task_completions WHERE id = :completionId")
    suspend fun deleteById(completionId: Long)

    @Query("DELETE FROM task_completions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM task_completions WHERE taskId = :taskId")
    suspend fun countForTask(taskId: Long): Int

    @Query("SELECT * FROM task_completions ORDER BY taskId ASC, completedAtMillis DESC")
    suspend fun findAll(): List<TaskCompletionEntity>

    @Insert
    suspend fun insertAll(completions: List<TaskCompletionEntity>)
}

