package com.example.mysleeptimer

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

import android.util.Log
import java.util.concurrent.TimeUnit
import androidx.core.content.edit


class SleepTimerTileService : TileService() {

    private val DURATION_CYCLE_MINUTES = listOf(1, 10, 30, 60, 90, 120) // Cycle values in minutes

    private fun getCurrentDuration(): Int {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(
            PREFS_CURRENT_DURATION,
            OFF_STATE
        )
    }

    private fun saveDuration(duration: Int) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
            putInt(
                PREFS_CURRENT_DURATION,
                duration
            )
        }
    }
    override fun onTileAdded(){
        super.onTileAdded()
        createNotificationChannel(this)

    }
    // Called when the tile becomes visible in the Quick Settings panel
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(getCurrentDuration())
    }

    // Called when the tile is no longer visible in the Quick Settings panel
    override fun onStopListening() {
        super.onStopListening()
    }

    // Called when the user taps the tile
    override fun onClick() {
        super.onClick()

        // This is important for some older Android versions:
        // If your app is not active, you may need to call unlockAndRun
        // if the tile service requires a running activity to function.
        if (!isLocked) {
            // Logic when the device is unlocked
            processTileClick()
        } else {
            // Logic when the device is locked
            unlockAndRun {
                processTileClick()
            }
        }
    }

    private fun processTileClick() {
        val currentDuration = getCurrentDuration()
        var nextDuration = OFF_STATE

        if (currentDuration == OFF_STATE) {
            // Off -> Start of the cycle (10 min)
            nextDuration = DURATION_CYCLE_MINUTES.first()
        } else {
            // Find current index and move to next
            val currentIndex = DURATION_CYCLE_MINUTES.indexOf(currentDuration)
            if (currentIndex < DURATION_CYCLE_MINUTES.lastIndex) {
                nextDuration = DURATION_CYCLE_MINUTES[currentIndex + 1]
            }
            // If at the end of the list (60 min), nextDuration remains OFF_STATE (0)
        }

        if (nextDuration != OFF_STATE) {
            // A duration is set, start the timer
            startSleepTimer(nextDuration.toLong())
        } else {
            // State is OFF, cancel any existing timer
            cancelSleepTimer()
        }

        saveDuration(nextDuration)
        updateTileState(nextDuration)
    }

    // --- Helper Functions for Tile Management ---

    private fun updateTileState(duration: Int) {
        qsTile?.apply {
            if (duration != OFF_STATE) {
                state = Tile.STATE_ACTIVE
                label = "$duration Min"
            } else {
                state = Tile.STATE_INACTIVE
                label = "Sleep Timer - OFF"
            }
            updateTile()
        }
    }

    // --- WorkManager Scheduling Logic ---

    /**
     * Schedules the sleep timer using WorkManager.
     * @param durationMinutes The time in minutes until the SleepActionWorker should run.
     */
    private fun startSleepTimer(durationMinutes: Long) {
        showNotification(this, durationMinutes)
        // 1. Define the work request
        val sleepWorkRequest = OneTimeWorkRequestBuilder<SleepActionWorker>()
            .setInitialDelay(durationMinutes, TimeUnit.MINUTES)
            .build()

        // 2. Enqueue the work with a unique name
        // ExistingWorkPolicy.REPLACE ensures that if the user taps the tile again (e.g., changes 10 min to 30 min),
        // the old timer is cancelled and replaced by the new one.
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            sleepWorkRequest
        )
        Log.i("SleepTimer", "Timer set for $durationMinutes minutes using WorkManager.")
    }


    //Cancels any currently scheduled sleep timer work.
    private fun cancelSleepTimer() {
        // Cancel the job using the unique WORK_NAME
        WorkManager.getInstance(applicationContext).cancelUniqueWork(WORK_NAME)
        Log.i("SleepTimer", "Sleep timer cancelled.")
    }

}