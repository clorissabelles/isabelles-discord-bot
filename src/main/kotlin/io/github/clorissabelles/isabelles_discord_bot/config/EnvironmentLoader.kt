package io.github.clorissabelles.isabelles_discord_bot.config

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.forEachLine

object EnvironmentLoader {
	private var _env: Map<String, String>? = null

	fun env(key: String): String {
		_env = _env ?: loadEnvironment()

		return _env!![key] ?: System.getenv(key)
	}

	fun envOrNull(key: String): String? {
		_env = _env ?: loadEnvironment()

		return _env!![key] ?: try {
			System.getenv(key)
		} catch (e: Exception) {
			null
		}
	}

	private fun loadEnvironment(): Map<String, String> {
		val envPath = Path.of("data", ".env")

		if (!envPath.exists()) {
			return emptyMap()
		}

		return buildMap {
			envPath.forEachLine {
				val (key, value) = it.split("=", limit = 2)
				put(key, value)
			}
		}
	}
}
