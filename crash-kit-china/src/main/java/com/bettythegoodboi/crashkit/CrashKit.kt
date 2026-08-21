package com.bettythegoodboi.crashkit

import android.content.Context
import androidx.annotation.Keep
import com.umeng.commonsdk.UMConfigure
import com.umeng.umcrash.UMCrash

/**
 * Production CrashKit — **China** (Umeng U-APM only).
 *
 * Call [init] with Umeng AppKey + channel after privacy consent.
 * Phone must reach errnewlog.umeng.com (China-oriented).
 */
@Keep
object CrashKit {

    @Volatile
    private var initialized = false

    /**
     * @param appKey Umeng AppKey for the target app
     * @param channel distribution channel string (e.g. "official")
     * @param enableCollection when false, still inits structure but you may delay full use until consent
     */
    @JvmStatic
    @JvmOverloads
    fun init(
        context: Context,
        appKey: String,
        channel: String = "default",
        enableCollection: Boolean = true
    ) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val app = context.applicationContext
            UMConfigure.setLogEnabled(false)
            UMConfigure.preInit(app, appKey, channel)
            if (enableCollection) {
                UMConfigure.init(
                    app,
                    appKey,
                    channel,
                    UMConfigure.DEVICE_TYPE_PHONE,
                    null
                )
            }
            initialized = true
        }
    }

    /**
     * After deferred consent: complete Umeng init if [init] was called with enableCollection=false.
     * For simple apps, prefer [init] with enableCollection=true after consent only.
     */
    @JvmStatic
    fun setCollectionEnabled(context: Context, appKey: String, channel: String, enabled: Boolean) {
        if (enabled) {
            UMConfigure.init(
                context.applicationContext,
                appKey,
                channel,
                UMConfigure.DEVICE_TYPE_PHONE,
                null
            )
            initialized = true
        }
    }

    @JvmStatic
    fun setUserId(userId: String) {
        // Umeng account id — best-effort via common profile if available
        try {
            val clazz = Class.forName("com.umeng.analytics.MobclickAgent")
            val method = clazz.getMethod("onProfileSignIn", String::class.java)
            method.invoke(null, userId)
        } catch (_: Throwable) {
            // optional
        }
    }

    @JvmStatic
    fun setCustomKey(key: String, value: String) {
        // No universal Crashlytics-like key store on free U-APM; no-op safe
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Int) {}

    @JvmStatic
    fun setCustomKey(key: String, value: Boolean) {}

    @JvmStatic
    fun log(message: String) {
        // Breadcrumb equivalent not same as Crashlytics on free tier
    }

    /** Non-fatal / custom exception. Console view may require U-APM 专业版. */
    @JvmStatic
    fun recordException(throwable: Throwable) {
        ensureInit()
        UMCrash.generateCustomLog(throwable, "crash_kit")
    }

    private fun ensureInit() {
        check(initialized) {
            "CrashKit.init(context, appKey, channel) must be called first."
        }
    }
}
