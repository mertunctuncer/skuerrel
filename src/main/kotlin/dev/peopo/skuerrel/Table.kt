package dev.peopo.skuerrel

import dev.peopo.skuerrel.annotation.Table
import dev.peopo.skuerrel.connection.ConnectionProvider
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

@Suppress("unused")
class Table<T: Any> (val dataSource: ConnectionProvider, kClass : KClass<out T>) {

	internal val columns = TableSerializer.serialize(kClass)
	internal val name : String = kClass.findAnnotation<Table>()?.name ?: throw IllegalArgumentException("Class must have @Table annotation")

	suspend fun fetchExists() = withContext(Dispatchers.IO) {
		var connection: Connection? = null
		var result : ResultSet? = null
		try {
			connection = dataSource.getConnection()
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

	fun fetchExistsAsync() = CoroutineScope(Dispatchers.IO).async {
		var connection: Connection? = null
		var result : ResultSet? = null
		try {
			connection = dataSource.getConnection()
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
	}

	suspend fun create(@Suppress("unused_parameter")ifNotExist : Boolean = true) = withContext(Dispatchers.IO) {
		CreateTableQuery(dataSource.getConnection(), this@Table, true).execute()
	}

	fun createAsync(@Suppress("unused_parameter")ifNotExist : Boolean = true) = CoroutineScope(Dispatchers.IO).launch {
		CreateTableQuery(dataSource.getConnection(), this@Table, true).execute()
	}

	suspend fun drop() = withContext(Dispatchers.IO) {
		DropTableQuery(dataSource.getConnection(), this@Table).execute()
	}

	fun dropAsync() = CoroutineScope(Dispatchers.IO).launch {
		DropTableQuery(dataSource.getConnection(), this@Table).execute()
	}

	suspend fun insert(serializable: T) = withContext(Dispatchers.IO) {
		InsertQuery(dataSource.getConnection(), this@Table, DataSerializer.serialize(serializable)).execute()
	}

	fun insertAsync(serializable: T) = CoroutineScope(Dispatchers.IO).launch {
		InsertQuery(dataSource.getConnection(), this@Table, DataSerializer.serialize(serializable)).execute()
	}

	suspend fun update(serializable: T, where: SQLPairList) = withContext(Dispatchers.IO) {
		UpdateQuery(dataSource.getConnection(), this@Table, DataSerializer.serialize(serializable), where).execute()
	}

	fun updateAsync(serializable: T, where: SQLPairList) = CoroutineScope(Dispatchers.IO).launch {
		UpdateQuery(dataSource.getConnection(), this@Table, DataSerializer.serialize(serializable), where).execute()
	}

	suspend fun update(set: SQLPairList, where: SQLPairList) = withContext(Dispatchers.IO) {
		UpdateQuery(dataSource.getConnection(), this@Table, set, where)
	}

	fun updateAsync(set: SQLPairList, where: SQLPairList) = CoroutineScope(Dispatchers.IO).launch {
		UpdateQuery(dataSource.getConnection(), this@Table, set, where)
	}

	suspend inline fun <reified K: Any>fetch(where: SQLPairList): List<K> = withContext(Dispatchers.IO){
		val result = SelectQuery(dataSource.getConnection(), this@Table, where).execute()
		return@withContext result.map { DataSerializer.deserialize<K>(it) }
	}

	inline fun <reified K: Any>fetchAsync(where: SQLPairList) = CoroutineScope(Dispatchers.IO).async {
		val result = SelectQuery(dataSource.getConnection(), this@Table, where).execute()
		return@async result.map { DataSerializer.deserialize<K>(it) }
	}

	suspend inline fun <reified K: Any> fetch(where: String, values: List<Any?>) = withContext(Dispatchers.IO) {
		val result = SelectQuery(dataSource.getConnection(), this@Table, where, values).execute()
		return@withContext result.map { DataSerializer.deserialize<K>(it) }
	}

	inline fun <reified K: Any> fetchAsync(where: String, values: List<Any?>) = CoroutineScope(Dispatchers.IO).async {
		val result = SelectQuery(dataSource.getConnection(), this@Table, where, values).execute()
		return@async result.map { DataSerializer.deserialize<K>(it) }
	}

	suspend inline fun <reified K: Any> fetchAll() = withContext(Dispatchers.IO) {
		val result = SelectQuery(dataSource.getConnection(), this@Table).execute()
		return@withContext result.map { DataSerializer.deserialize<K>(it) }
	}

	inline fun <reified K: Any> fetchAllAsync() = CoroutineScope(Dispatchers.IO).async {
		val result = SelectQuery(dataSource.getConnection(), this@Table).execute()
		return@async result.map { DataSerializer.deserialize<K>(it) }
	}

	suspend fun delete(where: SQLPairList) = withContext(Dispatchers.IO) {
		DeleteQuery(dataSource.getConnection(), this@Table, where).execute()
	}

	fun deleteAsync(where: SQLPairList) = CoroutineScope(Dispatchers.IO).launch {
		DeleteQuery(dataSource.getConnection(), this@Table, where).execute()
	}

	suspend fun deleteAll() = withContext(Dispatchers.IO) {
		DeleteQuery(dataSource.getConnection(), this@Table).execute()
	}

	fun deleteAllAsync() = CoroutineScope(Dispatchers.IO).launch {
		DeleteQuery(dataSource.getConnection(), this@Table).execute()
	}
}