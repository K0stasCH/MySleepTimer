package com.example.mysleeptimer

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.service.quicksettings.TileService
import android.content.ComponentName
import androidx.core.content.edit

import android.media.AudioManager
import android.view.KeyEvent


/**
 * Worker class that executes the sleep action (e.g., stopping music)
 * after the scheduled delay.
 */
class SleepActionWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    companion object {
        private const val TAG = "SleepActionWorker"
    }

    override fun doWork(): Result {
        Log.i(TAG, "--- Sleep Timer Action Triggered ---")

        sleepSequence()

        Log.i(TAG, "--- Sleep timer work finished ---")

        // Reset the saved duration to OFF_STATE in shared preferences
        applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putInt(PREFS_CURRENT_DURATION, OFF_STATE)
            }

        Log.i(TAG, "Preferences reset to OFF_STATE.")

        // Request the Android system to update the tile UI
        // This forces the system to call onStartListening() on SleepTimerTileService
        TileService.requestListeningState(
            applicationContext,
            ComponentName(applicationContext, SleepTimerTileService::class.java)
        )
        return Result.success()
    }

    fun sleepSequence(){
        // Simulate media key presses (PAUSE)
        Log.i(TAG, "Attempting to stop media playback now!")
        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.let { manager ->
            manager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
            manager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
            Log.i(TAG, "Media PAUSE key events dispatched.")
        } ?: run {
            Log.e(TAG, "Could not get AudioManager service.")
        }

        // Get the singleton instance of your service
        val serviceInstance = MyAccessibilityService.getSharedInstance()
        // Check if the service is running and then call the method
        if (serviceInstance != null) {
            serviceInstance.pressBackButton()
            serviceInstance.pressBackButton()
            serviceInstance.pressHomeButton()
            serviceInstance.screenLock()
        } else {
            Log.e("SleepActionWorker", "MyAccessibilityService instance is not available.")
        }

        deleteNotification(this.applicationContext)


        //**********************************************************
//        serviceInstance?.onServiceConnected()
//        serviceInstance?.onDestroy()
        //**********************************************************
    }
}
