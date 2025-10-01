package com.hisaabi.hisaabi_kmp

import android.app.Application
import com.hisaabi.hisaabi_kmp.di.initKoin
import org.koin.android.ext.koin.androidContext

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(config = {
            androidContext(this@MyApplication)
        })
    }
}