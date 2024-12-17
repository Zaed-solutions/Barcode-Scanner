package com.zaed.barcodescanner.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.zaed.barcodescanner.data.source.remote.GoogleAuthentication
import com.zaed.barcodescanner.data.source.remote.GoogleAuthentication.signInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections

@Composable
fun GoogleAuth() {
    val context = LocalContext.current
    val lifecycleScope = rememberCoroutineScope()

    val startForResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (result.data != null) {
                    val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    task.addOnSuccessListener { account ->
                        Log.d("TESTO", "TestDrive: ${task.result.email}")
                        Log.d("TESTO", "TestDrive: ${task.result.displayName}")
                        Log.d("TESTO", "TestDrive: ${task.result.photoUrl}")
                        ///////////////////////////////////////////////////////////////
                        // pass the account to viewmodel
                        val fileName = "myFile.txt"
                        val file = File(context.filesDir, fileName) // Use filesDir for permanent files
                        Log.d("FilePath", "Internal File Path: ${file.absolutePath}")
                    }
//                    }
                } else {
                    Toast.makeText(context, "Google Login Error!", Toast.LENGTH_LONG).show()
                }
            }
        }
    Column {
        Button(
            onClick = {
                startForResult.launch(GoogleSignIn.getClient(context, signInOptions).signInIntent)
            }, modifier = androidx.compose.ui.Modifier
        ) {
            Text(text = "Sign in with Google")
        }
        Button(
            onClick = {
                // Sign out from :[Google]

            }){
            Text(text = "log out")
        }
    }


}
