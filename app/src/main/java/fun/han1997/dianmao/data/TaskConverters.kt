package `fun`.han1997.dianmao.data

import androidx.room.TypeConverter

class TaskConverters {
    @TypeConverter
    fun toQuadrantType(value: String): QuadrantType = QuadrantType.valueOf(value)

    @TypeConverter
    fun fromQuadrantType(value: QuadrantType): String = value.name

    @TypeConverter
    fun toRepeatRule(value: String): RepeatRule = RepeatRule.valueOf(value)

    @TypeConverter
    fun fromRepeatRule(value: RepeatRule): String = value.name
}
