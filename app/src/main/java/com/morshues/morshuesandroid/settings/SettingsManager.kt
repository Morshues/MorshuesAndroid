package com.morshues.morshuesandroid.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.morshues.morshuesandroid.BuildConfig
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

    companion object {
        private const val DEFAULT_SERVER_PATH = BuildConfig.BASE_URL
        private val SERVER_PATH_KEY = stringPreferencesKey("server_path")
    }
}
