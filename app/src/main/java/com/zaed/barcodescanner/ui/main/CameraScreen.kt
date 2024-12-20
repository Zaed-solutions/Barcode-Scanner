package com.zaed.barcodescanner.ui.main

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun CameraPreviewScreen(
    imageQuality: Int = 20,
    numberOfImageCapturedForCurrentFolder: Int = 0,
    onImageCaptured: (Uri) -> Unit = {},
    onImageCapturedWithAddNewImage: (Uri) -> Unit = {},
    onImageCapturedFailed: () -> Unit = {},
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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
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
            IconButton(
                {
                    onImageCapturedFailed()

                }, modifier = Modifier
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
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Log.d("teno", "CameraPreviewScreen: ${uris.size}")
                AnimatedVisibility(uris.isNotEmpty()) {
                    Badge() {
                        Text(
                            text = "$uriCount",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        tonalElevation = 0.1.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = uris.last(),
                                    contentScale = ContentScale.Crop,
                                    filterQuality = FilterQuality.Low
                                ),
                                contentDescription = "Attachment BackGround",
                                modifier = Modifier.fillMaxSize()
                            )


                        }
                    }
                }
                IconButton(
                    enabled = uris.isNotEmpty(),
                    onClick = {
                        submitImages(uris)
                    },

                    ) {

                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Cancel",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            IconButton(
                {
                    captureImage(
                        imageCapture = imageCapture,
                        context = context,
                        onImageCaptured = {
                            uris.add(it)
                            uriCount++
                        },
                        onImageCapturedFailed = onImageCapturedFailed
                    )
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(
                        84.dp
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = "Cancel",
                    tint = Color.White,
                    modifier = Modifier.size(84.dp),
                )
            }


        }
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .background(
//                        Color.White.copy(alpha = 0.8f),
//                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
//                    )
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = "Image Captured ($numberOfImageCapturedForCurrentFolder)",
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 18.sp,
//                    color = Color(0xFF333333)
//                )
//
//                Row(
//                    horizontalArrangement = Arrangement.SpaceEvenly,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//
//                    CameraActionButton(
//                        modifier = Modifier.weight(1f).padding(4.dp),
//                        text = "Capture one",
//                        color = Color.Blue.copy(alpha = 0.8f),
//                        onClick = {
//                            captureImage(
//                                imageCapture = imageCapture,
//                                context = context,
//                                onImageCaptured = onImageCaptured,
//                                onImageCapturedFailed = onImageCapturedFailed
//                            )
//                        }
//                    )
//                    CameraActionButton(
//                        modifier = Modifier.weight(1f).padding(4.dp),
//                        text = "Capture Many",
//                        color = Color.Green.copy(alpha = 0.8f),
//                        onClick = {
//                            captureImage(
//                                imageCapture = imageCapture,
//                                context = context,
//                                onImageCaptured = onImageCapturedWithAddNewImage,
//                                onImageCapturedFailed = onImageCapturedFailed
//                            )
//                        }
//                    )
//                }
//            }
    }
    // Bottom button bar


}

@Composable
fun CameraActionButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun captureImage(
    imageCapture: ImageCapture,
    context: Context,
    onImageCaptured: (Uri) -> Unit,
    onImageCapturedFailed: () -> Unit
) {
    val name = "CameraxImage_${System.currentTimeMillis()}.jpeg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
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