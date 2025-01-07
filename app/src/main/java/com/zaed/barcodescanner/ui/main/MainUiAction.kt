package com.zaed.barcodescanner.ui.main

import android.net.Uri

sealed interface MainUiAction {
    data object OnScanBarcodeClicked: MainUiAction
    data object OnUploadFolders: MainUiAction
    data class OnAddProductImageClicked(val folderName: String): MainUiAction
    data class OnDeleteProductsFolder(val folderName: String): MainUiAction
    data class OnDeleteProductImage(val folderName: String, val imageUri: Uri): MainUiAction
    data class OnAddNewFolder(val folderName: String): MainUiAction
    data class OnAddNewProductImage(val folderName: String, val uri: Uri): MainUiAction
    data class OnUploadFolder(val folderName: String): MainUiAction
    data class OnEnteredBarcodeManually(val barcode: String): MainUiAction
    data object OnSignOut: MainUiAction
    data object OnDeleteAllFoldersClicked: MainUiAction
    data object OnDeleteAllFolders: MainUiAction
    data object OnWriteBarcodeManuallyClicked: MainUiAction
    data object OnSearchClicked: MainUiAction


}