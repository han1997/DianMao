package `fun`.han1997.dianmao.data

/**
 * Pure decoding rules for [TaskBackupCodec], kept free of `org.json` and
 * Android types so they are runnable in local JVM tests. Android's `org.json`
 * is a stub that throws in unit tests, so the codec owns the JSON plumbing and
 * delegates its data-safety decisions here.
 */
object BackupCodecRules {
    /** Marker that identifies a DianMao backup payload. */
    const val FORMAT = "dianmao.tasks"

    /** Highest backup version this build writes and accepts on import. */
    const val SUPPORTED_VERSION = 7

    /**
     * True when the payload declares the DianMao format and a version this
     * build understands. A wrong format or a newer version is rejected so an
     * unrecognized payload can never replace local rows.
     */
    fun acceptsPayload(format: String?, backupVersion: Int?): Boolean {
        return format == FORMAT && backupVersion != null && backupVersion in 1..SUPPORTED_VERSION
    }

    /** Keeps only ISO weekday numbers (1..7) from a raw stored array. */
    fun sanitizeRepeatWeekdays(raw: List<Int>): Set<Int> {
        return raw.filter { it in 1..7 }.toSet()
    }

    /** Keeps only non-negative reminder lead times from a raw stored array. */
    fun sanitizeReminderLeadExtras(raw: List<Int>): Set<Int> {
        return raw.filter { it >= 0 }.toSet()
    }
}

/**
 * Tolerant enum decoders shared by Room TypeConverters and the backup codec.
 * An unknown stored name returns null instead of throwing, so one corrupt row
 * cannot take down a whole table read. Pure and JVM-testable.
 */
fun quadrantTypeOrNull(name: String?): QuadrantType? {
    return QuadrantType.entries.firstOrNull { it.name == name }
}

fun repeatRuleOrNull(name: String?): RepeatRule? {
    return RepeatRule.entries.firstOrNull { it.name == name }
}