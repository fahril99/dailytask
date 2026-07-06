package com.example.dailyreminder.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reminder_prefs")

class DataStoreManager(private val context: Context) {
    companion object {
        val COMPLETED_TASKS_KEY = stringSetPreferencesKey("completed_tasks")
        val LAST_DATE_KEY = stringPreferencesKey("last_date")
        val SCHEDULE_TEXT_KEY = stringPreferencesKey("schedule_text")
        val HISTORY_KEY = stringPreferencesKey("history")
        val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        val STAGED_REMINDERS_KEY = booleanPreferencesKey("staged_reminders")
        val CUSTOM_SOUND_URI_KEY = stringPreferencesKey("custom_sound_uri")

    }

    // --- Existing flows ---

    val completedTasks: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[COMPLETED_TASKS_KEY] ?: emptySet()
    }

    val lastDate: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LAST_DATE_KEY]
    }

    val scheduleText: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SCHEDULE_TEXT_KEY] ?: ""
    }

    // --- History ---

    val historyJson: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[HISTORY_KEY] ?: "{}"
    }

    // --- Settings ---

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SOUND_ENABLED_KEY] ?: true
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[VIBRATION_ENABLED_KEY] ?: true
    }

    val stagedRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[STAGED_REMINDERS_KEY] ?: true
    }

    val customSoundUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_SOUND_URI_KEY]
    }

    // --- Task completion ---

    suspend fun setTaskCompleted(taskId: String, isCompleted: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[COMPLETED_TASKS_KEY]?.toMutableSet() ?: mutableSetOf()
            if (isCompleted) {
                current.add(taskId)
            } else {
                current.remove(taskId)
            }
            prefs[COMPLETED_TASKS_KEY] = current
        }
    }

    suspend fun clearCompletedTasks() {
        context.dataStore.edit { prefs ->
            prefs[COMPLETED_TASKS_KEY] = emptySet()
        }
    }

    suspend fun setLastDate(dateString: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_DATE_KEY] = dateString
        }
    }

    suspend fun setScheduleText(text: String) {
        context.dataStore.edit { prefs ->
            prefs[SCHEDULE_TEXT_KEY] = text
        }
    }

    // --- History management ---

    suspend fun addToHistory(taskId: String, dateStr: String) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[HISTORY_KEY] ?: "{}"
            val json = JSONObject(jsonStr)
            val dayArray = json.optJSONArray(dateStr) ?: JSONArray()
            var found = false
            for (i in 0 until dayArray.length()) {
                if (dayArray.getString(i) == taskId) {
                    found = true
                    break
                }
            }
            if (!found) {
                dayArray.put(taskId)
            }
            json.put(dateStr, dayArray)
            prefs[HISTORY_KEY] = json.toString()
        }
    }

    suspend fun getHistoryMap(): Map<String, Set<String>> {
        val jsonStr = context.dataStore.data.first()[HISTORY_KEY] ?: "{}"
        return parseHistoryJson(jsonStr)
    }

    // --- Settings updates ---

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[SOUND_ENABLED_KEY] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[VIBRATION_ENABLED_KEY] = enabled }
    }

    suspend fun setStagedRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[STAGED_REMINDERS_KEY] = enabled
        }
    }

    suspend fun setCustomSoundUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) {
                prefs.remove(CUSTOM_SOUND_URI_KEY)
            } else {
                prefs[CUSTOM_SOUND_URI_KEY] = uri
            }
        }
    }
}

fun parseHistoryJson(jsonStr: String): Map<String, Set<String>> {
    val result = mutableMapOf<String, Set<String>>()
    try {
        val json = JSONObject(jsonStr)
        val keys = json.keys()
        while (keys.hasNext()) {
            val dateKey = keys.next()
            val arr = json.optJSONArray(dateKey) ?: continue
            val ids = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                ids.add(arr.getString(i))
            }
            result[dateKey] = ids
        }
    } catch (_: Exception) { }
    return result
}
