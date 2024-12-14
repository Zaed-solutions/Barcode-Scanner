package com.zaed.scanqrcode.app

import android.app.Application
import com.zaed.scanqrcode.app.di.appModule
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    companion object {
        lateinit var realm: Realm
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }


        realm = Realm.open(
            configuration = RealmConfiguration.create(
                schema = setOf(
                )
            )
        )
    }
}