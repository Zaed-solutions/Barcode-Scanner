package com.zaed.barcodescanner.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.barcodescanner.data.source.local.FolderDataStore
import com.zaed.barcodescanner.data.source.remote.DriveRemoteSource
import com.zaed.barcodescanner.data.source.remote.GoogleAuth
import com.zaed.barcodescanner.ui.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageAccountViewModel(
    private val googleAuth: GoogleAuth,
    private val dataStore: FolderDataStore

): ViewModel() {
    private val _uiState = MutableStateFlow(ManageAccountUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getSignedInAccount()
        getMainFolder()
    }
    private fun getMainFolder() {
        viewModelScope.launch {
            dataStore.getFolderName().collect{ folderName ->
                _uiState.update {
                    it.copy(mainFolderName = folderName)
                }
            }
        }
    }
    fun changeMainFolderName(folderName: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(mainFolderName = folderName)
            }
        }
    }
    fun changeMainFolder() {
        viewModelScope.launch {
            dataStore.saveNewFolder(Constants.Folder_KEY,uiState.value.mainFolderName)
        }
    }



    fun getSignedInAccount() {
        viewModelScope.launch (Dispatchers.IO){
            googleAuth.getSignedInAccount().collect { result ->
                result.onSuccess { account ->
                    _uiState.value = _uiState.value.copy(
                        currentAccount = account
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            googleAuth.signOut()
            _uiState.value = _uiState.value.copy(
                currentAccount = null
            )
        }
    }
}
