package dev.peopo.skuerrel

import com.zaxxer.hikari.HikariDataSource
import dev.peopo.skuerrel.annotation.Table
import dev.peopo.skuerrel.data.SQLPairList
import dev.peopo.skuerrel.query.row.DeleteQuery
import dev.peopo.skuerrel.query.row.InsertQuery
import dev.peopo.skuerrel.query.row.SelectQuery
import dev.peopo.skuerrel.query.row.UpdateQuery
import dev.peopo.skuerrel.query.table.CreateTableQuery
import dev.peopo.skuerrel.query.table.DropTableQuery
import dev.peopo.skuerrel.reflection.DataSerializer
import dev.peopo.skuerrel.reflection.TableSerializer
import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


class Table<T: Any> (val dataSource: HikariDataSource, kClass : KClass<out T>) {

	internal val columns = TableSerializer.serialize(kClass)
	internal val name : String = kClass.findAnnotation<Table>()?.name ?: throw IllegalArgumentException("Class must have @Table annotation")


	@Suppress("unused")
	suspend fun fetchExistsAsync() = withContext(Dispatchers.IO) {
		var connection: Connection? = null
		var result : ResultSet? = null
		try {
			connection = dataSource.connection
			val meta = connection.metaData
			result = meta.getTables(null, null, name, null)
			val exist = result!!.next()
			connection.commit()
			return@withContext exist
		} catch (e: SQLException) {
			connection?.rollback()
			e.printStackTrace()
			return@withContext false
		} finally {
			result?.close()
			connection?.close()
		}
	}

	@Suppress("unused")
	suspend fun create(@Suppress("unused_parameter")ifNotExist : Boolean = true) = withContext(Dispatchers.IO) {
		CreateTableQuery(dataSource.connection, this@Table, true).execute()
	}

	@Suppress("unused")
	suspend fun drop() = withContext(Dispatchers.IO) {
		DropTableQuery(dataSource.connection, this@Table).execute()
	}

	@Suppress("unused")
	suspend fun insert(serializable: T) = withContext(Dispatchers.IO) {
		InsertQuery(dataSource.connection, this@Table, DataSerializer.serialize(serializable)).execute()
	}

	@Suppress("unused")
	suspend fun update(serializable: T, where: SQLPairList) = withContext(Dispatchers.IO) {
		UpdateQuery(dataSource.connection, this@Table, DataSerializer.serialize(serializable), where).execute()
	}

	@Suppress("unused")
	suspend fun update(set: SQLPairList, where: SQLPairList) = withContext(Dispatchers.IO) {
		UpdateQuery(dataSource.connection, this@Table, set, where)
	}

	@Suppress("unused")
	suspend inline fun <reified K: Any>fetch(where: SQLPairList): List<K> = withContext(Dispatchers.IO){
		val result = SelectQuery(dataSource.connection, this@Table, where).execute()
		return@withContext result.map { DataSerializer.deserialize<K>(it) }
	}

	@Suppress("unused")
	suspend inline fun <reified K: Any> fetch(where: String, values: List<Any?>) = withContext(Dispatchers.IO) {
		val result = SelectQuery(dataSource.connection, this@Table, where, values).execute()
		return@withContext result.map { DataSerializer.deserialize<K>(it) }
	}

	@Suppress("unused")
	suspend inline fun <reified K: Any> fetchAll() = withContext(Dispatchers.IO) {
		val result = SelectQuery(dataSource.connection, this@Table).execute()
		return@withContext result.map { DataSerializer.deserialize<K>(it) }
	}

	@Suppress("unused")
	suspend fun delete(where: SQLPairList) = withContext(Dispatchers.IO) {
		DeleteQuery(dataSource.connection, this@Table, where).execute()
	}

	@Suppress("unused")
	suspend fun deleteAll() = withContext(Dispatchers.IO) {
		DeleteQuery(dataSource.connection, this@Table).execute()
	}
}