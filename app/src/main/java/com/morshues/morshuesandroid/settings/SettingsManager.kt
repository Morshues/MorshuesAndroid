package com.morshues.morshuesandroid.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    fun getServerPath(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[SERVER_PATH_KEY] ?: DEFAULT_SERVER_PATH
        }
    }

    suspend fun setServerPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_PATH_KEY] = path
        }
    }

    fun getRootUrlList(): Flow<List<String>> {
        return context.dataStore.data.map { preferences ->
            val urlSet = preferences[ROOT_URL_LIST_KEY] ?: DEFAULT_ROOT_URL_SET
            urlSet.toList().sorted()
        }
    }

    suspend fun addRootUrl(url: String) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[ROOT_URL_LIST_KEY] ?: DEFAULT_ROOT_URL_SET
            preferences[ROOT_URL_LIST_KEY] = currentSet + url

            if (currentSet.isEmpty()) {
                preferences[SERVER_PATH_KEY] = url
            }
        }
    }

    suspend fun removeRootUrl(url: String) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[ROOT_URL_LIST_KEY] ?: DEFAULT_ROOT_URL_SET
            preferences[ROOT_URL_LIST_KEY] = currentSet - url

            if (url == preferences[SERVER_PATH_KEY]) {
                preferences[SERVER_PATH_KEY] =
                    preferences[ROOT_URL_LIST_KEY]?.firstOrNull() ?: DEFAULT_SERVER_PATH
            }
        }
    }

    companion object {
        const val DEFAULT_SERVER_PATH = "http://192.168.2.2:3000/"
        private val DEFAULT_ROOT_URL_SET = setOf(DEFAULT_SERVER_PATH)
        private val SERVER_PATH_KEY = stringPreferencesKey("server_path")
        private val ROOT_URL_LIST_KEY = stringSetPreferencesKey("root_url_list")
    }
}
