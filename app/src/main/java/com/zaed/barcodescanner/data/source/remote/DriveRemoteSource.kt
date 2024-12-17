package com.zaed.barcodescanner.data.source.remote

import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive
import kotlinx.coroutines.flow.Flow

interface DriveRemoteSource {
    fun uploadFileToDrive(
        account: GoogleSignInAccount,
        filePath: String,
        mimeType: String,
        fileName: String
    ): Flow<Result<String>>
    fun getAllFiles(
        account: GoogleSignInAccount,
    )

    fun createFolder(account: GoogleSignInAccount, folderName: String):String
    fun uploadFileToFolder(
        account: GoogleSignInAccount,
        folderId: String,
        filePath: String,
        mimeType: String,
        fileName: String
    )

    fun uploadFileToSpecificFolder(
        account: GoogleSignInAccount,
        fileUri: Uri,
        mimeType: String,
        fileName: String,
        folderId: String,
        folderName: String
    ): Flow<Result<Pair<String,Float>>>
}