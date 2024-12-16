package com.zaed.barcodescanner.ui.main

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.zaed.barcodescanner.R
import com.zaed.barcodescanner.data.models.ProductsFolder
import com.zaed.barcodescanner.ui.main.components.ConfirmDeleteDialog
import com.zaed.barcodescanner.ui.main.components.FoldersList
import com.zaed.barcodescanner.ui.util.createImageFile
import org.koin.androidx.compose.koinViewModel


val TAG = "MainScreen"
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
) {
    LaunchedEffect (true){
        Log.d(TAG, "MainScreen: LaunchedEffect")
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedFolder by remember {
        mutableStateOf("")
    }
    var photoUri by rememberSaveable{ mutableStateOf<Uri?>(null) }
    val cameraCaptureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            Log.d(TAG, "MainScreen: Image capture result: $success, $photoUri")
            if (success && photoUri != null) {
                Log.d(TAG, "MainScreen: Image capture successful.")
                viewModel.handleAction(MainUiAction.OnAddNewProductImage(selectedFolder, photoUri?:Uri.EMPTY))
            } else {
                Log.d(TAG, "Image capture failed.")
            }
        }
    MainScreenContent(
        modifier = modifier,
        folders = state.folders,
        onAction = { action ->
            when(action){
                is MainUiAction.OnAddProductImageClicked -> {
                    val result = createImageFile(context)
                    Log.d(TAG, "MainScreen: Image file created: $result")
                    photoUri = result
                    selectedFolder = action.folderName
                    cameraCaptureLauncher.launch(photoUri?:Uri.EMPTY)
                }
                MainUiAction.OnScanBarcodeClicked -> {
                    val options = GmsBarcodeScannerOptions.Builder()
//                        .setBarcodeFormats(
//                            Barcode.FORMAT_QR_CODE,
//                            Barcode.FORMAT_AZTEC)
                        .enableAutoZoom()
                        .build()
                    val scanner = GmsBarcodeScanning.getClient(context, options)
                    scanner.startScan().addOnSuccessListener { barCode ->
                        Log.d("Barcode", "${ barCode.rawValue }")
                        viewModel.handleAction(MainUiAction.OnAddNewFolder(barCode.rawValue?:""))
                    }.addOnCanceledListener {
                        Log.e("Barcode", "Cancelled")
                    }.addOnFailureListener { e ->
                        Log.e("Barcode", "Failed: ${e.message}")
                    }
                }
                else -> viewModel.handleAction(action)
            }
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    folders: List<ProductsFolder>,
    onAction: (MainUiAction) -> Unit,
) {
    var selectedFolderName by remember {
        mutableStateOf("")
    }
    var isConfirmDeleteSheetVisible by remember{
        mutableStateOf(false)
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {

                },
                actions = {

                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction(MainUiAction.OnScanBarcodeClicked)
                }
            ){
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = null
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            FoldersList(
                modifier = Modifier
                    .fillMaxSize(),
                folders = folders,
                onAddImageClicked = { folderName ->
                    onAction(MainUiAction.OnAddProductImageClicked(folderName))
                },
                onDeleteFolderClicked = {folderName ->
                    selectedFolderName = folderName
                    isConfirmDeleteSheetVisible = true
                }
            )
            AnimatedVisibility(isConfirmDeleteSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        selectedFolderName = ""
                        isConfirmDeleteSheetVisible = false
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmDeleteDialog(
                        label = stringResource(R.string.folder),
                        onDismiss = {
                            selectedFolderName = ""
                            isConfirmDeleteSheetVisible = false
                        },
                        onConfirm = {
                            isConfirmDeleteSheetVisible = false
                            onAction(MainUiAction.OnDeleteProductsFolder(selectedFolderName))
                            selectedFolderName = ""
                        }
                    )
                }
            }
        }
    }

}