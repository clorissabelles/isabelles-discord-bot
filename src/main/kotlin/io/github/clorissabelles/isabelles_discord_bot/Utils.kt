package io.github.clorissabelles.isabelles_discord_bot

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import io.github.clorissabelles.isabelles_discord_bot.config.ConfigHandler
import io.github.clorissabelles.isabelles_discord_bot.config.EnvironmentLoader.env
import io.github.clorissabelles.isabelles_discord_bot.config.EnvironmentLoader.envOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import mu.KotlinLogging

val DISCORD_TOKEN = env("DISCORD_TOKEN")
val GUILD = envOrNull("GUILD")
val OWNER_SNOWFLAKE = Snowflake(env("OWNER_SNOWFLAKE"))

val LOGGER = KotlinLogging.logger("isabelles-discord-bot")

val EMPTY_STAR_EMOJI = Snowflake("1179024220861243452")

const val EARLY_REFRESH_HOUR = 4
const val LATE_REFRESH_HOUR = 16

@OptIn(ExperimentalSerializationApi::class)
val JSON: Json = Json {
	ignoreUnknownKeys = true
	prettyPrint = true
	prettyPrintIndent = "  "
	encodeDefaults = true
	namingStrategy = JsonNamingStrategy.SnakeCase
}

fun getCurrentTime(): LocalDateTime {
	return Clock.System.now().toLocalDateTime(ConfigHandler.get().misc.timezone)
}

fun Arguments.localDate(
	body: LocalDateConverterBuilder.() -> Unit
): SingleConverter<LocalDate> {
	val builder = LocalDateConverterBuilder()

	body(builder)

	builder.validateArgument()

	return builder.build(this)
}

private class LocalDateConverter(
	override var validator: Validator<LocalDate> = null
) : SingleConverter<LocalDate>() {
	override val signatureTypeString: String = "local date"
	override val showTypeInSignature: Boolean = false

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		parsed = convertString(arg)

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply {
			this@apply.maxLength = 3 + 3 + 4
			this@apply.minLength = 3 + 3 + 2

			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false

		parsed = convertString(optionValue)

		return true
	}

	private fun convertString(value: String): LocalDate {
		// todo: support ISO-8601 dates
		if (value.length < 3 + 3 + 2) {
			throw DiscordRelayedException("Too short, date must be in the format DD/MM/[YY]YY.")
		} else if (value.length > 3 + 3 + 4) {
			throw DiscordRelayedException("Too long, date must be in the format DD/MM/[YY]YY.")
		}

		return try {
			val parts = value.split("/").map { it.toInt() }

			if (parts.size != 3) {
				throw DiscordRelayedException("Date must be in the format DD/MM/[YY]YY.")
			}

			val year = if (parts[2] < 100) {
				2000 + parts[2]
			} else {
				parts[2]
			}

			LocalDate(year, parts[1], parts[0])
		} catch (e: NumberFormatException) {
			throw DiscordRelayedException("Part of date is not a number")
		} catch (e: IllegalArgumentException) {
			throw DiscordRelayedException("Day, month, or year is invalid")
		}
	}
}

class LocalDateConverterBuilder : ConverterBuilder<LocalDate>() {
	override fun build(arguments: Arguments): SingleConverter<LocalDate> {
		return arguments.arg(
			displayName = name,
			description = description,

			converter = LocalDateConverter().withBuilder(this)
		)
	}
}
