package com.morshues.morshuesandroid.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.morshues.morshuesandroid.data.model.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "auth")

class SessionStore(private val context: Context) {

    val accessToken: Flow<String?> = context.dataStore.data.map { it[KEY_ACCESS] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH] }
    val user: Flow<UserDto?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER]?.let { Json.decodeFromString<UserDto>(it) }
    }

    suspend fun getOrCreateDeviceId(): String {
        val existingId = context.dataStore.data.map { it[KEY_DEVICE_ID] }.first()
        return if (existingId != null) {
            existingId
        } else {
            val newId = UUID.randomUUID().toString()
            context.dataStore.edit { prefs ->
                prefs[KEY_DEVICE_ID] = newId
            }
            newId
        }
    }

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = access
            prefs[KEY_REFRESH] = refresh
        }
    }

    suspend fun saveUser(user: UserDto) {
        val userJson = Json.encodeToString(user)
        context.dataStore.edit { prefs ->
            prefs[KEY_USER] = userJson
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        private val KEY_ACCESS = stringPreferencesKey("access")
        private val KEY_REFRESH = stringPreferencesKey("refresh")
        private val KEY_USER = stringPreferencesKey("user")
        private val KEY_DEVICE_ID = stringPreferencesKey("device_id")
    }
}
