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
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val IS_SENIOR_KEY = booleanPreferencesKey("is_senior")
        val IS_FIRST_TIME_KEY = booleanPreferencesKey("is_first_time")
    }

    val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            val darkMode = preferences[DARK_MODE_KEY] ?: false
            val dynamicColor =
                preferences[DYNAMIC_COLOR_KEY] ?: (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            val isSenior = preferences[IS_SENIOR_KEY] ?: false
            val isFirstTime = preferences[IS_FIRST_TIME_KEY] ?: true
            UserPreferences(darkMode, dynamicColor, isSenior, isFirstTime)
        }

    suspend fun updateIsFirstTime(isFirstTime: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_TIME_KEY] = isFirstTime
        }
    }

    suspend fun updateIsSenior(isSenior: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_SENIOR_KEY] = isSenior
        }
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
    val isSenior: Boolean,
    val isFirstTime: Boolean

)
