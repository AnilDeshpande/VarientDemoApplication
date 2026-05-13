package com.codetutor.varientdemo.diagnostics

import android.os.Build
import com.codetutor.varientdemo.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DiagnosticsImpl : Diagnostics {
    override fun info(): String {
        val tz = TimeZone.getDefault().id
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .apply { timeZone = TimeZone.getDefault() }
            .format(Date())

        return buildString {
            appendLine("Diagnostics (debug)")
            appendLine("• Manufacturer: ${Build.MANUFACTURER}")
            appendLine("• Model: ${Build.MODEL}")
            appendLine("• SDK: ${Build.VERSION.SDK_INT}")
            appendLine("• Device: ${Build.DEVICE}")
            appendLine("• Time: $time ($tz)")
            appendLine("• AppId: ${BuildConfig.APPLICATION_ID}")
            appendLine("• Version: ${BuildConfig.VERSION_NAME}")
        }
    }
}