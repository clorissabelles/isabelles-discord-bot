package io.github.clorissabelles.isabelles_discord_bot

import io.github.clorissabelles.isabelles_discord_bot.data.DatabaseHolder
import mu.KotlinLogging

suspend fun main(args: Array<String>) {
    Application.init(args)
}

object Application {
	val LOGGER = KotlinLogging.logger("Isabelle's Discord Bot")

    suspend fun init(args: Array<String>) {
		try {
		    DatabaseHolder.start()
		} catch (e: Exception) {
			LOGGER.error(e) { "Failed to start the database, aborting." }
			return
		}
    }
}
