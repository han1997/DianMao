package `fun`.han1997.dianmao.data

import org.json.JSONArray
import org.json.JSONObject

object TaskBackupCodec {
    private const val FORMAT = "dianmao.tasks"
    private const val BACKUP_VERSION = 1

    fun encode(tasks: List<TaskEntity>, createdAtMillis: Long = System.currentTimeMillis()): String {
        val root = JSONObject()
            .put("format", FORMAT)
            .put("backupVersion", BACKUP_VERSION)
            .put("databaseVersion", TASK_DATABASE_VERSION)
            .put("createdAtMillis", createdAtMillis)
            .put(
                "tasks",
                JSONArray().apply {
                    tasks.forEach { put(taskToJson(it)) }
                }
            )
        return root.toString(2)
    }

    fun decodeForImport(json: String): List<TaskEntity> {
        val root = JSONObject(json)
        require(root.getString("format") == FORMAT) { "Unsupported backup format" }
        require(root.getInt("backupVersion") <= BACKUP_VERSION) { "Unsupported backup version" }

        val tasks = root.getJSONArray("tasks")
        return List(tasks.length()) { index ->
            taskFromJson(tasks.getJSONObject(index)).copy(calendarEventId = null)
        }
    }

    private fun taskToJson(task: TaskEntity): JSONObject {
        return JSONObject()
            .put("id", task.id)
            .put("title", task.title)
            .put("quadrant", task.quadrant.name)
            .putNullable("dueLabel", task.dueLabel)
            .putNullable("dueAtMillis", task.dueAtMillis)
            .put("useCalendar", task.useCalendar)
            .putNullable("calendarEventId", task.calendarEventId)
            .put("useAlarm", task.useAlarm)
            .put("repeatRule", task.repeatRule.name)
            .put("completed", task.completed)
            .putNullable("completionNote", task.completionNote)
            .put("createdAtMillis", task.createdAtMillis)
            .put("updatedAtMillis", task.updatedAtMillis)
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
            repeatRule = RepeatRule.valueOf(json.getString("repeatRule")),
            completed = json.getBoolean("completed"),
            completionNote = json.nullableString("completionNote"),
            createdAtMillis = json.getLong("createdAtMillis"),
            updatedAtMillis = json.getLong("updatedAtMillis")
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
