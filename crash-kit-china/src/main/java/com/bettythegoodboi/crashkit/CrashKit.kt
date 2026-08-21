package com.bettythegoodboi.crashkit

import android.content.Context
import androidx.annotation.Keep
import com.umeng.commonsdk.UMConfigure
import com.umeng.umcrash.UMCrash

/**
 * Production CrashKit — China (Umeng U-APM only).
 *
 * Documented surface:
 * - [init] → UMConfigure.preInit + UMConfigure.init (crash capture via apm dependency)
 * - [recordException] → UMCrash.generateCustomLog (console list may require U-APM paid plan)
 *
 * Fatal crashes: uncaught exceptions after init; reopen app so report can upload.
 * Network: device must reach errnewlog.umeng.com (China-oriented).
 *
 * No Crashlytics-style setUserId / log / setCustomKey on this backend.
 */
@Keep
object CrashKit {

    @Volatile
    private var initialized = false

    /**
     * @param appKey Umeng AppKey from owner
     * @param channel Umeng channel string (e.g. official)
     * @param enableCollection if false, only preInit; call again with true after consent, or call [init] only after consent
     */
    @JvmStatic
    @JvmOverloads
    fun init(
        context: Context,
        appKey: String,
        channel: String = "default",
        enableCollection: Boolean = true
    ) {
        if (initialized && enableCollection) return
        synchronized(this) {
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
                initialized = true
            }
        }
    }

    /**
     * Non-fatal / custom exception.
     * Uses UMCrash.generateCustomLog. Viewing in U-APM 自定义异常 may require 专业版.
     */
    @JvmStatic
    fun recordException(throwable: Throwable) {
        check(initialized) {
            "CrashKit.init(context, appKey, channel) must be called first."
        }
        UMCrash.generateCustomLog(throwable, "crash_kit")
    }
}
