package com.bettythegoodboi.crashkit

import android.content.Context
import androidx.annotation.Keep
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Production CrashKit — **Global** (Firebase Crashlytics only).
 *
 * Host must provide google-services.json + Google Services & Crashlytics Gradle plugins.
 */
@Keep
object CrashKit {

    @Volatile
    private var initialized = false

    @JvmStatic
    fun init(context: Context, enableCollection: Boolean = true) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val app = context.applicationContext
            if (FirebaseApp.getApps(app).isEmpty()) {
                FirebaseApp.initializeApp(app)
                    ?: error(
                        "FirebaseApp failed. Ensure google-services.json and Google Services plugin."
                    )
            }
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(enableCollection)
            crashlytics.setCustomKey("crash_kit", "global")
            crashlytics.setCustomKey("crash_kit_version", "1.0.0")
            initialized = true
        }
    }

    @JvmStatic
    fun setCollectionEnabled(enabled: Boolean) {
        ensureInit()
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
    }

    @JvmStatic
    fun setUserId(userId: String) {
        ensureInit()
        FirebaseCrashlytics.getInstance().setUserId(userId)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: String) {
        ensureInit()
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Int) {
        ensureInit()
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Boolean) {
        ensureInit()
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    @JvmStatic
    fun log(message: String) {
        ensureInit()
        FirebaseCrashlytics.getInstance().log(message)
    }

    @JvmStatic
    fun recordException(throwable: Throwable) {
        ensureInit()
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    private fun ensureInit() {
        check(initialized) { "CrashKit.init(context) must be called first." }
    }
}
