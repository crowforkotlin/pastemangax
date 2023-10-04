@file:Suppress("unused")

package com.crow.base.tools.extensions

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crow.base.R
import com.crow.base.app.app
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/extensions
 * @Time: 2022/6/26 12:35
 * @Author: CrowForKotlin
 * @Description: DataStoreExt
 * @formatter:on
 **************************/

object DataStoreAgent {

    val APP_CONFIG = stringPreferencesKey("app.config")
    val USER_CONFIG = stringPreferencesKey("user.config")
    val DATA_USER = stringPreferencesKey("data.user")
    val DATA_BOOK = stringPreferencesKey("data.book")
}

object SpNameSpace {

    const val CATALOG_NIGHT_MODE = "Catalog.NightMode"

    object Key { const val ENABLE_DARK = "enable_dark" }
}

object DBNameSpace {
    const val app = "preference.chapter.db"
}

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(app.getString(R.string.BaseAppName))
val Context.appConfigDataStore: DataStore<Preferences> by preferencesDataStore(
    app.getString(
        R.string.BaseAppName
    ).plus(DataStoreAgent.APP_CONFIG.name)
)
val Context.appBookDataStore: DataStore<Preferences> by preferencesDataStore(
    app.getString(R.string.BaseAppName).plus(DataStoreAgent.DATA_BOOK.name)
)

suspend fun DataStore<Preferences>.getIntData(name: String) =
    data.map { preferences -> preferences[intPreferencesKey(name)] ?: 0 }.first()

suspend fun DataStore<Preferences>.getLonganData(name: String) =
    data.map { preferences -> preferences[longPreferencesKey(name)] ?: 0L }.first()

suspend fun DataStore<Preferences>.getFloatData(name: String) =
    data.map { preferences -> preferences[floatPreferencesKey(name)] ?: 0.0f }.first()

suspend fun DataStore<Preferences>.getDoubleData(name: String) =
    data.map { preferences -> preferences[doublePreferencesKey(name)] ?: 0.00 }.first()

suspend fun DataStore<Preferences>.getBooleanData(name: String) =
    data.map { preferences -> preferences[booleanPreferencesKey(name)] ?: false }.first()

suspend fun DataStore<Preferences>.getStringData(name: String) =
    data.map { preferences -> preferences[stringPreferencesKey(name)] ?: "" }.first()

suspend fun DataStore<Preferences>.getStringSetData(name: String) =
    data.map { preferences -> preferences[stringSetPreferencesKey(name)] ?: emptySet() }.first()

suspend fun <T> DataStore<Preferences>.asyncEncode(key: Preferences.Key<T>, value: T) {
    edit { it[key] = value }
}

suspend fun <T> DataStore<Preferences>.asyncDecode(preferencesKey: Preferences.Key<T>): T? {
    return data.map { it[preferencesKey] }.first()
}

fun <T> DataStore<Preferences>.decode(key: Preferences.Key<T>): T? {
    return runBlocking {
        data.map { it[key] }.first()
    }
}

/*
* @Description: DataStore 扩展
* @author: lei
*/
fun <T> DataStore<Preferences>.encode(key: Preferences.Key<T>, value: T) {
    runBlocking { edit { it[key] = value } }
}

suspend fun <T> Preferences.Key<T>.asyncEncode(value: T) {
    app.appDataStore.edit { it[this] = value }
}

suspend fun <T> Preferences.Key<T>.asyncClear() {
    app.appDataStore.edit { it.clear() }
}

suspend fun <T> Preferences.Key<T>.asyncDecode(): T? {
    return app.appDataStore.data.map { it[this] }.firstOrNull()
}

fun <T> Preferences.Key<T>.encode(value: T) {
    runBlocking { app.appDataStore.edit { it[this@encode] = value } }
}

fun <T> Preferences.Key<T>.decode(): T? {
    return runBlocking { app.appDataStore.data.map { it[this@decode] }.firstOrNull() }
}

fun <T> Preferences.Key<T>.clear() {
    runBlocking { app.appDataStore.edit { it.clear() } }
}

fun String.getSharedPreferences(): SharedPreferences {
    return app.getSharedPreferences(this, Context.MODE_PRIVATE)
}

inline fun <reified T : RoomDatabase> buildDatabase(dbName: String): T {
    return Room.databaseBuilder(app, T::class.java, dbName).fallbackToDestructiveMigration().build()
}