package com.zaed.barcodescanner.ui.main

import android.net.Uri

sealed interface MainUiAction {
    data object OnScanBarcodeClicked: MainUiAction
    data class OnAddProductImageClicked(val folderName: String): MainUiAction
    data class OnDeleteProductsFolder(val folderName: String): MainUiAction
    data class OnAddNewFolder(val folderName: String): MainUiAction
    data class OnAddNewProductImage(val folderName: String, val uri: Uri): MainUiAction
}