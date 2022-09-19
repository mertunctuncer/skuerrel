package dev.peopo.apexsql

import com.zaxxer.hikari.HikariDataSource
import dev.peopo.apexsql.annotation.Table
import dev.peopo.apexsql.data.SQLPairList
import dev.peopo.apexsql.query.row.DeleteQuery
import dev.peopo.apexsql.query.row.InsertQuery
import dev.peopo.apexsql.query.row.SelectQuery
import dev.peopo.apexsql.query.row.UpdateQuery
import dev.peopo.apexsql.query.table.CreateTableQuery
import dev.peopo.apexsql.query.table.DropTableQuery
import dev.peopo.apexsql.reflection.DataSerializer
import dev.peopo.apexsql.reflection.TableSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


class Table<T: Any> (val dataSource: HikariDataSource, kClass : KClass<out T>) {

	internal val columns = TableSerializer.serialize(kClass)
	internal val name : String = kClass.findAnnotation<Table>()?.name ?: throw IllegalArgumentException("Class must have @Table annotation")


	@Suppress("unused")
	suspend fun fetchExistsAsync() = CoroutineScope(Dispatchers.IO).async {
		var connection: Connection? = null
		var result : ResultSet? = null
		try {
			connection = dataSource.connection
			val meta = connection.metaData
			result = meta.getTables(null, null, name, null)
			val exist = result!!.next()
			connection.commit()
			return@async exist
		} catch (e: SQLException) {
			connection?.rollback()
			e.printStackTrace()
			return@async false
		} finally {
			result?.close()
			connection?.close()
		}
	}.await()

	@Suppress("unused")
	fun createAsync(@Suppress("unused_parameter")ifNotExist : Boolean = true) = CoroutineScope(Dispatchers.IO).launch {
		CreateTableQuery(dataSource.connection, this@Table, true).execute()
	}

	@Suppress("unused")
	fun dropAsync() = CoroutineScope(Dispatchers.IO).launch {
		DropTableQuery(dataSource.connection, this@Table).execute()
	}

	@Suppress("unused")
	fun insertAsync(serializable: T) = CoroutineScope(Dispatchers.IO).launch {
		InsertQuery(dataSource.connection, this@Table, DataSerializer.serialize(serializable)).execute()
	}

	@Suppress("unused")
	fun updateAsync(serializable: T, where: SQLPairList) = CoroutineScope(Dispatchers.IO).launch {
		UpdateQuery(dataSource.connection, this@Table, DataSerializer.serialize(serializable), where).execute()
	}

	@Suppress("unused")
	fun updateAsync(set: SQLPairList, where: SQLPairList) = CoroutineScope(Dispatchers.IO).launch {
		UpdateQuery(dataSource.connection, this@Table, set, where)
	}

	@Suppress("unused")
	suspend inline fun <reified K: Any>fetchAsync(where: SQLPairList) = CoroutineScope(Dispatchers.IO).async {
		val result = SelectQuery(dataSource.connection, this@Table, where).execute()
		return@async result.map { DataSerializer.deserialize<K>(it) }
	}.await()

	@Suppress("unused")
	suspend inline fun <reified K: Any> fetchAsync(where: String, values: List<Any?>) = CoroutineScope(Dispatchers.IO).async {
		val result = SelectQuery(dataSource.connection, this@Table, where, values).execute()
		return@async result.map { DataSerializer.deserialize<K>(it) }
	}.await()

	@Suppress("unused")
	suspend inline fun <reified K: Any> fetchAllAsync() = CoroutineScope(Dispatchers.IO).async {
		val result = SelectQuery(dataSource.connection, this@Table).execute()
		return@async result.map { DataSerializer.deserialize<K>(it) }
	}.await()

	@Suppress("unused")
	fun deleteAsync(where: SQLPairList) = CoroutineScope(Dispatchers.IO).launch { DeleteQuery(dataSource.connection, this@Table, where) }

	@Suppress("unused")
	fun deleteAllAsync() = CoroutineScope(Dispatchers.IO).launch { DeleteQuery(dataSource.connection, this@Table) }
}