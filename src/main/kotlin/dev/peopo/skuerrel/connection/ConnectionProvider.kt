package dev.peopo.skuerrel.connection

import java.sql.Connection

interface ConnectionProvider {

	fun getConnection() : Connection

	fun setCommit(autoCommit : Boolean)
}