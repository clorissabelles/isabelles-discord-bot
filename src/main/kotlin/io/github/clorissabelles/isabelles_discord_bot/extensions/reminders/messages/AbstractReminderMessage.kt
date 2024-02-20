package io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import io.github.clorissabelles.isabelles_discord_bot.config.ConfigHandler
import io.github.clorissabelles.isabelles_discord_bot.config.Reminder
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.ReminderExtension

abstract class AbstractReminderMessage<DataType : Reminder>(
	protected val data: DataType,
	protected val extension: ReminderExtension
) {
	open fun reset() {

	}

	abstract suspend fun update()

	suspend fun getMessage(): Message? {
		val message = extension.bot.kordRef.getChannelOf<MessageChannel>(data.channelId)?.getMessageOrNull(data.messageId)

		if (message == null) {
			extension.invalidateReminder(data)
			ConfigHandler.get().reminders.removeIf { it::class == data::class }
		}

		return message
	}
}

suspend fun EphemeralSlashCommandContext<*, *>.setReminderInstance(data: Reminder, message: String) {
	val config = ConfigHandler.get()

	config.reminders.removeIf { it::class == data::class }
	config.reminders.add(data)

	ConfigHandler.save()

	respond { content = message }
}
