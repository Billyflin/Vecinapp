package com.vecinapp

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val SHOW_NAVBAR_KEY = booleanPreferencesKey("show_navbar")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    }

    val preferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        val darkMode = preferences[DARK_MODE_KEY] ?: false
        val showNavBar = preferences[SHOW_NAVBAR_KEY] ?: true
        val dynamicColor =
            preferences[DYNAMIC_COLOR_KEY] ?: (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        UserPreferences(darkMode, dynamicColor, showNavBar)
    }

    suspend fun updateDarkMode(darkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = darkMode
        }
    }

    suspend fun updateShowNavBar(showNavBar: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_NAVBAR_KEY] = showNavBar
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
    val showNavBar: Boolean
)
