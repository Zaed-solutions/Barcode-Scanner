package com.zaed.barcodescanner.data.source.remote

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.FileContent
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.InputStream
import java.util.Collections

class DriveRemoteSourceImpl(
    private val context: Context
) : DriveRemoteSource {
    fun createDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            com.google.api.client.json.gson.GsonFactory(),
            credential
        ).setApplicationName("Barcode Scanner") // Replace with your app's name
            .build()
    }

    override fun uploadFileToDrive(
        account: GoogleSignInAccount,
        filePath: String,
        mimeType: String,
        fileName: String
    ): Flow<Result<String>> = flow {
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            name = fileName
        }
        val contentResolver: ContentResolver = context.contentResolver
        val fileUri: Uri = Uri.parse(filePath)
        val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
        val mediaContent = InputStreamContent(mimeType, inputStream)
        try {
            val driveService = createDriveService(account)
            val uploadedFile =
                driveService.files().create(fileMetadata, mediaContent).setFields("id").execute()
            Log.d("UPLOAD_SUCCESS", "File ID: ${uploadedFile.id}")
        } catch (e: Exception) {
            Log.e("UPLOAD_SUCCESS", "Failed to upload file: ${e.message}", e)
        }
    }

    override fun uploadFileToSpecificFolder(
        account: GoogleSignInAccount,
        fileUri: Uri,
        mimeType: String,
        fileName: String,
        folderId: String
    ): Flow<Result<Double>> = callbackFlow {
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            name = fileName
            parents = Collections.singletonList(folderId)

        }
        val contentResolver: ContentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
        if (inputStream == null) {
            trySend(Result.failure(Exception("Failed to open input stream for URI: $fileUri")))
            return@callbackFlow
        }
        val fileSize: Long = contentResolver.openFileDescriptor(fileUri, "r")?.statSize ?: -1
        val mediaContent = InputStreamContent(mimeType, inputStream).apply {
            length =fileSize
        }
        try {
            val driveService = createDriveService(account)

            val request =
                driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, parents")
            request.mediaHttpUploader.apply {
                progressListener = MediaHttpUploaderProgressListener {uploader->
                    uploader.setDirectUploadEnabled(false)
                    uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE)
                    Log.d("UPLOAD_SUCCESS", "Upload progress: ${uploader.numBytesUploaded}")
                    when(uploader.uploadState){
                        MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS -> {
                            val progress = (uploader.progress *100)
                            trySend(Result.success(progress))
                            Log.d("UPLOAD_SUCCESS", "Upload progress: $progress%")
                        }
                        MediaHttpUploader.UploadState.MEDIA_COMPLETE ->
                            trySend(Result.success(100.0))
                        else->{}
                    }
                }
            }
            val uploadedFile = request.execute()
            Log.d("UPLOAD_SUCCESS", "File ID: ${uploadedFile.id}")
        } catch (e: Exception) {
            trySend(Result.failure(e))
            Log.e("UPLOAD_SUCCESS", "Failed to upload file: ${e.message}", e)
        }finally {
            inputStream?.close()
        }
        awaitClose {  }
    }

    override fun createFolder(
        account: GoogleSignInAccount,
        folderName: String
    ): String {
        val isFolderExists = isFolderExist(account, folderName)
        if (isFolderExists.isNotEmpty()) {
            return isFolderExists
        }
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            name = folderName
            mimeType = "application/vnd.google-apps.folder"
        }
        try {
            val driveService = createDriveService(account)
            val folder = driveService.files().create(fileMetadata).setFields("id, name").execute()
            Log.d("UPLOAD_SUCCESS", "Folder ID: ${folder.id} ${folder.name}")
            return folder.id
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("UPLOAD_ERROR", "Failed to create folder: ${e.message}", e)
            return ""
        }
    }

    override fun uploadFileToFolder(
        account: GoogleSignInAccount,
        folderId: String,
        filePath: String,
        mimeType: String,
        fileName: String
    ) {
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            name = fileName
            parents = Collections.singletonList(folderId)
        }
        val file = File(filePath)
        val mediaContent = FileContent(mimeType, file)
        try {
            val driveService = createDriveService(account)
            val uploadedFile =
                driveService.files().create(fileMetadata, mediaContent).setFields("id, parents")
                    .execute()
            Log.d("UPLOAD_SUCCESS", "File ID: ${uploadedFile.id}")
        } catch (e: Exception) {
            Log.e("UPLOAD_SUCCESS", "Failed to upload file: ${e.message}", e)
        }
    }

    fun moveFileToFolder(
        account: GoogleSignInAccount,
        fileId: String,
        folderId: String
    ) {
        try {
            val driveService = createDriveService(account)
            val uploadedFile =
                driveService.files().get(fileId).setFields("parents").execute()
            val parents = StringBuilder()
            for (parent in uploadedFile.parents) {
                parents.append(parent).append(",")
            }
            val file = driveService.files().update(fileId, null)
                .setAddParents(folderId)
                .setRemoveParents(parents.toString())
                .setFields("id, parents")
                .execute()
            Log.d("UPLOAD_SUCCESS", "File ID: ${file.parents}")
        } catch (e: Exception) {
            Log.e("UPLOAD_ERROR", "Failed to upload file: ${e.message}", e)
        }
    }

    fun deleteFile(
        account: GoogleSignInAccount,
        fileId: String
    ) {
        try {
            val driveService = createDriveService(account)
            driveService.files().delete(fileId).execute()
        } catch (e: Exception) {
            Log.e("UPLOAD_ERROR", "Failed to upload file: ${e.message}", e)
        }
    }

    override fun getAllFiles(
        account: GoogleSignInAccount,
    ) {
        try {
            val service = createDriveService(account)
            val result = service.files().list()
                .setPageSize(100)
                .setFields("nextPageToken, files(id, name)")
                .execute()
            val files = result.files
            if (files == null || files.isEmpty()) {
                Log.d("UPLOAD_SUCCESS", "No files found.")
            } else {
                Log.d("UPLOAD_SUCCESS", "Files:")
                for (file in files) {
                    Log.d("UPLOAD_SUCCESS", "${file.name} (${file.id}) ")
                }

            }
        } catch (e: Exception) {
            Log.e("UPLOAD_SUCCESS", "Failed to upload file: ${e.message}", e)
        }
    }

    private fun isFolderExist(
        account: GoogleSignInAccount,
        folderName: String
    ): String {
        try {
            var folderId = ""
            val service = createDriveService(account)
            val result = service.files().list()
                .setPageSize(100)
                .setFields("nextPageToken, files(id, name)")
                .execute()
            val files = result.files
            if (files == null || files.isEmpty()) {
                Log.d("UPLOAD_SUCCESS", "No files found.")
            } else {
                Log.d("UPLOAD_SUCCESS", "Files:")
                for (file in files) {
                    if (file.name == folderName) {
                        Log.d("UPLOAD_SUCCESS", "${file.name} (${file.id}) ")
                        folderId = file.id
                    }
                }
            }
            return folderId
        } catch (e: Exception) {
            Log.e("UPLOAD_SUCCESS", "Failed to upload file: ${e.message}", e)
            return ""
        }
    }
}