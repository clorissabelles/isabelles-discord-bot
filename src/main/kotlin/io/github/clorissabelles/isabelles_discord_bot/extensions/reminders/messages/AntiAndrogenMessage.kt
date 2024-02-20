package io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages

import com.kotlindiscord.kord.extensions.DISCORD_PINK
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
import io.github.clorissabelles.isabelles_discord_bot.config.AntiAndrogenReminder
import io.github.clorissabelles.isabelles_discord_bot.config.AntiAndrogenSize
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.ReminderExtension
import kotlinx.datetime.Clock

class AntiAndrogenMessage(
	data: AntiAndrogenReminder,
	extension: ReminderExtension
) : AbstractReminderMessage<AntiAndrogenReminder>(data, extension) {
	override fun reset() {
		data.completed = false
	}

	suspend fun use(size: AntiAndrogenSize) {
		data.completed = true

		data.lastUsedSize = if (data.lastUsedSize == size.opposite()) {
			null
		} else {
			size
		}

		update()
	}

	override suspend fun update() {
		val message = getMessage() ?: return

		message.edit {
			embed { buildEmbed(data.completed, data.lastUsedSize) }
			actionRow { buildActionRow(data.completed) }
		}
	}

	companion object {
		const val COMPLETED_BUTTON_ID = "anti_androgen_reminder:completed"
		const val USE_SMALL_BUTTON_ID = "anti_androgen_reminder:use_small"
		const val USE_MEDIUM_BUTTON_ID = "anti_androgen_reminder:use_medium"
		const val USE_LARGE_BUTTON_ID = "anti_androgen_reminder:use_large"
		const val CUT_ANTI_ANDROGEN_BUTTON_ID = "anti_androgen_reminder:cut_anti_androgen"

		suspend fun EphemeralSlashCommandContext<Arguments, ModalForm>.createAntiAndrogenMessage(extension: ReminderExtension) : AntiAndrogenMessage {
			val message = event.interaction.channel.createMessage {
				embed { buildEmbed(false, AntiAndrogenSize.MEDIUM) }
				actionRow { buildActionRow(false) }
			}

			val data = AntiAndrogenReminder(
				messageId = message.id,
				channelId = message.channelId,
				completed = false,
				lastUsedSize = null
			)

			setReminderInstance(data, "Anti-androgen reminder created.")

			return AntiAndrogenMessage(data, extension)
		}

		fun EmbedBuilder.buildEmbed(completed: Boolean, lastUsedSize: AntiAndrogenSize?) {
			title = "Anti-Androgen Reminder"
			description = if (completed) {
				"""
				|Good job on doing your anti-androgen today!
				|
				|Check back tomorrow.
				""".trimMargin().trimStart()
			} else {
				val sizeString = when (lastUsedSize) {
					AntiAndrogenSize.MEDIUM, null -> "an"
					else -> "a **${lastUsedSize.opposite()}**"
				}

				"Remember to take $sizeString anti-androgen today."
			}
			color = DISCORD_PINK
			timestamp = Clock.System.now()
			image = "https://raw.githubusercontent.com/CompassSystem/headmate-labeller/main/resources/filler.png"
		}

		fun ActionRowBuilder.buildActionRow(completed: Boolean) {
			if (completed) {
				interactionButton(ButtonStyle.Success, COMPLETED_BUTTON_ID) {
					label = "Completed"
					emoji = DiscordPartialEmoji(name = "⭐")
					disabled = true
				}
			} else {
				interactionButton(ButtonStyle.Primary, USE_LARGE_BUTTON_ID) {
					label = "Large"
					emoji = DiscordPartialEmoji(name = "💊")
				}

				interactionButton(ButtonStyle.Primary, USE_MEDIUM_BUTTON_ID) {
					label = "Medium"
					emoji = DiscordPartialEmoji(name = "💊")
				}

				interactionButton(ButtonStyle.Primary, USE_SMALL_BUTTON_ID) {
					label = "Small"
					emoji = DiscordPartialEmoji(name = "💊")
				}
			}

			interactionButton(ButtonStyle.Secondary, CUT_ANTI_ANDROGEN_BUTTON_ID) {
				label = "Cut"
				emoji = DiscordPartialEmoji(name = "✂️")
			}
		}
	}
}
