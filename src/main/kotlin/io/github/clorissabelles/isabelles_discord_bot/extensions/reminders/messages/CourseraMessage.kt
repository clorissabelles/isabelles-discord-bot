package io.github.ellie_semele.ellie_utility_bot.extensions.reminders.messages

import com.kotlindiscord.kord.extensions.DISCORD_FUCHSIA
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed
import io.github.clorissabelles.isabelles_discord_bot.config.CourseraReminder
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.ReminderExtension
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages.AbstractReminderMessage
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages.setReminderInstance
import io.github.clorissabelles.isabelles_discord_bot.getCurrentTime
import io.github.clorissabelles.isabelles_discord_bot.localDate
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.math.ceil

class CourseraMessage(
	data: CourseraReminder,
	extension: ReminderExtension
) : AbstractReminderMessage<CourseraReminder>(data, extension) {
	suspend fun completeCourse() {
		data.coursesCompleted++

		update()
	}

	override suspend fun update() {
		val message = getMessage() ?: return

		message.edit {
			embed { buildEmbed(data.totalCourses - data.coursesCompleted, data.endDate) }

			if (data.totalCourses == data.coursesCompleted) {
				components = mutableListOf()
			} else {
				actionRow { buildActionRow() }
			}
		}
	}

	companion object {
		const val COMPLETE_COURSE_BUTTON_ID = "coursera_reminder:complete_course"

		suspend fun EphemeralSlashCommandContext<CourseraReminderArgs, ModalForm>.createCourseraMessage(extension: ReminderExtension): CourseraMessage {
			val message = event.interaction.channel.createMessage {
				embed { buildEmbed(arguments.remaining.toUInt(), arguments.endDate) }
				actionRow { buildActionRow() }
			}

			val data = CourseraReminder(message.id, message.channelId, arguments.completed.toUInt(), (arguments.completed + arguments.remaining).toUInt(), arguments.endDate)

			setReminderInstance(data, "Coursera reminder created.")

			return CourseraMessage(data, extension)
		}

		private fun EmbedBuilder.buildEmbed(remainingCourses: UInt, endDate: LocalDate) {
			title = "Coursera reminder"
			description = if (remainingCourses == 0.toUInt()) {
				"Congratulations! You have completed all your courses!"
			} else {
				val daysUntilEnd = getCurrentTime().date.daysUntil(endDate)
				val coursesPerWeek = ceil(7 * remainingCourses.toDouble() / daysUntilEnd).toInt()

				val endDateAsString = endDate.let {
					"${it.dayOfMonth.toString().padStart(2, '0')}/${it.monthNumber.toString().padStart(2, '0')}/${it.year}"
				}

				"""
				|You have **${remainingCourses}** courses remaining to complete by **${endDateAsString}**.
				|
				|You should try to complete **${coursesPerWeek}** courses per week.
				|""".trimMargin().trimStart()
			}
			color = DISCORD_FUCHSIA
			timestamp = Clock.System.now()
			image = "https://raw.githubusercontent.com/CompassSystem/headmate-labeller/main/resources/filler.png"
		}

		private fun ActionRowBuilder.buildActionRow() {
			interactionButton(ButtonStyle.Primary, COMPLETE_COURSE_BUTTON_ID) {
				emoji = DiscordPartialEmoji(name = "✅")
			}
		}
	}
}

class CourseraReminderArgs : Arguments() {
	val completed by int {
		name = "completed"
		description = "The number of courses completed."
		minValue = 0
	}

	val remaining by int {
		name = "remaining"
		description = "The remaining number of courses to complete."
		minValue = 0
	}

	val endDate by localDate {
		name = "end-date"
		description = "The deadline to finish courses by."
	}
}
