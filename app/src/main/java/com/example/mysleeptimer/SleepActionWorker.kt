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

    override fun doWork(): Result {
        Log.i("SleepActionWorker", "--- Sleep Timer Action Triggered ---")

        Log.i("SleepActionWorker", "Attempting to stop media playback now!")
        // Simulate media key presses (PAUSE)
        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.let { manager ->
            manager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
            manager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
            Log.i("SleepActionWorker", "Media PAUSE key events dispatched.")
        } ?: run {
            Log.e("SleepActionWorker", "Could not get AudioManager service.")
        }

        Log.i("SleepActionWorker", "Sleep timer work finished.")

        // Reset the saved duration to OFF_STATE in shared preferences
        applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putInt(PREFS_CURRENT_DURATION, OFF_STATE)
            }

        Log.i("SleepActionWorker", "Preferences reset to OFF_STATE.")

        // Request the Android system to update the tile UI
        // This forces the system to call onStartListening() on SleepTimerTileService
        TileService.requestListeningState(
            applicationContext,
            ComponentName(applicationContext, SleepTimerTileService::class.java)
        )

        return Result.success()
    }
}
