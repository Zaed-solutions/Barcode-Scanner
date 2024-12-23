package com.zaed.barcodescanner.ui.main

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.zaed.barcodescanner.R
import com.zaed.barcodescanner.data.models.ProductsFolder
import com.zaed.barcodescanner.ui.main.components.ConfirmDeleteAllFoldersDialog
import com.zaed.barcodescanner.ui.main.components.ConfirmDeleteDialog
import com.zaed.barcodescanner.ui.main.components.ConfirmNavigateToLoginDialog
import com.zaed.barcodescanner.ui.main.components.EnterBarCodeDialog
import com.zaed.barcodescanner.ui.main.components.FoldersList
import com.zaed.barcodescanner.ui.util.createImageFile
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


val TAG = "MainScreen"

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    navigateToLogin: () -> Unit = {},
    imageQuality: Int = 20
) {
    LaunchedEffect(true) {
        Log.d(TAG, "MainScreen: LaunchedEffect")
    }
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedFolder by remember {
        mutableStateOf("")
    }
    var isCameraBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var isWrongBarcodeSheetVisible by remember {
        mutableStateOf(false)
    }

    MainScreenContent(
        modifier = modifier,
        submitImages = { images ->
            images.forEach { image ->
                viewModel.handleAction(
                    MainUiAction.OnAddNewProductImage(
                        selectedFolder,
                        image
                    )
                )
            }
        },
        folders = state.folders,
        thereIsFoldersNotUploadedYet = state.thereIsFoldersNotUploadedYet,
        isWrongBarcodeSheetVisible = isWrongBarcodeSheetVisible,
        hostState = snackbarHostState,
        isCameraBottomSheetVisible = isCameraBottomSheetVisible,
        closeCameraBottomSheet = {
            isCameraBottomSheetVisible = false
        },
        closeWrongBarcodeSheet = {
            isWrongBarcodeSheetVisible = false
        },
        needToLogin = state.needToLogin,
        navigateToLogin = navigateToLogin,
        resetNeedToLogin = { viewModel.resetNeedToLogin() },
        resetThereIsFoldersNotUploadedYet = { viewModel.resetThereIsFoldersNotUploadedYet() },
        onImageCapturedFailed = {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.image_capture_failed))
            }
            isCameraBottomSheetVisible = false
        },
        imageQuality = imageQuality,
        onAction = { action ->
            when (action) {
                is MainUiAction.OnAddProductImageClicked -> {
                    selectedFolder = action.folderName
                    isCameraBottomSheetVisible = true
                }

                is MainUiAction.OnEnteredBarcodeManually -> {
                    val code = action.barcode
                    if (code.isNotBlank() && state.folders.none { it.name == code }) {
                        viewModel.handleAction(
                            MainUiAction.OnAddNewFolder(
                                code
                            )
                        )
                        val result = createImageFile(context)
                        Log.d(TAG, "MainScreen: Image file created: $result")
                        selectedFolder = code
                        isCameraBottomSheetVisible = true
                    } else {
                        selectedFolder = code
                        isCameraBottomSheetVisible = true
                    }
                }

                MainUiAction.OnScanBarcodeClicked -> {
                    val options = GmsBarcodeScannerOptions.Builder()
                        .enableAutoZoom()
                        .build()
                    val scanner = GmsBarcodeScanning.getClient(context, options)
                    scanner.startScan().addOnSuccessListener { barCode ->
                        if ((barCode.rawValue?.trim()?.length ?: 0) > 7) {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.invalid_barcode))
                            }
                            return@addOnSuccessListener
                        } else {
                            Log.d("Barcode", "${barCode.rawValue}")
                            val code = barCode.rawValue?.trim() ?: ""
                            if (code.isNotBlank() && state.folders.none { it.name == code }) {
                                viewModel.handleAction(
                                    MainUiAction.OnAddNewFolder(
                                        code
                                    )
                                )
                                val result = createImageFile(context)
                                Log.d(TAG, "MainScreen: Image file created: $result")
                                selectedFolder = code
                                isCameraBottomSheetVisible = true
                            } else {
//                                scope.launch {
//                                    snackbarHostState.showSnackbar(context.getString(R.string.folder_already_exists))
//                                }
                                selectedFolder = code
                                isCameraBottomSheetVisible = true
                            }
                        }
                    }.addOnCanceledListener {
                        Log.e("Barcode", "Cancelled")
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.barcode_scan_cancelled))
                        }
                    }.addOnFailureListener { e ->
                        Log.e("Barcode", "Failed: ${e.message}")
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.barcode_scan_failed))
                        }
                    }
                }

                MainUiAction.OnWriteBarcodeManuallyClicked -> {

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
    hostState: SnackbarHostState = SnackbarHostState(),
    folders: List<ProductsFolder> = emptyList(),
    thereIsFoldersNotUploadedYet: Boolean = false,
    onAction: (MainUiAction) -> Unit = {},
    navigateToLogin: () -> Unit = {},
    closeWrongBarcodeSheet: () -> Unit = {},
    isCameraBottomSheetVisible: Boolean = false,
    closeCameraBottomSheet: () -> Unit = {},
    needToLogin: Boolean = false,
    isWrongBarcodeSheetVisible: Boolean = false,
    resetNeedToLogin: () -> Unit = {},
    onImageCapturedFailed: () -> Unit = {},
    imageQuality: Int = 20,
    submitImages: (List<Uri>) -> Unit = {},
    resetThereIsFoldersNotUploadedYet: () -> Unit = {},
) {
    var selectedFolderName by remember {
        mutableStateOf("")
    }
    var isConfirmDeleteSheetVisible by remember {
        mutableStateOf(false)
    }

    var isNeedToLoginSheetVisible by remember {
        mutableStateOf(false)
    }
    var isThereIsFoldersNotUploadedYetSheetVisible by remember {
        mutableStateOf(false)
    }
    var isEnterBarCodeManuallySheetVisible by remember {
        mutableStateOf(false)
    }
    var isOptionsMenuVisible by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = needToLogin) {
        if (needToLogin) {
            isNeedToLoginSheetVisible = true
            resetNeedToLogin()
        }
    }
    LaunchedEffect(thereIsFoldersNotUploadedYet) {
        if (thereIsFoldersNotUploadedYet) {
            isThereIsFoldersNotUploadedYetSheetVisible = true
            resetThereIsFoldersNotUploadedYet()
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(
                            onClick = { isOptionsMenuVisible = !isOptionsMenuVisible },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                            )
                        }
                        DropdownMenu(
                            expanded = isOptionsMenuVisible,
                            onDismissRequest = { isOptionsMenuVisible = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    isOptionsMenuVisible = false
                                    isEnterBarCodeManuallySheetVisible = true
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.add_folder),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onAction(MainUiAction.OnUploadFolders)
                                    isOptionsMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.upload_all),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onAction(MainUiAction.OnDeleteAllFoldersClicked)
                                    isOptionsMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.delete_all),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                onClick = {
                                    navigateToLogin()
                                    isOptionsMenuVisible = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.settings),
                                    )
                                },
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction(MainUiAction.OnScanBarcodeClicked)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = null
                )
            }

        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FoldersList(
                modifier = Modifier
                    .fillMaxWidth(),
                folders = folders,
                onAddImageClicked = { folderName ->
                    onAction(MainUiAction.OnAddProductImageClicked(folderName))
                },
                onDeleteImage = { folderName, uri ->
                    onAction(MainUiAction.OnDeleteProductImage(folderName, uri))
                },
                onDeleteFolderClicked = { folderName ->
                    selectedFolderName = folderName
                    isConfirmDeleteSheetVisible = true
                },
                onUploadFolder = { folderName ->
                    onAction(MainUiAction.OnUploadFolder(folderName))
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
            AnimatedVisibility(isNeedToLoginSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isNeedToLoginSheetVisible = false
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmNavigateToLoginDialog(
                        onDismiss = {
                            isNeedToLoginSheetVisible = false
                        },
                        onConfirm = {
                            isNeedToLoginSheetVisible = false
                            navigateToLogin()
                        }
                    )
                }
            }
            AnimatedVisibility(isEnterBarCodeManuallySheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isEnterBarCodeManuallySheetVisible = false
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    EnterBarCodeDialog(
                        onDismiss = {
                            isEnterBarCodeManuallySheetVisible = false
                        },
                        onConfirm = { barcode ->
                            isEnterBarCodeManuallySheetVisible = false
                            onAction(MainUiAction.OnEnteredBarcodeManually(barcode))
                        }
                    )
                }
            }
            AnimatedVisibility(isThereIsFoldersNotUploadedYetSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isThereIsFoldersNotUploadedYetSheetVisible = false
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmDeleteAllFoldersDialog(
                        onDismiss = {
                            isThereIsFoldersNotUploadedYetSheetVisible = false
                        },
                        onConfirm = {
                            isThereIsFoldersNotUploadedYetSheetVisible = false
                            onAction(MainUiAction.OnDeleteAllFolders)
                        }
                    )
                }
            }
            AnimatedVisibility(isWrongBarcodeSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        closeWrongBarcodeSheet()
                    },
                    sheetState = rememberModalBottomSheetState()
                ) {
                    ConfirmNavigateToLoginDialog(
                        onDismiss = {
                            isNeedToLoginSheetVisible = false
                        },
                        onConfirm = {
                            isNeedToLoginSheetVisible = false
                            navigateToLogin()
                        }
                    )
                }
            }
            AnimatedVisibility(isCameraBottomSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        closeCameraBottomSheet()
                    },
                    sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    ),
                    dragHandle = {}
                ) {
                    CameraPreviewScreen(
                        imageQuality = imageQuality,
                        onImageCapturedFailed = onImageCapturedFailed,
                        submitImages = submitImages,
                        onDismiss = {
                            closeCameraBottomSheet()
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreenContent(
        folders = listOf(
            ProductsFolder(
                name = "Folder 1",
                images = listOf(
                    com.zaed.barcodescanner.data.models.ProductImage(Uri.EMPTY),
                    com.zaed.barcodescanner.data.models.ProductImage(Uri.EMPTY),
                ),
            ),
            ProductsFolder("Folder 2"),
            ProductsFolder("Folder 3"),
        )

    )
}