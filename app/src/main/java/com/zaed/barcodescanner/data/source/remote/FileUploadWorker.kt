package com.zaed.barcodescanner.data.source.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class FileUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val account: GoogleSignInAccount,
    private val driveRemoteSource: DriveRemoteSource
) : CoroutineWorker(appContext, workerParams) {

    private val folderName = inputData.getString("folderName") ?: ""
    private val fileUri = Uri.parse(inputData.getString("fileUri"))
    private val folderId = inputData.getString("folderId") ?: ""
    private val mimeType = inputData.getString("mimeType") ?: ""
    private val fileName = inputData.getString("fileName") ?: ""

    override suspend fun doWork(): Result {
        return try {

                driveRemoteSource.uploadFileToSpecificFolder(
                    account = account,
                    fileUri = fileUri,
                    mimeType = mimeType,
                    fileName = fileName,
                    folderId = folderId,
                    folderName = folderName
                ).collect { result ->
                    result.onSuccess { data ->
                        setProgressAsync(workDataOf("progress" to data.second))

                        Log.d("UPLOAD_SUCCESS", "Upload progress: ${data.second}")
                    }
                    result.onFailure {
                        Log.e("UPLOAD_SUCCESS", "Failed to upload file: ${it.message}", it)
                    }
                }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
