package com.zaed.barcodescanner.data.source.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.zaed.barcodescanner.data.source.remote.GoogleAuthentication.signInOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GoogleAuthImpl(
    private val context: Context
) : GoogleAuth {
    override suspend fun signOut(): Boolean {
        try {
            GoogleSignIn.getClient(context, signInOptions).signOut()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun getSignedInAccount(): Flow<Result<GoogleSignInAccount>>  = flow {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                emit(Result.success(account))
            } else {
                emit(Result.failure(Exception("No account found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
            e.printStackTrace()
        }
    }
}