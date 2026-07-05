package com.example.dailyreminder.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reminder_prefs")

class DataStoreManager(private val context: Context) {
    companion object {
        val COMPLETED_TASKS_KEY = stringSetPreferencesKey("completed_tasks")
        val LAST_DATE_KEY = stringPreferencesKey("last_date")
    }

    val completedTasks: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[COMPLETED_TASKS_KEY] ?: emptySet()
    }

    val lastDate: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LAST_DATE_KEY]
    }

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
}
