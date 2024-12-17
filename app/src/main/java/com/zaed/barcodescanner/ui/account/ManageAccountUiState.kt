package com.zaed.barcodescanner.ui.account

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

data class ManageAccountUiState(
    val currentAccount : GoogleSignInAccount? = null,
)
