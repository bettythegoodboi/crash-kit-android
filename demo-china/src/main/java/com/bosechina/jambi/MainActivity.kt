package com.bosechina.jambi

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bettythegoodboi.crashkit.CrashKit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.tvInfo).text = buildString {
            appendLine("CrashKit CHINA (Umeng)")
            appendLine("AppKey: ${BuildConfig.UMENG_APPKEY}")
            appendLine("package=$packageName")
        }

        findViewById<Button>(R.id.btnFatal).setOnClickListener {
            throw RuntimeException("CrashKit china demo fatal")
        }
        findViewById<Button>(R.id.btnNonFatal).setOnClickListener {
            try {
                throw IllegalStateException("CrashKit china demo custom")
            } catch (t: Throwable) {
                CrashKit.recordException(t)
                Toast.makeText(this, "Custom log sent (console may need U-APM paid)", Toast.LENGTH_LONG).show()
            }
        }
    }
}
