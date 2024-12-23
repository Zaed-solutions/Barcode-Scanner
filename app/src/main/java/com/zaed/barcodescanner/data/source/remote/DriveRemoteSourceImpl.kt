package com.zaed.barcodescanner.data.source.remote

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.FileContent
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Collections

class DriveRemoteSourceImpl(
    private val context: Context
) : DriveRemoteSource {
    private fun createDriveService(account: GoogleSignInAccount): Drive {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("UPLOAD_ERROR", "Failed to create Drive service: ${e.message}", e)
            throw e
        }
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


    private fun fileFromContentUri(context: Context, contentUri: Uri): File {

        val fileExtension = getFileExtension(context, contentUri)
        val fileName = "temporary_file" + if (fileExtension != null) ".$fileExtension" else ""

        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }

            oStream.flush()
            oStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tempFile
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }
    override fun uploadFileToSpecificFolder(
        account: GoogleSignInAccount,
        fileUri: Uri,
        mimeType: String,
        fileName: String,
        folderId: String,
        folderName: String
    ): Flow<Result<Pair<String,Float>>> = callbackFlow {
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            name = fileName
            parents = Collections.singletonList(folderId)
        }
        Firebase.firestore.collection("TEST").add(mapOf("datetime" to Clock.System.now().epochSeconds,"readable" to Clock.System.now().toString()))
        val contentResolver: ContentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
        val fileSize: Long = contentResolver.openFileDescriptor(fileUri, "r")?.statSize ?: -1
        Log.d("UPLOAD_SUCCESS", "File size: $fileSize")
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
                            val progress = (uploader.progress).toFloat()
                            trySend(Result.success(Pair(fileName,progress)))
                            Log.d("UPLOAD_SUCCESS", "Upload progress: $progress%")
                        }
                        MediaHttpUploader.UploadState.MEDIA_COMPLETE ->
                            trySend(Result.success(Pair(fileName,1.0f)))
                        else->{}
                    }
                }
            }
            val uploadedFile = request.execute()
            Log.d("UPLOAD_SUCCESS", "File ID: ${uploadedFile.id}")
        } catch (e: Exception) {
            trySend(Result.failure(e))
            Log.e("UPLOAD_SUCCESS", "Failed to upload file: ${e.message}", e)
        }
        awaitClose {  }
    }

    override fun createFolder(
        account: GoogleSignInAccount,
        folderName: String,
        mainFolderName: String
    ): String {
        val driveService = createDriveService(account)

        // Find or create the main folder if provided
        val parentFolderId = if (mainFolderName.isNotEmpty()) {
            val mainFolderId = isFolderExist(account, mainFolderName)
            if (mainFolderId.isNotEmpty()) {
                mainFolderId
            } else {
                // Create the main folder if it doesn't exist
                val mainFolderMetadata = com.google.api.services.drive.model.File().apply {
                    name = mainFolderName
                    mimeType = "application/vnd.google-apps.folder"
                }
                try {
                    val mainFolder = driveService.files()
                        .create(mainFolderMetadata)
                        .setFields("id")
                        .execute()
                    mainFolder.id
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("UPLOAD_ERROR", "Failed to create main folder: ${e.message}", e)
                    return ""
                }
            }
        } else {
            // Use the root folder as the parent
            "root"
        }

        // Check if the target folder already exists in the parent folder
        val query = "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder' and '$parentFolderId' in parents"
        val existingFolders = driveService.files().list()
            .setQ(query)
            .setFields("files(id, name)")
            .execute()
            .files

        if (existingFolders.isNotEmpty()) {
            return existingFolders.first().id
        }

        // Create the folder within the specified parent folder
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            name = folderName
            mimeType = "application/vnd.google-apps.folder"
            parents = listOf(parentFolderId)
        }

        try {
            val folder = driveService.files()
                .create(fileMetadata)
                .setFields("id, name")
                .execute()
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