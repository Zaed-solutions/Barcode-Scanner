package com.zaed.barcodescanner.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.zaed.barcodescanner.ui.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("MainFolder")

class FolderDataStoreImpl(val context: Context) : FolderDataStore {

    override fun getFolderName(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[Constants.Folder_KEY] ?: ""
        }
    }

    override suspend fun saveNewFolder(key: Preferences.Key<String>, folderName: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = folderName
        }
    }
}