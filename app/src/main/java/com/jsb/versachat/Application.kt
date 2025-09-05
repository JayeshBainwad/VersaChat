package com.jsb.versachat

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VersaChatApplication : Application() {

    companion object {
        private const val TAG = "VersaChatApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VersaChat Application started")

        // Set up global exception handler for debugging
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", exception)
            // In production, you might want to send this to a crash reporting service
            // For now, we'll just log it
        }

        Log.i(TAG, "Application initialization completed")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory called with level: $level")

        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> Log.d(TAG, "UI is hidden, app moved to background")
            TRIM_MEMORY_RUNNING_MODERATE -> Log.d(TAG, "App is running, moderate memory pressure")
            TRIM_MEMORY_RUNNING_LOW -> Log.w(TAG, "App is running, low memory pressure")
            TRIM_MEMORY_RUNNING_CRITICAL -> Log.e(TAG, "App is running, critical memory pressure")
        }
    }
}