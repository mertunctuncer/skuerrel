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

/**
 * A table representing a table in an SQL database
 *
 * @param T the serializable class matching the SQL table
 * @param kClass serializable class matching the SQL table
 * @param connProvider connection provider for the table
 * @constructor Serializes the given [KClass] into SQL column data. [create] or [createAsync] must be called to create the table in the database.
 */
@Suppress("unused")
class Table<T: Any> (val connProvider: ConnectionProvider, kClass : KClass<out T>) {

	internal val columns = TableSerializer.serialize(kClass)
	internal val name : String = kClass.findAnnotation<Table>()?.name ?: throw IllegalArgumentException("Class must have @Table annotation")

	init { connProvider.setCommit(false) }

	/**
	 * Suspends and fetches the table existence from the database with [Dispatchers.IO] context.
	 *
	 * @return *true* - if the table exists, *false* if not
	 */
	suspend fun fetchExists() = withContext(Dispatchers.IO) { fetchExistsBlocking() }

	/**
	 * Launches a new coroutine and fetches the table existence.
	 *
	 * @return A [Deferred]<[Boolean]> result, [Deferred.await] must be called to get the result when needed.
	 */
	fun fetchExistsAsync() = CoroutineScope(Dispatchers.IO).async { return@async fetchExistsBlocking() }

	/**
	 * Suspends and creates the table in the database with [Dispatchers.IO] context.
	 *
	 * @param ifNotExist if true, adds ```IF NOT EXISTS``` to the generated SQL syntax.
	 */
	suspend fun create(@Suppress("unused_parameter")ifNotExist : Boolean = true) = withContext(Dispatchers.IO) {
		CreateTableQuery(connProvider.getConnection(), this@Table, true).execute()
	}

	/**
	 * Launches a new coroutine using [Dispatchers.IO] and creates the table in the database.
	 *
	 * @param ifNotExist if true, adds ```IF NOT EXISTS``` to the generated SQL syntax.
	 */
	fun createAsync(@Suppress("unused_parameter")ifNotExist : Boolean = true) = CoroutineScope(Dispatchers.IO).launch {
		CreateTableQuery(connProvider.getConnection(), this@Table, true).execute()
	}

	/**
	 * Suspends and drops the table in the database with [Dispatchers.IO] context.
	 */
	suspend fun drop() = withContext(Dispatchers.IO) {
		DropTableQuery(connProvider.getConnection(), this@Table).execute()
	}

	/**
	 * Launches a new coroutine using [Dispatchers.IO] and drops the table in the database.
	 */
	fun dropAsync() = CoroutineScope(Dispatchers.IO).launch {
		DropTableQuery(connProvider.getConnection(), this@Table).execute()
	}

	/**
	 * Suspends and inserts the given [serializable] class with [Dispatchers.IO] context.
	 *
	 * @param serializable serializable instance to be inserted
	 */
	suspend fun insert(serializable: T) = withContext(Dispatchers.IO) {
		InsertQuery(connProvider.getConnection(), this@Table, DataSerializer.serialize(serializable)).execute()
	}

	/**
	 * Launches a new coroutine using [Dispatchers.IO] and inserts the given [serializable] class.
	 *
	 * @param serializable serializable instance to be inserted
	 */
	fun insertAsync(serializable: T) = CoroutineScope(Dispatchers.IO).launch {
		InsertQuery(connProvider.getConnection(), this@Table, DataSerializer.serialize(serializable)).execute()
	}

	/**
	 * Suspends and updates row(s) with the given [serializable] class with [Dispatchers.IO] context.
	 *
	 * @param serializable serializable instance that holds the new values
	 * @param where where pairs to be used while updating
	 */
	suspend fun update(serializable: T, where: SQLPairList) = withContext(Dispatchers.IO) {
		UpdateQuery(connProvider.getConnection(), this@Table, DataSerializer.serialize(serializable), where).execute()
	}

	/**
	 * Launches a new coroutine using [Dispatchers.IO] and updates row(s) with the given [serializable] class.
	 *
	 * @param serializable serializable instance that holds the new values
	 * @param where where pairs to be used while updating
	 */
	fun updateAsync(serializable: T, where: SQLPairList) = CoroutineScope(Dispatchers.IO).launch {
		UpdateQuery(connProvider.getConnection(), this@Table, DataSerializer.serialize(serializable), where).execute()
	}

	/**
	 * Suspends and updates row(s) with the given [set] and [where] with [Dispatchers.IO] context.
	 *
	 * @param set list that holds the new key - value pairs
	 * @param where where pairs to be used while updating
	 */
	suspend fun update(set: SQLPairList, where: SQLPairList) = withContext(Dispatchers.IO) {
		UpdateQuery(connProvider.getConnection(), this@Table, set, where)
	}

	/**
	 * Launches a new coroutine using [Dispatchers.IO] and updates row(s) with the given [set] and [where] list.
	 *
	 * @param set list that holds the new key - value pairs
	 * @param where where pairs to be used while updating
	 */
	fun updateAsync(set: SQLPairList, where: SQLPairList) = CoroutineScope(Dispatchers.IO).launch {
		UpdateQuery(connProvider.getConnection(), this@Table, set, where)
	}

	/**
	 * Suspends and fetches the rows that fit the given [where] with [Dispatchers.IO] context.
	 *
	 * @param where where pairs to be used while fetching
	 * @param K serializable class to deserialize into.
	 * @return deserialized result.
	 */
	suspend inline fun <reified K: Any>fetch(where: SQLPairList): List<K> = withContext(Dispatchers.IO){
		val result = SelectQuery(connProvider.getConnection(), this@Table, where).execute()
		return@withContext result.map { DataSerializer.deserialize<K>(it) }
	}

	/**
	 * Launches a new coroutine using [Dispatchers.IO] and fetches rows with the given [where] pairs.
	 *
	 * @param where where pairs to be used while fetching
	 * @param K serializable class to deserialize into.
	 * @return A [List]<[Deferred]<[K]>> result, [Deferred.await] must be called to get the result when needed.
	 */
	inline fun <reified K: Any>fetchAsync(where: SQLPairList) = CoroutineScope(Dispatchers.IO).async {
		val result = SelectQuery(connProvider.getConnection(), this@Table, where).execute()
		return@async result.map { DataSerializer.deserialize<K>(it) }
	}

	/**
	 * Suspends and fetches the rows that fit the given [where] query and [values] list with [Dispatchers.IO] context.
	 *
	 * @param where where query to be used while fetching
	 * @param values where values to be used while fetching
	 * @param K serializable class to deserialize into.
	 * @return deserialized result.
	 */
	suspend inline fun <reified K: Any> fetch(where: String, values: List<Any?>) = withContext(Dispatchers.IO) {
		val result = SelectQuery(connProvider.getConnection(), this@Table, where, values).execute()
		return@withContext result.map { DataSerializer.deserialize<K>(it) }
	}

	/**
	 * Launches a new coroutine using [Dispatchers.IO] and fetches rows with the given [where] query and [values] list
	 *
	 * @param where where query to be used while fetching
	 * @param values where values to be used while fetching
	 * @param K serializable class to deserialize into.
	 * @return A [List]<[Deferred]<[K]>> result, [Deferred.await] must be called to get the result when needed.
	 */
	inline fun <reified K: Any> fetchAsync(where: String, values: List<Any?>) = CoroutineScope(Dispatchers.IO).async {
		val result = SelectQuery(connProvider.getConnection(), this@Table, where, values).execute()
		return@async result.map { DataSerializer.deserialize<K>(it) }
	}

	/**
	 * Suspends and fetches all rows with [Dispatchers.IO] context.
	 *
	 * @param K serializable class to deserialize into.
	 * @return deserialized result.
	 */
	suspend inline fun <reified K: Any> fetchAll() = withContext(Dispatchers.IO) {
		val result = SelectQuery(connProvider.getConnection(), this@Table).execute()
		return@withContext result.map { DataSerializer.deserialize<K>(it) }
	}

	/**
	 * Launches a new coroutine and fetches all rows with [Dispatchers.IO] context.
	 *
	 * @param K serializable class to deserialize into.
	 * @return A [List]<[Deferred]<[K]>> result, [Deferred.await] must be called to get the result when needed.
	 */
	inline fun <reified K: Any> fetchAllAsync() = CoroutineScope(Dispatchers.IO).async {
		val result = SelectQuery(connProvider.getConnection(), this@Table).execute()
		return@async result.map { DataSerializer.deserialize<K>(it) }
	}

	/**
	 * Suspends and deletes the rows matching the [where] pair list with [Dispatchers.IO] context.
	 *
	 * @param where pair list to match while deleting.
	 */
	suspend fun delete(where: SQLPairList) = withContext(Dispatchers.IO) {
		DeleteQuery(connProvider.getConnection(), this@Table, where).execute()
	}

	/**
	 * Launches a new coroutine using [Dispatchers.IO] and deletes the rows matching the [where] pair list.
	 *
	 * @param where pair list to match while deleting.
	 */
	fun deleteAsync(where: SQLPairList) = CoroutineScope(Dispatchers.IO).launch {
		DeleteQuery(connProvider.getConnection(), this@Table, where).execute()
	}

	/**
	 * Suspends and deletes the all rows with [Dispatchers.IO] context.
	 */
	suspend fun deleteAll() = withContext(Dispatchers.IO) {
		DeleteQuery(connProvider.getConnection(), this@Table).execute()
	}

	/**
	 * Launches a new coroutine using [Dispatchers.IO] and deletes all rows.
	 */
	fun deleteAllAsync() = CoroutineScope(Dispatchers.IO).launch {
		DeleteQuery(connProvider.getConnection(), this@Table).execute()
	}

	private fun fetchExistsBlocking() : Boolean {
		var connection: Connection? = null
		var result : ResultSet? = null
		return try {
			connection = connProvider.getConnection()
			val meta = connection.metaData
			result = meta.getTables(null, null, name, null)
			val exist = result!!.next()
			connection.commit()
			exist
		} catch (e: SQLException) {
			connection?.rollback()
			e.printStackTrace()
			false
		} finally {
			result?.close()
			connection?.close()
		}
	}
}