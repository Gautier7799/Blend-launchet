package com.example

import android.app.Application
import android.content.Intent
import android.util.Log
import com.example.di.AppContainer
import com.example.ui.screens.crash.CrashActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.PrintWriter
import java.io.StringWriter

class DocCloudApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        
        // إعداد صائد الأخطاء العام (Crash Handler)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            handleUncaughtException(thread, exception, defaultHandler)
        }

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("dummy-project")
                    .setApplicationId("1:1032483011322:android:552ab8671607cb3e0e71ce")
                    .setApiKey("dummy-api-key-so-app-does-not-crash")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            Log.e("DocCloudApplication", "Failed to initialize Firebase", e)
        }
        container = AppContainer()
    }

    private fun handleUncaughtException(
        thread: Thread,
        exception: Throwable,
        defaultHandler: Thread.UncaughtExceptionHandler?
    ) {
        val stringWriter = StringWriter()
        exception.printStackTrace(PrintWriter(stringWriter))
        val stackTrace = stringWriter.toString()

        Log.e("DocCloudApplication", "Uncaught Exception: $stackTrace")

        val intent = Intent(this, CrashActivity::class.java).apply {
            putExtra("EXTRA_ERROR_DETAILS", stackTrace)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        
        startActivity(intent)

        // إنهاء العملية بشكل كامل
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(1)
    }
}
