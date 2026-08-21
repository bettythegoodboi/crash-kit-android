package com.bettythegoodboi.crashdemo

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
        findViewById<TextView>(R.id.tvInfo).text =
            "CrashKit GLOBAL (Firebase)\npackage=$packageName"

        findViewById<Button>(R.id.btnFatal).setOnClickListener {
            CrashKit.log("before fatal")
            throw RuntimeException("CrashKit global demo fatal")
        }
        findViewById<Button>(R.id.btnNonFatal).setOnClickListener {
            try {
                throw IllegalStateException("CrashKit global demo non-fatal")
            } catch (t: Throwable) {
                CrashKit.recordException(t)
                Toast.makeText(this, "Non-fatal sent", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
