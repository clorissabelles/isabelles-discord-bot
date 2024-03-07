package io.github.clorissabelles.isabelles_discord_bot.data

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

fun Connection.insertOrUpdate(sql: String, block: PreparedStatement.() -> Unit = { }) {
	val statement = prepareStatement(sql)
	statement.block()
	statement.execute()
	statement.close()
}

fun Connection.query(sql: String, block: PreparedStatement.() -> Unit): ResultSet {
	val statement = prepareStatement(sql)
	statement.block()
	return statement.executeQuery()
}

fun runMigrations(fromVersion: Int, connection: Connection) {
	var version = fromVersion
	var previousVersion: Int

	do {
	    previousVersion = version

		version = when (version) {
			0 -> connection.migrate0to1()
			else -> version
		}

	} while (version != previousVersion)

	if (version != fromVersion) {
		connection.insertOrUpdate("UPDATE Versions SET version=? WHERE name=?;") {
			setInt(1, version)
			setString(2, "global")
		}
	}
}

fun Connection.migrate0to1(): Int {
	return 1
}
