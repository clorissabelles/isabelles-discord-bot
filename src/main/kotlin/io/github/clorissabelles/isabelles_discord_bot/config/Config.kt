package io.github.clorissabelles.isabelles_discord_bot.config

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
	val enabledExtensions: MutableMap<String, Boolean> = mutableMapOf(),
	val reminders: MutableList<Reminder> = mutableListOf(),
	val misc: Miscellaneous = Miscellaneous()
)

//#region // Reminders
@Serializable
sealed class Reminder {
	abstract val messageId: Snowflake
	abstract val channelId: Snowflake
}

//#region // Teeth Brushing Reminder
@Serializable
@SerialName("isabelles_discord_bot:teeth_brushing_reminder")
class TeethBrushingReminder(
	override val messageId: Snowflake,
	override val channelId: Snowflake,
	var progress: TeethBrushingProgress,
	val streak: TeethBrushingStreak
) : Reminder()

@Serializable
enum class TeethBrushingProgress {
	@SerialName("none") NONE,
	@SerialName("morning") MORNING,
	@SerialName("evening") EVENING,
	@SerialName("both") BOTH;

	fun isHalfComplete(): Boolean = this == MORNING || this == EVENING
}

@Serializable
data class TeethBrushingStreak(
	var days: UInt = 0u,
	var yesterdayCompletion: TeethBrushingProgress = TeethBrushingProgress.NONE,
	var ereyesterdayCompletion: TeethBrushingProgress = TeethBrushingProgress.NONE
)
//#endregion

//#region // Estrogen Reminder
@Serializable
@SerialName("isabelles_discord_bot:estrogen_reminder")
class EstrogenReminder(
	override val messageId: Snowflake,
	override val channelId: Snowflake,
	val doubleDoseLeg: EstrogenLeg,
	val doubleDoseStartDate: LocalDate,
	var completed: Boolean
) : Reminder()

@Serializable
enum class EstrogenLeg(override val readableName: String) : ChoiceEnum {
	@SerialName("left") LEFT("Left leg"),
	@SerialName("right") RIGHT("Right leg");

	override fun toString(): String = name.lowercase()

	fun opposite(): EstrogenLeg = when (this) {
		LEFT -> RIGHT
		RIGHT -> LEFT
	}
}
//#endregion

//#region // Anti-Androgen Reminder
@Serializable
@SerialName("isabelles_discord_bot:anti_androgen_reminder")
class AntiAndrogenReminder(
	override val messageId: Snowflake,
	override val channelId: Snowflake,
	var completed: Boolean,
	var lastUsedSize: AntiAndrogenSize? // null if not used yet
) : Reminder()

@Serializable
enum class AntiAndrogenSize {
	@SerialName("small") SMALL,
	@SerialName("medium") MEDIUM,
	@SerialName("large") LARGE;

	fun opposite() = when (this) {
		SMALL -> LARGE
		MEDIUM -> MEDIUM
		LARGE -> SMALL
	}

	override fun toString() = when (this) {
		SMALL -> "small"
		MEDIUM -> "medium"
		LARGE -> "big"
	}
}
//#endregion

@Serializable
@SerialName("isabelles_discord_bot:coursera_reminder")
class CourseraReminder(
	override val messageId: Snowflake,
	override val channelId: Snowflake,
	var coursesCompleted: UInt,
	val totalCourses: UInt,
	val endDate: LocalDate
) : Reminder()

@Serializable
@SerialName("isabelles_discord_bot:medicine_reminder")
class MedicineReminder(
	override val messageId: Snowflake,
	override val channelId: Snowflake,
	var estrogenPumps: UInt,
	var cutAntiAndrogens: UInt,
	var wholeAntiAndrogens: UInt
) : Reminder()
//#endregion

@Serializable
data class Miscellaneous(
	val estrogenPumpsPerDay: UInt = 3u,
	val estrogenPumpsPerPack: UInt = 64u,
	val minDaysForMedicineReorder: UInt = 51u,
	val timezone: TimeZone = TimeZone.of("Europe/London"),
	var lastEarlyRefresh: LocalDate? = null
)
