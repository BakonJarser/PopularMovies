package com.cellblock70.popularmovies.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

val Context.tabDataStore: DataStore<Preferences> by preferencesDataStore(name = "tab_preferences")

object TabPreferences {
    private val SELECTED_TAB_KEY = stringPreferencesKey("selected_tab")

    fun getSelectedTab(context: Context): Flow<String?> {
        return context.tabDataStore.data
            .map { prefs -> prefs[SELECTED_TAB_KEY] }
    }

    suspend fun saveSelectedTab(context: Context, selectedTab: String) {
        context.tabDataStore.edit { prefs ->
            prefs[SELECTED_TAB_KEY] = selectedTab
        }
    }
}
