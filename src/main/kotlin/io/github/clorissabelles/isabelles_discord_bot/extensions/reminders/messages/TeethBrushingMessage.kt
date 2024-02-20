package io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
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
import io.github.clorissabelles.isabelles_discord_bot.LATE_REFRESH_HOUR
import io.github.clorissabelles.isabelles_discord_bot.config.TeethBrushingProgress
import io.github.clorissabelles.isabelles_discord_bot.config.TeethBrushingReminder
import io.github.clorissabelles.isabelles_discord_bot.config.TeethBrushingStreak
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.ReminderExtension
import io.github.clorissabelles.isabelles_discord_bot.getCurrentTime
import kotlinx.datetime.Clock

class TeethBrushingMessage(
	data: TeethBrushingReminder,
	extension: ReminderExtension
) : AbstractReminderMessage<TeethBrushingReminder>(data, extension) {
	override fun reset() {
		if (
			data.progress == TeethBrushingProgress.NONE ||
			data.progress.isHalfComplete() && data.streak.yesterdayCompletion.isHalfComplete() && data.streak.ereyesterdayCompletion.isHalfComplete()
			) {
			data.streak.days = 0u
		}

		data.streak.ereyesterdayCompletion = data.streak.yesterdayCompletion
		data.streak.yesterdayCompletion = data.progress
		data.progress = TeethBrushingProgress.NONE
	}

	suspend fun completeMorning() {
		data.progress = TeethBrushingProgress.MORNING

		update()
	}

	suspend fun completeEvening() {
		data.progress = when (data.progress) {
			TeethBrushingProgress.NONE -> TeethBrushingProgress.EVENING
			TeethBrushingProgress.MORNING -> TeethBrushingProgress.BOTH
			else -> return
		}

		if (data.progress == TeethBrushingProgress.BOTH) {
			data.streak.days++
		}

		update()
	}

	override suspend fun update() {
		val message = getMessage() ?: return

		message.edit {
			embed { buildEmbed(data.progress, data.streak) }
			actionRow { buildActionRow(data.progress) }
		}
	}

	companion object {
		const val MORNING_BUTTON_ID = "teeth_brushing_reminder:morning"
		const val EVENING_BUTTON_ID = "teeth_brushing_reminder:evening"
		const val COMPLETED_BUTTON_ID = "teeth_brushing_reminder:completed"

		suspend fun EphemeralSlashCommandContext<Arguments, ModalForm>.createToothBrushingMessage(extension: ReminderExtension): TeethBrushingMessage {
			val message = event.interaction.channel.createMessage {
				embed { buildEmbed(progress = TeethBrushingProgress.NONE, streak = TeethBrushingStreak()) }
				actionRow { buildActionRow(progress = TeethBrushingProgress.NONE) }
			}

			val data = TeethBrushingReminder(message.id, message.channelId, TeethBrushingProgress.NONE, TeethBrushingStreak())

			setReminderInstance(data, "Teeth brushing reminder created.")

			return TeethBrushingMessage(data, extension)
		}

		private fun EmbedBuilder.buildEmbed(progress: TeethBrushingProgress, streak: TeethBrushingStreak) {
			title = "Teeth Brushing Reminder"
			description = buildString {
				when (progress) {
					TeethBrushingProgress.NONE -> {
						if (getCurrentTime().hour >= LATE_REFRESH_HOUR) {
							appendLine("Don't forget to brush your teeth in the evening.")
						} else {
							appendLine("Remember to brush your teeth twice a day.")
						}
					}

					TeethBrushingProgress.MORNING -> appendLine("Good job brushing your teeth, now remember in the evening.")
					TeethBrushingProgress.EVENING -> appendLine("Good job brushing your teeth but remember in the morning tomorrow.")
					TeethBrushingProgress.BOTH -> appendLine("Well done for brushing your teeth today!")
				}

				appendLine()

				if (streak.days > 0u) {
					appendLine("You've brushed your teeth for **${streak.days}** days in a row, good job!")
				}
			}
			color = DISCORD_GREEN
			timestamp = Clock.System.now()
			image = "https://raw.githubusercontent.com/CompassSystem/headmate-labeller/main/resources/filler.png"
		}

		private fun ActionRowBuilder.buildActionRow(progress: TeethBrushingProgress) {
			when (progress) {
				TeethBrushingProgress.NONE -> {
					if (getCurrentTime().hour >= LATE_REFRESH_HOUR) {
						interactionButton(ButtonStyle.Primary, EVENING_BUTTON_ID) {
							emoji = DiscordPartialEmoji(name = "🌙")
							label = "Evening"
						}
					} else {
						interactionButton(ButtonStyle.Primary, MORNING_BUTTON_ID) {
							emoji = DiscordPartialEmoji(name = "☀️")
							label = "Morning"
						}
					}

				}

				TeethBrushingProgress.MORNING -> {
					interactionButton(ButtonStyle.Primary, EVENING_BUTTON_ID) {
						emoji = DiscordPartialEmoji(name = "🌙")
						label = "Evening"
					}
				}

				TeethBrushingProgress.EVENING, TeethBrushingProgress.BOTH -> {
					interactionButton(ButtonStyle.Success, COMPLETED_BUTTON_ID) {
						emoji = if (progress == TeethBrushingProgress.EVENING) {
							DiscordPartialEmoji(id = EMPTY_STAR_EMOJI)
						} else {
							DiscordPartialEmoji(name = "⭐")
						}
						label = "Completed"
						disabled = true
					}
				}
			}
		}
	}
}


