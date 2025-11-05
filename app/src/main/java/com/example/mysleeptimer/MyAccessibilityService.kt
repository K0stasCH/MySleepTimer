package com.example.mysleeptimer

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent


class MyAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "MyAccessibilityService"
        private var instance: MyAccessibilityService? = null
        fun getSharedInstance(): MyAccessibilityService? {
            return instance
        }
    }

    override fun onServiceConnected() {
        try {
            super.onServiceConnected()
            instance = this
            Log.d(TAG, "Accessibility Service connected.")
        }
        catch (e: Exception) {
            Log.e(TAG, "Error in onServiceConnected: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.i(TAG, "Service destroyed and instance cleared.")
    }

    fun pressBackButton() {
        val success = performGlobalAction(GLOBAL_ACTION_BACK)
        if (success) {
            Log.d(TAG, "Back button pressed successfully.")
        } else {
            Log.e(TAG, "Failed to perform GLOBAL_ACTION_BACK. Service might not be active or permission missing."
            )
        }
    }

    fun pressHomeButton() {
        val success = performGlobalAction(GLOBAL_ACTION_HOME)
        if (success) {
            Log.d(TAG, "Home button pressed successfully.")
        } else {
            Log.e(TAG, "Failed to perform GLOBAL_ACTION_HOME. Service might not be active or permission missing."
            )
        }
    }

    fun screenLock() {
        val success = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        if (success) {
            Log.d(TAG, "Screen locked successfully.")
        } else {
            Log.e(TAG, "Failed to perform GLOBAL_ACTION_LOCK_SCREEN. Service might not be active or permission missing."
            )
        }
    }

    // Run when the screen changes.
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events here
        Log.d(TAG, "New window state detected.")
    }

    override fun onInterrupt() {
        // Handle service interruption here
        Log.w(TAG, "Accessibility Service interrupted. Releasing resources.")
    }
}