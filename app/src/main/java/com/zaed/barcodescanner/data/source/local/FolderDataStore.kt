package com.zaed.barcodescanner.data.source.local

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface FolderDataStore {
    fun getFolderName(): Flow<String>
    suspend fun saveNewFolder(key: Preferences.Key<String>, folderName: String)

}