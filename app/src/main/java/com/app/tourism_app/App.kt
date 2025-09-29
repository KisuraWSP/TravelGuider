package com.app.tourism_app

import android.app.Application
import android.content.Intent
import android.util.Log
import com.app.tourism_app.activities.CrashHandlerActivity
import java.io.PrintWriter
import java.io.StringWriter

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val trace = sw.toString()
            Log.e("AppCrash", "Uncaught on ${thread.name}\n$trace")

            val intent = Intent(this, CrashHandlerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("stacktrace", trace)
                putExtra("throwable", throwable.message ?: "")
            }
            startActivity(intent)
        }
    }
}
