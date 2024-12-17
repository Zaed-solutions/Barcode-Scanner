package com.zaed.barcodescanner.data.models

data class ProductsFolder(
    val name: String = "",
    val images: List<ProductImage> = emptyList(),
    val isUploading: Boolean = false
)
