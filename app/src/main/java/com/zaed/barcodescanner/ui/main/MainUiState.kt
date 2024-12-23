package com.zaed.barcodescanner.ui.main

import com.zaed.barcodescanner.data.models.ProductsFolder

data class MainUiState(
    val text: String = "",
    val folders: List<ProductsFolder> = emptyList(),
    val needToLogin: Boolean = false,
    val thereIsFoldersNotUploadedYet : Boolean = false,
    val mainFolderName: String = ""
)
