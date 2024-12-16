package com.zaed.barcodescanner.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.barcodescanner.data.models.ProductImage
import com.zaed.barcodescanner.data.models.ProductsFolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    fun handleAction(action: MainUiAction){
        when(action){
            is MainUiAction.OnDeleteProductsFolder -> {
                deleteProductsFolder(action.folderName)
            }
            is MainUiAction.OnAddNewFolder -> {
                addNewFolder(action.folderName)
            }
            is MainUiAction.OnAddNewProductImage -> {
                addNewProductImage(action.folderName, action.uri)
            }
            is MainUiAction.OnDeleteProductImage -> deleteProductImage(action.folderName, action.imageUri)
            MainUiAction.OnUploadFolders -> uploadFolders()
            else -> Unit
        }
    }

    private fun deleteProductImage(folderName: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { oldState ->
                oldState.copy(
                    folders = oldState.folders.map { folder ->
                        if(folder.name == folderName){
                            folder.copy(images = folder.images.filter { it.uri != imageUri })
                        } else {
                            folder
                        }
                    }
                )
            }
        }
    }

    private fun uploadFolders() {
        TODO("Not yet implemented")
    }

    private fun addNewProductImage(folderName: String, uri: Uri) {
        viewModelScope.launch {
            val image = ProductImage(uri)
            _uiState.update { oldState ->
                oldState.copy(
                    folders = oldState.folders.map { folder ->
                        if(folder.name == folderName){
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
}