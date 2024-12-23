package com.zaed.barcodescanner.ui.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.barcodescanner.data.models.ProductImage
import com.zaed.barcodescanner.data.models.ProductsFolder
import com.zaed.barcodescanner.data.source.local.FolderDataStore
import com.zaed.barcodescanner.data.source.remote.DriveRemoteSource
import com.zaed.barcodescanner.data.source.remote.GoogleAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val driveRemoteSource: DriveRemoteSource,
    private val googleAuth: GoogleAuth,
    private val folderDataStore: FolderDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
init {
    getCurrentAccount()
    getMainFolderName()
}

    private fun getMainFolderName() {
        viewModelScope.launch {
            folderDataStore.getFolderName().collect { folderName ->
                _uiState.update {
                    it.copy(mainFolderName = folderName)
                }
            }
        }
    }

    private fun getCurrentAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            googleAuth.getSignedInAccount().collect { result ->
                result.onSuccess { account ->
                    _uiState.update { it.copy(needToLogin = false) }
                }.onFailure {
                    _uiState.update { it.copy(needToLogin = true) }
                }
            }
        }
    }

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
            MainUiAction.OnDeleteAllFoldersClicked -> onDeleteAllFoldersClicked()
            MainUiAction.OnDeleteAllFolders -> deleteAllFolders()

            MainUiAction.OnUploadFolders -> uploadFolders()
            MainUiAction.OnSignOut -> signOut()
            is MainUiAction.OnUploadFolder -> uploadFolder(action.folderName)
            else -> Unit
        }
    }

    private fun deleteAllFolders() {
        viewModelScope.launch(Dispatchers.IO){
            _uiState.update {
                it.copy(
                    folders = emptyList()
                )
            }
        }
    }

    private fun onDeleteAllFoldersClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            val folders = uiState.value.folders
            val hasUnUploadedFolders = folders.any { folder ->
                folder.images.isNotEmpty() && folder.images.any { !it.isUploaded }
            }

            if (hasUnUploadedFolders) {
                _uiState.update {
                    it.copy(thereIsFoldersNotUploadedYet = true)
                }
            } else {
                deleteAllFolders()
            }
        }
    }
    fun resetThereIsFoldersNotUploadedYet(){
        _uiState.update {
            it.copy(
                thereIsFoldersNotUploadedYet = false
            )
        }
    }

    private fun uploadFolder(folderName: String) {
        val folder = uiState.value.folders.find { it.name == folderName }
        if (folder != null) {
            viewModelScope.launch(Dispatchers.IO) {
                googleAuth.getSignedInAccount().collect { result ->
                    result.onSuccess { account ->
                        _uiState.update { it.copy(needToLogin = false) }
                        Log.d("UPLOAD_SUCCESS", "${account.email}")
                        val folderId = driveRemoteSource.createFolder(account, folder.name, uiState.value.mainFolderName)
                        folder.images.filter { !it.isUploaded }.forEach { image ->
                            ///////////
                            launch(Dispatchers.IO) {
                                driveRemoteSource.uploadFileToSpecificFolder(
                                    account = account,
                                    fileUri = image.uri,
                                    mimeType = "image/jpg",
                                    fileName = image.uri.toString().substringAfterLast("/"),
                                    folderId = folderId,
                                    folderName = folder.name
                                ).collect { result ->
                                    result.onSuccess { data ->
                                        _uiState.update { oldState ->
                                            oldState.copy(
                                                folders = oldState.folders.map { currentFolder ->
                                                    if (currentFolder.name == folder.name) {
                                                        currentFolder.copy(images = currentFolder.images.map { currentImage ->

                                                            if (!currentImage.isUploaded && image.uri.toString()
                                                                    .substringAfterLast("/") == data.first
                                                            ) {
                                                                if (data.second != 1.0f) {
                                                                    currentImage.copy(
                                                                        uploadProgress = data.second,
                                                                    )
                                                                } else {
                                                                    currentImage.copy(
                                                                        uploadProgress = 1.0f,
                                                                        isUploaded = true
                                                                    )
                                                                }
                                                            } else {
                                                                currentImage
                                                            }

                                                        })
                                                    } else currentFolder
                                                }
                                            )
                                        }
                                        Log.d("UPLOAD_SUCCESS", "uploadFolders: $data")
                                    }
                                    result.onFailure {
                                        Log.d("UPLOAD_SUCCESS", "uploadFolders: ${it.message}")
                                    }
                                }
                            }
                        }
                    }.onFailure {
                        Log.d("UPLOAD_SUCCESS", "uploadFolders: ${it.message}")
                        _uiState.update { it.copy(needToLogin = true) }

                    }
                }
            }
        }
    }


    private fun uploadFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            googleAuth.getSignedInAccount().collect { result ->
                result.onSuccess { account ->
                    _uiState.update { it.copy(needToLogin = false) }
                    Log.d("UPLOAD_SUCCESS", "${account.email}")
                    uiState.value.folders.forEach { folder ->
                        val folderId = driveRemoteSource.createFolder(
                            account,
                            folder.name,
                            uiState.value.mainFolderName
                        )
                        folder.images.filter { !it.isUploaded }.forEach { image ->
                            ///////////
                            launch(Dispatchers.IO) {
                                driveRemoteSource.uploadFileToSpecificFolder(
                                    account = account,
                                    fileUri = image.uri,
                                    mimeType = "image/jpg",
                                    fileName = image.uri.toString().substringAfterLast("/"),
                                    folderId = folderId,
                                    folderName = folder.name
                                ).collect { result ->
                                    result.onSuccess { data ->
                                        _uiState.update { oldState ->
                                            oldState.copy(
                                                folders = oldState.folders.map { currentFolder ->
                                                    if (currentFolder.name == folder.name) {
                                                        currentFolder.copy(images = currentFolder.images.map { currentImage ->

                                                            if (!currentImage.isUploaded && image.uri.toString()
                                                                    .substringAfterLast("/") == data.first
                                                            ) {
                                                                if (data.second != 1.0f) {
                                                                    currentImage.copy(
                                                                        uploadProgress = data.second,
                                                                    )
                                                                } else {
                                                                    currentImage.copy(
                                                                        uploadProgress = 1.0f,
                                                                        isUploaded = true
                                                                    )
                                                                }
                                                            } else {
                                                                currentImage
                                                            }

                                                        })
                                                    } else currentFolder
                                                }
                                            )
                                        }
                                        if (uiState.value.folders.all { folder -> folder.images.all { it.isUploaded } }) {
                                                _uiState.update { oldState ->
                                                    oldState.copy(folders = emptyList())
                                                }
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

                }.onFailure {
                    _uiState.update { it.copy(needToLogin = true) }
                    Log.d("UPLOAD_SUCCESS", "uploadFolders: ${it.message}")
                }
            }
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
                    driveRemoteSource.createFolder(
                        account,
                        folderName,
                        uiState.value.mainFolderName
                    )
                }
            }
        }
    }

    fun resetNeedToLogin() {
        _uiState.update { it.copy(needToLogin = false) }
    }

//
}