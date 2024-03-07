package io.github.clorissabelles.isabelles_discord_bot.data

import io.github.clorissabelles.isabelles_discord_bot.Application
import io.ktor.network.sockets.*
import java.sql.Connection
import java.sql.DriverManager

object DatabaseHolder {
	private lateinit var connection: Connection

	@Throws(Exception::class)
	fun start() {
		connection = DriverManager.getConnection("jdbc:h2:./config/database")

		prepareDatabase()
	}

	private fun prepareDatabase() {
		connection.insertOrUpdate("""CREATE TABLE IF NOT EXISTS Versions (
			name VARCHAR(64) PRIMARY KEY,
			version INTEGER NOT NULL
		);""")

		val result = connection.query("SELECT * FROM Versions WHERE name=?;") {
			setString(1, "global")
		}

		if (!result.next()) {
			runMigrations(0, connection)
		} else {
			val version = result.getInt("version")

			runMigrations(version, connection)
		}

		val result2 = connection.query("SELECT * FROM Versions;") {
			//setString(1, "global")
		}

		if (result2.next()) {
			Application.LOGGER.info { "Database version is: ${result2.getInt("version")}" }
		} else {
			Application.LOGGER.info { "Failed to find database version." }
		}
	}

	fun stop() {
		connection.close()
	}
}
