package com.zaed.barcodescanner.data.models

import android.net.Uri

data class ProductImage(
    val uri: Uri = Uri.EMPTY,
    val fileName: String = "",
    val mimeType: String = "",
    val uploadProgress: Float = 0f,
    val isUploaded: Boolean = false,
)
