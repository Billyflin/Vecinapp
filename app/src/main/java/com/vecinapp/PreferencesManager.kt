package com.vecinapp

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    }

    val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            val darkMode = preferences[DARK_MODE_KEY] ?: false
            val dynamicColor = preferences[DYNAMIC_COLOR_KEY] ?: false
            UserPreferences(darkMode, dynamicColor)
        }

    suspend fun updateDarkMode(darkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = darkMode
        }
    }

    suspend fun updateDynamicColor(dynamicColor: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = dynamicColor
        }
    }
}

data class UserPreferences(
    val darkMode: Boolean,
    val dynamicColor: Boolean,
)
