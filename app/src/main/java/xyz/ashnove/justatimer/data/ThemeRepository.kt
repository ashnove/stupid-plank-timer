package xyz.ashnove.justatimer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemeRepository(private val context: Context) {

    private val themeKey = stringPreferencesKey("app_theme")

    val theme: Flow<String> = context.themeDataStore.data
        .map { preferences ->
            preferences[themeKey] ?: "Black" // Default theme
        }

    suspend fun setTheme(themeName: String) {
        context.themeDataStore.edit { preferences ->
            preferences[themeKey] = themeName
        }
    }
} 