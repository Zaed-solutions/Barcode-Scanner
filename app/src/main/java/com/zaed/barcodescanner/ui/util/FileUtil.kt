package com.zaed.barcodescanner.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createImageFile(context: Context): Uri {
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File.createTempFile("photo_${timestamp}_", ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

suspend fun downloadImage(context: Context, imageUrl: String) {
    val client = OkHttpClient()

    // Make network call to download the image
    val request = Request.Builder().url(imageUrl).build()
    val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }

    if (response.isSuccessful) {
        val imageBytes = response.body?.byteStream()
        val fileName = "downloaded_image_${System.currentTimeMillis()}.jpg"
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        // Write to file
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { output ->
                imageBytes?.copyTo(output)
            }
        }

        Toast.makeText(context, "Image saved to Downloads", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
    }
}
fun shareImageFromUrl(context: Context, imageUrl: String) {
    val imageLoader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .target { result ->
            val file = File(context.cacheDir, "shared_image.jpg")
            file.outputStream().use { outputStream ->
                val bitmap = (result as BitmapDrawable).bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Image"))
        }
        .build()
    imageLoader.enqueue(request)
}
suspend fun downloadImageWithMediaStore(context: Context, imageUrl: String) {
    val client = OkHttpClient()

    // Make network call to download the image
    val request = Request.Builder().url(imageUrl).build()
    val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }

    if (response.isSuccessful) {
        val imageBytes = response.body?.byteStream()
        val fileName = "downloaded_image_${System.currentTimeMillis()}.jpg"

        // Save image using MediaStore for Android 10 and higher
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

        val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            withContext(Dispatchers.IO) {
                resolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                    imageBytes?.copyTo(outputStream)
                }
            }
            Toast.makeText(context, "Image saved to DCIM", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
    }
}
