package com.zaed.barcodescanner.app.di

import com.zaed.barcodescanner.data.source.remote.DriveRemoteSource
import com.zaed.barcodescanner.data.source.remote.DriveRemoteSourceImpl
import com.zaed.barcodescanner.data.source.remote.GoogleAuth
import com.zaed.barcodescanner.data.source.remote.GoogleAuthImpl
import com.zaed.barcodescanner.ui.account.ManageAccountViewModel
import com.zaed.barcodescanner.ui.main.MainViewModel
import com.zaed.barcodescanner.data.source.local.FolderDataStore
import com.zaed.barcodescanner.data.source.local.FolderDataStoreImpl
import com.zaed.barcodescanner.ui.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    //ViewModel
    viewModelOf(::MainViewModel)
    viewModelOf(::ManageAccountViewModel)
    viewModelOf(::SearchViewModel)
    //Remote Source
    singleOf(::DriveRemoteSourceImpl){ bind<DriveRemoteSource>()}
    singleOf(::GoogleAuthImpl){ bind<GoogleAuth>()}
    singleOf(::FolderDataStoreImpl) { bind<FolderDataStore>() }

}

