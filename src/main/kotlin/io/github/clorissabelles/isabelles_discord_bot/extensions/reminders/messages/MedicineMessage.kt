package io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.messages

import com.kotlindiscord.kord.extensions.DISCORD_WHITE
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
import io.github.clorissabelles.isabelles_discord_bot.config.ConfigHandler
import io.github.clorissabelles.isabelles_discord_bot.config.MedicineReminder
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.ReminderExtension
import kotlinx.datetime.Clock

class MedicineMessage(
	data: MedicineReminder,
	extension: ReminderExtension
) : AbstractReminderMessage<MedicineReminder>(data, extension) {
	private val estrogenPumpsPerDay: UInt = ConfigHandler.get().misc.estrogenPumpsPerDay
	private val estrogenPumpsPerPack: UInt = ConfigHandler.get().misc.estrogenPumpsPerPack

	fun hasEstrogen() : Boolean {
		return data.estrogenPumps >= estrogenPumpsPerDay
	}

	suspend fun useEstrogen() {
		data.estrogenPumps -= estrogenPumpsPerDay

		update()
	}

	fun hasCutAntiAndrogen() : Boolean {
		return data.cutAntiAndrogens >= 1u
	}

	suspend fun useAntiAndrogen() {
		data.cutAntiAndrogens--

		update()
	}

	fun hasWholeAntiAndrogen(): Boolean {
		return data.wholeAntiAndrogens >= 1u
	}

	suspend fun cutAntiAndrogen() {
		data.cutAntiAndrogens += 4u
		data.wholeAntiAndrogens--

		update()
	}

	override suspend fun update() {
		val message = getMessage() ?: return

		message.edit {
			embed { buildEmbed(data.estrogenPumps, estrogenPumpsPerDay, data.cutAntiAndrogens, data.wholeAntiAndrogens) }
			actionRow { buildActionRow() }
		}
	}

	suspend fun restock(estrogenPacks: UInt, wholeAntiAndrogens: UInt) {
		data.estrogenPumps += estrogenPacks * estrogenPumpsPerPack
		data.wholeAntiAndrogens += wholeAntiAndrogens

		update()
	}

	companion object {
		const val RESTOCK_BUTTON_ID = "medicine_reminder:restock"

		suspend fun EphemeralSlashCommandContext<MedicineReminderArgs, ModalForm>.createMedicineMessage(extension: ReminderExtension): MedicineMessage {
			val estrogenPumps = arguments.estrogenPumps.toUInt()
			val cutAntiAndrogen = arguments.cutAntiAndrogen.toUInt()
			val wholeAntiAndrogens = arguments.wholeAntiAndrogens.toUInt()

			val message = event.interaction.channel.createMessage {
				embed { buildEmbed(estrogenPumps, ConfigHandler.get().misc.estrogenPumpsPerDay, cutAntiAndrogen, wholeAntiAndrogens) }
				actionRow { buildActionRow() }
			}

			val data = MedicineReminder(message.id, message.channelId, estrogenPumps, cutAntiAndrogen, wholeAntiAndrogens)

			setReminderInstance(data, "Medicine reminder created.")

			return MedicineMessage(data, extension)
		}

		private fun EmbedBuilder.buildEmbed(estrogenPumps: UInt, estrogenPumpsPerDay: UInt, cutAntiAndrogen: UInt, wholeAntiAndrogen: UInt) {
			val daysOfEstrogen = estrogenPumps / estrogenPumpsPerDay
			val daysOfAntiAndrogens = cutAntiAndrogen + 4u * wholeAntiAndrogen

			val daysUntilRestock = minOf(daysOfEstrogen, daysOfAntiAndrogens) - ConfigHandler.get().misc.minDaysForMedicineReorder

			title = "Medicine Reminder"
			description = """
			| You have **$estrogenPumps** pumps of estrogen remaining and
			| you have **$cutAntiAndrogen** prepared and **$wholeAntiAndrogen** whole anti-androgens remaining.
			|
			| You should buy more medicine in ${stringify(daysUntilRestock)}.
			""".trimMargin().trimStart()
			color = DISCORD_WHITE
			timestamp = Clock.System.now()
			image = "https://raw.githubusercontent.com/CompassSystem/headmate-labeller/main/resources/filler.png"
		}

		private fun ActionRowBuilder.buildActionRow() {
			interactionButton(ButtonStyle.Primary, RESTOCK_BUTTON_ID) {
				label = "Restock"
				emoji = DiscordPartialEmoji(name = "📦")
			}
		}

		private fun stringify(days: UInt): String {
			if (days == 0u) {
				return "**now**"
			} else {
				val weeks = days / 7u
				val weekDays = days - weeks * 7u

				val weeksString = run {
					when (weeks) {
						0u -> ""
						1u -> "**1** week"
						else -> "**$weeks** weeks"
					}
				}

				val weekDaysString = run {
					when (weekDays) {
						0u -> ""
						1u -> "**1** day"
						else -> "**$weekDays** days"
					}
				}

				return if (weeksString.isNotEmpty() && weekDaysString.isNotEmpty()) {
					"$weeksString and $weekDaysString"
				} else {
					weeksString + weekDaysString
				}
			}
		}
	}
}

class MedicineRestockModal : ModalForm() {
	override var title = "Restock Medicine"

	val estrogenPacks = lineText {
		label = "Estrogen Packs"
	}

	val wholeAntiAndrogens = lineText {
		label = "Anti-Androgens"
	}
}

class MedicineReminderArgs : Arguments() {
	val estrogenPumps by int {
		name = "estrogen"
		description = "The number of estrogen pumps remaining."
	}

	val cutAntiAndrogen by int {
		name = "cut-anti-androgen"
		description = "The number of cut anti-androgen pills remaining."
	}

	val wholeAntiAndrogens by int {
		name = "whole-anti-androgen"
		description = "The number of whole anti-androgen pills remaining."
	}
}
