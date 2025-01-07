package com.zaed.barcodescanner.data.source.remote

import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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

    fun createFolder(account: GoogleSignInAccount, folderName: String, mainFolderName: String):String
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

    fun searchFolderAndGetImages(
        account: GoogleSignInAccount,
        folderName: String
    ): Flow<Result<List<String>>>
}