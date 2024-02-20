package io.github.clorissabelles.isabelles_discord_bot.config

import io.github.clorissabelles.isabelles_discord_bot.JSON
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

object ConfigHandler {
	private val configPath = Path.of("data", "config.json")
	private lateinit var config: Config

	@OptIn(ExperimentalSerializationApi::class)
	fun get(): Config {
		if (!ConfigHandler::config.isInitialized) {
			config = if (configPath.exists()) {
				JSON.decodeFromStream(Files.newInputStream(configPath))
			} else {
				Config()
			}
		}

		return config
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun save() {
		if (!configPath.parent.exists()) {
			Files.createDirectories(configPath.parent)
		}

		JSON.encodeToStream(config, Files.newOutputStream(configPath))
	}
}
