package com.bettythegoodboi.crashdemo

import android.app.Application
import com.bettythegoodboi.crashkit.CrashKit

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashKit.init(this, enableCollection = true)
        CrashKit.setUserId("global-demo-user")
        CrashKit.log("DemoApp started")
    }
}
