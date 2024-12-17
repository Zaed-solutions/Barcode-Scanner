package com.zaed.barcodescanner.ui.main

import com.zaed.barcodescanner.data.models.ProductsFolder

data class MainUiState(
    val text: String = "",
    val folders: List<ProductsFolder> = emptyList(),
    val progress: Double = 0.0
)
