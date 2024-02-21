package io.github.clorissabelles.isabelles_discord_bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.clorissabelles.isabelles_discord_bot.extensions.ColouringExtension

suspend fun main(args: Array<String>) {
    Application.init(args)
}

object Application {
	private lateinit var bot: ExtensibleBot

    suspend fun init(args: Array<String>) {
		LOGGER.info { "Setting up Discord bot." }

		createAndStartBot()
    }

	private suspend fun createAndStartBot() {
		bot = ExtensibleBot(DISCORD_TOKEN) {
			applicationCommands {
				defaultGuild(GUILD)
			}

			extensions {
				add(::ColouringExtension)
			}
		}

		LOGGER.info { "Starting Discord bot." }

		bot.start()
	}
}
