package io.github.clorissabelles.isabelles_discord_bot

import io.github.clorissabelles.isabelles_discord_bot.EnvironmentLoader.env
import io.github.clorissabelles.isabelles_discord_bot.EnvironmentLoader.envOrNull
import mu.KotlinLogging

val DISCORD_TOKEN = env("DISCORD_TOKEN")
val GUILD = envOrNull("GUILD")

val LOGGER = KotlinLogging.logger("isabelles-discord-bot")
