package io.github.clorissabelles.isabelles_discord_bot.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.kord.common.Color
import dev.kord.core.behavior.createRole

class ColouringExtension : Extension() {
	override val name: String = "colouring-extension"

	override suspend fun setup() {
		ephemeralSlashCommand(::ColourNameArguments) {
			name = "colour"
			description = "Change the colour of your name."

			action {
				colourUserName()
			}
		}
	}

	private suspend fun EphemeralSlashCommandContext<ColourNameArguments, *>.colourUserName() {
		val guildId = event.interaction.command.data.guildId

		if (guildId.value == null) {
			respond {
				content = "This command can only be used in a guild."
			}

			return
		}

		val guild = event.kord.getGuild(guildId.value!!)

		val colour = colourOrNull(arguments.colour)

		if (colour == null) {
			respond {
				content = "Invalid colour, must be in format #RRGGBB."
			}

			return
		}

		val role = guild.createRole {
			name = arguments.name
			color = colour
		}

		event.interaction.user.asMember(guildId.value!!).addRole(role.id)

		respond {
			content = "Created test role and assigned it to you."
		}
	}

	private fun colourOrNull(input: String): Color? {
		val hex = input.removePrefix("#")

		if (hex.length != 6) {
			return null
		}

		val red = hex.substring(0, 2).toIntOrNull(16) ?: return null
		val green = hex.substring(2, 4).toIntOrNull(16) ?: return null
		val blue = hex.substring(4, 6).toIntOrNull(16) ?: return null

		return Color(red, green, blue)
	}
}

class ColourNameArguments : Arguments() {
	val name: String by string {
		name = "name"
		description = "The name of the role."
	}

	val colour: String by string {
		name = "colour"
		description = "The hex colour you want to change your name to."
	}
}
