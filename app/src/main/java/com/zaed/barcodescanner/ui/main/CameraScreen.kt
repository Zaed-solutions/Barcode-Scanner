package com.zaed.barcodescanner.ui.main

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPreviewScreen(
    imageQuality: Int = 20,
    onImageCapturedFailed: () -> Unit = {},
    onDismiss: () -> Unit = {},
    submitImages: (List<Uri>) -> Unit = {}
) {
    var uris = remember { mutableListOf<Uri>() }
    var uriCount by remember { mutableStateOf(0) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val preview = remember { Preview.Builder().build() }
    val previewView = remember { PreviewView(context) }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val imageCapture = remember {
        ImageCapture.Builder()
            .setJpegQuality(imageQuality)
            .build()
    }
    var scope = rememberCoroutineScope()

    var isFullScreenImageVisible by remember { mutableStateOf(false) }
    var showFlash by remember { mutableStateOf(false) }  // Flash state

    // Flash opacity animation
    val flashAlpha by animateFloatAsState(targetValue = if (showFlash) 1f else 0f)

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            preview.surfaceProvider = previewView.surfaceProvider
        } catch (e: Exception) {
            println("Error initializing CameraX: ${e.message}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

// Flash Overlay with Center Animation
        AnimatedVisibility(
            visible = showFlash,
            enter = scaleIn(
                animationSpec = tween(durationMillis = 150),
                initialScale = 0.1f, // Starts small from the center
                transformOrigin = TransformOrigin.Center
            ) + fadeIn(animationSpec = tween(150)),
            exit = scaleOut(
                animationSpec = tween(durationMillis = 150),
                targetScale = 0.1f, // Shrinks back to the center
                transformOrigin = TransformOrigin.Center
            ) + fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }


        // Bottom Control Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.7f),
                        )
                    )
                ),
        ) {
            // Cancel Button
            IconButton(
                { onDismiss() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Cancel",
                    tint = Color.Red,
                    modifier = Modifier.size(36.dp),
                )
            }

            // Submit Button & Thumbnail
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(uriCount > 0) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.surfaceVariant),
                        tonalElevation = 0.1.dp,
                        onClick = { isFullScreenImageVisible = true }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uris.last()),
                                contentDescription = "Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Badge {
                        Text(
                            text = "$uriCount",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    enabled = uriCount > 0,
                    onClick = {
                        submitImages(uris)
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Submit",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            // Capture Button
            IconButton(
                {
                    showFlash = true // Trigger flash animation
                    captureImage(
                        imageCapture = imageCapture,
                        context = context,
                        onImageCaptured = {
                            uris.add(it)
                            uriCount++
                            scope.launch() {
                                delay(25)
                                showFlash = false // Hide flash after delay
                            }
                        },
                        onImageCapturedFailed = {
                            showFlash = false // Hide flash if failed
                            onImageCapturedFailed()
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(84.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = "Capture",
                    tint = Color.White,
                    modifier = Modifier.size(84.dp),
                )
            }
        }

        // Full-Screen Image Viewer
        AnimatedVisibility(isFullScreenImageVisible) {
            ModalBottomSheet(
                dragHandle = {},
                onDismissRequest = { isFullScreenImageVisible = false },
                shape = RoundedCornerShape(0.dp),
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        16.dp
                    )
                ) {
                    item {
                        IconButton(
                            modifier = Modifier
                                .padding(24.dp)
                                .size(32.dp),
                            onClick = { isFullScreenImageVisible = false }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(uris) { uri ->
                        Box {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Captured Image",
                                modifier = Modifier
                                    .heightIn(min = 350.dp)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.FillWidth
                            )
                            IconButton(
                                onClick = {
                                    uris.remove(uri)
                                    uriCount--
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .background(MaterialTheme.colorScheme.onError, CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun captureImage(
    imageCapture: ImageCapture,
    context: Context,
    onImageCaptured: (Uri) -> Unit,
    onImageCapturedFailed: () -> Unit
) {
    val name = "CameraxImage_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Images")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageCaptured(outputFileResults.savedUri!!)
                println("Image saved successfully: ${outputFileResults.savedUri}")
            }

            override fun onError(exception: ImageCaptureException) {
                onImageCapturedFailed()
                println("Image capture failed: ${exception.localizedMessage}")
            }
        }
    )
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                continuation.resume(cameraProviderFuture.get())
            } catch (e: Exception) {
                continuation.cancel(e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun CameraPreviewScreenPreview() {
    CameraPreviewScreen()
}