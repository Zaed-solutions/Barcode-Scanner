package com.zaed.barcodescanner.data.source.remote

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.Flow

interface GoogleAuth {
    suspend fun signOut(): Boolean
    fun getSignedInAccount(): Flow<Result<GoogleSignInAccount>>
}