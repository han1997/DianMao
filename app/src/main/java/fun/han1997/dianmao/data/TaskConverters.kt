package `fun`.han1997.dianmao.data

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class TaskConverters {
    @TypeConverter
    fun toQuadrantType(value: String): QuadrantType = QuadrantType.valueOf(value)

    @TypeConverter
    fun fromQuadrantType(value: QuadrantType): String = value.name

    @TypeConverter
    fun toRepeatRule(value: String): RepeatRule = RepeatRule.valueOf(value)

    @TypeConverter
    fun fromRepeatRule(value: RepeatRule): String = value.name

    @TypeConverter
    fun toRepeatWeekdays(value: String): Set<Int> {
        if (value.isBlank()) return emptySet()
        return value.split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..7 }
            .toSet()
    }

    @TypeConverter
    fun fromRepeatWeekdays(value: Set<Int>): String {
        return value.filter { it in 1..7 }.sorted().joinToString(",")
    }

    @TypeConverter
    fun toSubTasks(value: String): List<TaskSubTask> {
        if (value.isBlank()) return emptyList()
        val array = JSONArray(value)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            TaskSubTask(
                id = item.optLong("id", index + 1L),
                title = item.getString("title"),
                completed = item.optBoolean("completed", false)
            )
        }
    }

    @TypeConverter
    fun fromSubTasks(value: List<TaskSubTask>): String {
        return JSONArray().apply {
            value
                .filter { it.title.isNotBlank() }
                .forEach { item ->
                    put(
                        JSONObject()
                            .put("id", item.id)
                            .put("title", item.title)
                            .put("completed", item.completed)
                    )
                }
        }.toString()
    }
}
