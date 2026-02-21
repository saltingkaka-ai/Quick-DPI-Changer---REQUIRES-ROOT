package com.dpi.changer.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dpi.changer.data.model.Preset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "presets")

object PresetDataStore {
    private lateinit var dataStore: DataStore<Preferences>
    private val gson = Gson()
    private val PRESETS_KEY = stringPreferencesKey("presets")

    fun initialize(context: Context) {
        dataStore = context.dataStore
    }

    val presets: Flow<List<Preset>> = dataStore.data.map { preferences ->
        val json = preferences[PRESETS_KEY] ?: "[]"
        val type = object : TypeToken<List<Preset>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun addPreset(preset: Preset) {
        dataStore.edit { preferences ->
            val currentList = getCurrentList(preferences)
            val newList = currentList + preset
            preferences[PRESETS_KEY] = gson.toJson(newList)
        }
    }

    suspend fun updatePreset(preset: Preset) {
        dataStore.edit { preferences ->
            val currentList = getCurrentList(preferences)
            val newList = currentList.map { if (it.id == preset.id) preset else it }
            preferences[PRESETS_KEY] = gson.toJson(newList)
        }
    }

    suspend fun deletePreset(presetId: String) {
        dataStore.edit { preferences ->
            val currentList = getCurrentList(preferences)
            val newList = currentList.filter { it.id != presetId }
            preferences[PRESETS_KEY] = gson.toJson(newList)
        }
    }

    suspend fun importPresets(presets: List<Preset>) {
        dataStore.edit { preferences ->
            preferences[PRESETS_KEY] = gson.toJson(presets)
        }
    }

    private fun getCurrentList(preferences: Preferences): List<Preset> {
        val json = preferences[PRESETS_KEY] ?: "[]"
        val type = object : TypeToken<List<Preset>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}