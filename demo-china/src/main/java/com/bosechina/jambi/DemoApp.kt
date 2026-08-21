package com.bosechina.jambi

import android.app.Application
import com.bettythegoodboi.crashkit.CrashKit

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashKit.init(
            this,
            appKey = BuildConfig.UMENG_APPKEY,
            channel = BuildConfig.UMENG_CHANNEL,
            enableCollection = true
        )
        CrashKit.setUserId("china-demo-user")
    }
}
