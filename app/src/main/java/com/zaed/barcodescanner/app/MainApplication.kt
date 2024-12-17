package com.zaed.barcodescanner.app

import android.app.Application
import com.zaed.barcodescanner.app.di.appModule

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
//    companion object {
//        lateinit var realm: Realm
//    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }


//        realm = Realm.open(
//            configuration = RealmConfiguration.create(
//                schema = setOf(
//                )
//            )
//        )
    }
}