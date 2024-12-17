package com.zaed.barcodescanner.app.di

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.zaed.barcodescanner.data.source.remote.DriveRemoteSource
import com.zaed.barcodescanner.data.source.remote.DriveRemoteSourceImpl
import com.zaed.barcodescanner.data.source.remote.GoogleAuth
import com.zaed.barcodescanner.data.source.remote.GoogleAuthImpl
import org.koin.android.ext.koin.androidContext
import com.zaed.barcodescanner.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainViewModel)
    singleOf(::DriveRemoteSourceImpl){ bind<DriveRemoteSource>()}
    singleOf(::GoogleAuthImpl){ bind<GoogleAuth>()}
//    includes(remoteSourceModule)
}


val remoteSourceModule = module {

}
