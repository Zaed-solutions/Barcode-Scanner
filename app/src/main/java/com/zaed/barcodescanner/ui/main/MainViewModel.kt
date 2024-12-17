package com.zaed.barcodescanner.ui.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.barcodescanner.data.models.ProductImage
import com.zaed.barcodescanner.data.models.ProductsFolder
import com.zaed.barcodescanner.data.source.remote.DriveRemoteSource
import com.zaed.barcodescanner.data.source.remote.GoogleAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val driveRemoteSource: DriveRemoteSource,
    private val googleAuth: GoogleAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    fun handleAction(action: MainUiAction) {
        when (action) {
            is MainUiAction.OnDeleteProductsFolder -> {
                deleteProductsFolder(action.folderName)
            }

            is MainUiAction.OnAddNewFolder -> {
                addNewFolder(action.folderName)
            }

            is MainUiAction.OnAddNewProductImage -> {
                addNewProductImage(action.folderName, action.uri)
            }

            is MainUiAction.OnDeleteProductImage -> deleteProductImage(
                action.folderName,
                action.imageUri
            )

            MainUiAction.OnUploadFolders -> uploadFolders()
            else -> Unit
        }
    }

    private fun deleteProductImage(folderName: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(
                    folders = oldState.folders.map { folder ->
                        if (folder.name == folderName) {
                            folder.copy(images = folder.images.filter { it.uri != imageUri })
                        } else {
                            folder
                        }
                    }
                )
            }
        }
    }
    //TODO BY ZAREA
    private fun uploadFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            googleAuth.getSignedInAccount().collect {
                it.onSuccess { account ->
                    Log.d("UPLOAD_SUCCESS", "${account.email}")
                    uiState.value.folders.forEach { folder ->
                        val folderId = driveRemoteSource.createFolder(account, folder.name)
                        folder.images.forEach { image ->
                            driveRemoteSource.uploadFileToSpecificFolder(
                                account = account,
                                fileUri = image.uri,
                                mimeType = "image/jpeg",
                                fileName = image.uri.toString().substringAfterLast("/"),
                                folderId = folderId
                            ).collect{result->
                                result.onSuccess {data->
                                    _uiState.update {
                                        it.copy(progress = data)
                                    }
                                    Log.d("UPLOAD_SUCCESS", "uploadFolders: $data")
                                }
                                result.onFailure {
                                    Log.d("UPLOAD_SUCCESS", "uploadFolders: ${it.message}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addNewProductImage(folderName: String, uri: Uri) {
        viewModelScope.launch {
            val image = ProductImage(uri)
            _uiState.update { oldState ->
                oldState.copy(
                    folders = oldState.folders.map { folder ->
                        if (folder.name == folderName) {
                            folder.copy(images = folder.images + image)
                        } else {
                            folder
                        }
                    }
                )
            }
        }
    }

    private fun addNewFolder(folderName: String) {
        viewModelScope.launch {
            val folders = uiState.value.folders.toMutableList()
            folders.add(ProductsFolder(folderName))
            _uiState.update {
                it.copy(folders = folders)
            }
        }
    }

    private fun deleteProductsFolder(folderName: String) {
        viewModelScope.launch {
            val folders = uiState.value.folders.toMutableList()
            folders.removeIf { it.name == folderName }
            _uiState.update {
                it.copy(folders = folders)
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            googleAuth.signOut()
        }
    }

    fun getAllFiles() {
        Log.d("UPLOAD_SUCCESS", "getAllFiles:0 ")
        viewModelScope.launch(Dispatchers.IO) {
            googleAuth.getSignedInAccount().collect {
                it.onSuccess { account ->
                    Log.d("UPLOAD_SUCCESS", "${account.displayName} ")
                    driveRemoteSource.getAllFiles(account)
                }
            }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            googleAuth.getSignedInAccount().collect {
                it.onSuccess { account ->
                    Log.d("UPLOAD_SUCCESS", "${account.email} ")
                    driveRemoteSource.createFolder(account, folderName)
                }
            }
        }
    }

//
}