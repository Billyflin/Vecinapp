package com.vecinapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    private val context: Context
) {

    // Claves para las preferencias
    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    private val FONT_SIZE = stringPreferencesKey("font_size")
    private val SELECTED_COMMUNITY_ID = stringPreferencesKey("selected_community_id")
    private val SENIOR_MODE = booleanPreferencesKey("senior_mode")

    // Flujos para observar las preferencias
    val darkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DARK_MODE] ?: false }

    val dynamicColorsFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DYNAMIC_COLORS] ?: true }

    val fontSizeFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[FONT_SIZE] ?: "large" }

    val selectedCommunityIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[SELECTED_COMMUNITY_ID] }

    val seniorModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[SENIOR_MODE] ?: false }


    // Funciones para actualizar las preferencias

    suspend fun setSeniorMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SENIOR_MODE] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLORS] = enabled
        }
    }

    suspend fun setFontSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    suspend fun setSelectedCommunityId(communityId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_COMMUNITY_ID] = communityId
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(SELECTED_COMMUNITY_ID)
        }
    }
}
