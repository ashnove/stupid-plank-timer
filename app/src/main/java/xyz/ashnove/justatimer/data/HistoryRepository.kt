package xyz.ashnove.justatimer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "history")

class HistoryRepository(private val context: Context) {

    private val historyKey = stringPreferencesKey("plank_history")

    val history: Flow<List<Pair<Long, Long>>> = context.dataStore.data
        .map { preferences ->
            preferences[historyKey]?.split(",")?.filter { it.isNotBlank() }?.mapNotNull {
                val parts = it.split(";")
                if (parts.size == 2) {
                    parts[0].toLongOrNull() to parts[1].toLongOrNull()
                } else {
                    null
                }
            }?.mapNotNull { (duration, timestamp) ->
                if (duration != null && timestamp != null) {
                    duration to timestamp
                } else {
                    null
                }
            } ?: emptyList()
        }

    suspend fun addHistory(duration: Long) {
        context.dataStore.edit { preferences ->
            val newRecord = "$duration;${System.currentTimeMillis()}"
            val currentHistory = preferences[historyKey]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            val newHistory = (listOf(newRecord) + currentHistory).take(5)
            preferences[historyKey] = newHistory.joinToString(",")
        }
    }

    suspend fun deleteHistory(timestamp: Long) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[historyKey]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            val newHistory = currentHistory.filterNot {
                it.split(";").getOrNull(1)?.toLongOrNull() == timestamp
            }
            preferences[historyKey] = newHistory.joinToString(",")
        }
    }
} 