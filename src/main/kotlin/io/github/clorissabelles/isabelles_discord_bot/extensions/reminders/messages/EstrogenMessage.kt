package io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed
import io.github.clorissabelles.isabelles_discord_bot.EMPTY_STAR_EMOJI
import io.github.clorissabelles.isabelles_discord_bot.config.EstrogenLeg
import io.github.clorissabelles.isabelles_discord_bot.config.EstrogenReminder
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.ReminderExtension
import io.github.clorissabelles.isabelles_discord_bot.getCurrentTime
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

class EstrogenMessage(
	data: EstrogenReminder,
	extension: ReminderExtension
) : AbstractReminderMessage<EstrogenReminder>(data, extension) {
	override fun reset() {
		data.completed = false
	}

	suspend fun complete() {
		data.completed = true

		update()
	}

	override suspend fun update() {
		val message = getMessage() ?: return

		message.edit {
			embed { buildEmbed(data.completed, data.doubleDoseLeg, data.doubleDoseStartDate) }

			actionRow { buildActionRow(data.completed) }
		}
	}

	companion object {
		const val COMPLETE_BUTTON_ID = "estrogen_reminder:complete"
		const val COMPLETED_BUTTON_ID = "estrogen_reminder:completed"

		suspend fun EphemeralSlashCommandContext<EstrogenReminderArgs, ModalForm>.createEstrogenMessage(extension: ReminderExtension): EstrogenMessage {
			val date = getCurrentTime().date

			val message = event.interaction.channel.createMessage {
				embed { buildEmbed(false, arguments.leg, date) }
				actionRow { buildActionRow(false) }
			}

			val data = EstrogenReminder(message.id, message.channelId, arguments.leg, getCurrentTime().date, false)

			setReminderInstance(data, "Estrogen reminder created.")

			return EstrogenMessage(data, extension)
		}

		private fun EmbedBuilder.buildEmbed(complete: Boolean, startLeg: EstrogenLeg, startDate: LocalDate) {
			title = "Estrogen Reminder"
			description = if (complete) {
				"""
				|Good job on doing your estrogen today!
				|
				|Check back tomorrow.
				""".trimMargin().trimStart()
			} else {
				val leg = if (startDate.daysUntil(getCurrentTime().date) % 2 == 0) {
					startLeg
				} else {
					startLeg.opposite()
				}

				"""
				|Today is **${leg}** leg day.
				|
				|Remember to apply 2 doses of estrogen to your **${leg}** leg.
				""".trimMargin().trimStart()
			}
			color = DISCORD_BLURPLE
			timestamp = Clock.System.now()
			image = "https://raw.githubusercontent.com/CompassSystem/headmate-labeller/main/resources/filler.png"
		}

		private fun ActionRowBuilder.buildActionRow(complete: Boolean) {
			if (complete) {
				interactionButton(ButtonStyle.Success, COMPLETED_BUTTON_ID) {
					label = "Completed"
					emoji = DiscordPartialEmoji(name = "⭐")
					disabled = true
				}
			} else {
				interactionButton(ButtonStyle.Primary, COMPLETE_BUTTON_ID) {
					label = "Complete"
					emoji = DiscordPartialEmoji(id = EMPTY_STAR_EMOJI)
				}
			}
		}
	}

}

class EstrogenReminderArgs : Arguments() {
	val leg by enumChoice<EstrogenLeg> {
		name = "leg"
		description = "The leg to apply 2 doses of estrogen to today."
		typeName = "EstrogenLeg"
	}
}
