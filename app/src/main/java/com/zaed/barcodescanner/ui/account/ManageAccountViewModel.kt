package com.zaed.barcodescanner.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.barcodescanner.data.source.remote.DriveRemoteSource
import com.zaed.barcodescanner.data.source.remote.GoogleAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageAccountViewModel(
    private val driveSource: DriveRemoteSource,
    private val googleAuth: GoogleAuth
): ViewModel() {
    private val _uiState = MutableStateFlow(ManageAccountUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getSignedInAccount()
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
