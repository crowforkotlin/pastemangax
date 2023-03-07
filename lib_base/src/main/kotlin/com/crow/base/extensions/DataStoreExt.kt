package com.crow.base.extensions

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/extensions
 * @Time: 2022/6/26 12:35
 * @Author: BarryAllen
 * @Description: DataStoreExt
 * @formatter:on
 **************************/

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

suspend fun <T> DataStore<Preferences>.setData(key: Preferences.Key<T>, value: T) {
    edit { it[key] = value }
}