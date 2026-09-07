package `fun`.han1997.dianmao.data

import android.os.Build
import java.time.OffsetDateTime
import java.time.ZoneId
import org.json.JSONArray
import org.json.JSONObject

object TaskBackupCodec {
    private const val FORMAT = "dianmao.tasks"
    private const val BACKUP_VERSION = 4

    fun encode(
        tasks: List<TaskEntity>,
        completions: List<TaskCompletionEntity>,
        createdAtMillis: Long = System.currentTimeMillis(),
        appVersionName: String = "1.0"
    ): String {
        val root = JSONObject()
            .put("format", FORMAT)
            .put("backupVersion", BACKUP_VERSION)
            .put("databaseVersion", TASK_DATABASE_VERSION)
            .put("createdAtMillis", createdAtMillis)
            .put("createdAt", isoTimestamp(createdAtMillis))
            .put(
                "app",
                JSONObject()
                    .put("versionName", appVersionName)
                    .put("platform", "android")
                    .put("osVersion", Build.VERSION.SDK_INT)
            )
            .put(
                "summary",
                JSONObject()
                    .put("taskCount", tasks.size)
                    .put("completionCount", completions.size)
                    .put("archivedCount", tasks.count { it.archived })
            )
            .put(
                "tasks",
                JSONArray().apply {
                    tasks.forEach { put(taskToJson(it)) }
                }
            )
            .put(
                "completions",
                JSONArray().apply {
                    completions.forEach { put(completionToJson(it)) }
                }
            )
        return root.toString(2)
    }

    fun decodeForImport(json: String): Pair<List<TaskEntity>, List<TaskCompletionEntity>> {
        val root = JSONObject(json)
        require(root.getString("format") == FORMAT) { "Unsupported backup format" }
        require(root.getInt("backupVersion") <= BACKUP_VERSION) { "Unsupported backup version" }

        val tasks = root.getJSONArray("tasks")
        val taskList = List(tasks.length()) { index ->
            taskFromJson(tasks.getJSONObject(index)).copy(calendarEventId = null)
        }

        val completions = if (root.has("completions")) {
            val arr = root.getJSONArray("completions")
            List(arr.length()) { index ->
                completionFromJson(arr.getJSONObject(index))
            }
        } else {
            emptyList()
        }

        return Pair(taskList, completions)
    }

    private fun isoTimestamp(millis: Long): String {
        return OffsetDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(millis),
            ZoneId.systemDefault()
        ).toString()
    }

    private fun taskToJson(task: TaskEntity): JSONObject {
        return JSONObject()
            .put("id", task.id)
            .put("title", task.title)
            .put("quadrant", task.quadrant.name)
            .put("quadrantLabel", TaskPriority.valueOf(task.quadrant.name).label)
            .putNullable("dueLabel", task.dueLabel)
            .putNullable("dueAtMillis", task.dueAtMillis)
            .putNullable("dueAt", task.dueAtMillis?.let { isoTimestamp(it) })
            .put("useCalendar", task.useCalendar)
            .putNullable("calendarEventId", task.calendarEventId)
            .put("useAlarm", task.useAlarm)
            .put("useLocalReminder", task.useLocalReminder)
            .put("reminderLeadMinutes", task.reminderLeadMinutes)
            .put("repeatRule", task.repeatRule.name)
            .put("repeatRuleLabel", task.repeatRule.label)
            .put("subTasks", subTasksToJson(task.subTasks))
            .put("completionCount", task.completionCount)
            .putNullable("lastCompletedAtMillis", task.lastCompletedAtMillis)
            .putNullable("lastCompletedAt", task.lastCompletedAtMillis?.let { isoTimestamp(it) })
            .put("completed", task.completed)
            .putNullable("completionNote", task.completionNote)
            .put("archived", task.archived)
            .put("displayOrder", task.displayOrder)
            .put("createdAtMillis", task.createdAtMillis)
            .put("createdAt", isoTimestamp(task.createdAtMillis))
            .put("updatedAtMillis", task.updatedAtMillis)
            .put("updatedAt", isoTimestamp(task.updatedAtMillis))
    }

    private fun taskFromJson(json: JSONObject): TaskEntity {
        return TaskEntity(
            id = json.getLong("id"),
            title = json.getString("title"),
            quadrant = QuadrantType.valueOf(json.getString("quadrant")),
            dueLabel = json.nullableString("dueLabel"),
            dueAtMillis = json.nullableLong("dueAtMillis"),
            useCalendar = json.getBoolean("useCalendar"),
            calendarEventId = json.nullableLong("calendarEventId"),
            useAlarm = json.getBoolean("useAlarm"),
            useLocalReminder = json.optBoolean("useLocalReminder", false),
            reminderLeadMinutes = json.optInt("reminderLeadMinutes", 0),
            repeatRule = RepeatRule.valueOf(json.getString("repeatRule")),
            subTasks = subTasksFromJson(json),
            completionCount = json.optInt("completionCount", 0),
            lastCompletedAtMillis = json.nullableLong("lastCompletedAtMillis"),
            completed = json.getBoolean("completed"),
            completionNote = json.nullableString("completionNote"),
            archived = json.optBoolean("archived", false),
            displayOrder = json.optInt("displayOrder", 0),
            createdAtMillis = json.getLong("createdAtMillis"),
            updatedAtMillis = json.getLong("updatedAtMillis")
        )
    }

    private fun completionToJson(completion: TaskCompletionEntity): JSONObject {
        return JSONObject()
            .put("id", completion.id)
            .put("taskId", completion.taskId)
            .put("completedAtMillis", completion.completedAtMillis)
            .put("completedAt", isoTimestamp(completion.completedAtMillis))
            .putNullable("note", completion.note)
    }

    private fun subTasksToJson(subTasks: List<TaskSubTask>): JSONArray {
        return JSONArray().apply {
            subTasks.forEach { item ->
                put(
                    JSONObject()
                        .put("id", item.id)
                        .put("title", item.title)
                        .put("completed", item.completed)
                )
            }
        }
    }

    private fun subTasksFromJson(json: JSONObject): List<TaskSubTask> {
        if (!json.has("subTasks") || json.isNull("subTasks")) return emptyList()
        val array = json.getJSONArray("subTasks")
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            TaskSubTask(
                id = item.optLong("id", index + 1L),
                title = item.getString("title"),
                completed = item.optBoolean("completed", false)
            )
        }
    }

    private fun completionFromJson(json: JSONObject): TaskCompletionEntity {
        return TaskCompletionEntity(
            id = json.getLong("id"),
            taskId = json.getLong("taskId"),
            completedAtMillis = json.getLong("completedAtMillis"),
            note = json.nullableString("note")
        )
    }

    private fun JSONObject.putNullable(name: String, value: String?): JSONObject {
        return put(name, value ?: JSONObject.NULL)
    }

    private fun JSONObject.putNullable(name: String, value: Long?): JSONObject {
        return put(name, value ?: JSONObject.NULL)
    }

    private fun JSONObject.nullableString(name: String): String? {
        return if (isNull(name)) null else getString(name)
    }

    private fun JSONObject.nullableLong(name: String): Long? {
        return if (isNull(name)) null else getLong(name)
    }
}
