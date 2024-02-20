package io.github.clorissabelles.isabelles_discord_bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.clorissabelles.isabelles_discord_bot.config.ConfigHandler
import io.github.clorissabelles.isabelles_discord_bot.extensions.reminders.ReminderExtension

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
		Runtime.getRuntime().addShutdownHook(Thread {
			LOGGER.info { "Saving config." }

			try {
				ConfigHandler.save()
				LOGGER.info { "Config has been saved." }
			} catch (e: Exception) {
				LOGGER.error(e) { "Failed to save config." }
			}
		})

		bot = ExtensibleBot(DISCORD_TOKEN) {
			applicationCommands {
				defaultGuild(GUILD)

				slashCommandCheck {
					failIfNot(
						event.interaction.user.id == OWNER_SNOWFLAKE,
						"Must be <@${OWNER_SNOWFLAKE}> to use this command."
					)
				}
			}

			extensions {
				add(::ReminderExtension)
			}
		}

		LOGGER.info { "Starting Discord bot." }

		bot.start()
	}
}
