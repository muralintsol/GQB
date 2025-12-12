package com.gurukulaboard

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.gurukulaboard.utils.AppInitializer
import com.gurukulaboard.utils.TestDataGenerator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GurukulaBoardApplication : Application() {

    @Inject
    lateinit var appInitializer: AppInitializer
    
    @Inject
    lateinit var testDataGenerator: TestDataGenerator

    override fun onCreate() {
        super.onCreate()
        
        // Initialize test accounts and data after Hilt injection completes
        // Hilt processes @HiltAndroidApp and injects fields during onCreate()
        // Post to ensure all initialization is complete
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (::appInitializer.isInitialized) {
                    appInitializer.initializeTestAccounts()
                }
                if (::testDataGenerator.isInitialized) {
                    testDataGenerator.generateTestQuestions()
                }
            } catch (e: Exception) {
                // Ignore - will be initialized in LoginActivity as fallback
            }
        }, 500) // Small delay to ensure Hilt injection is complete
    }
}

