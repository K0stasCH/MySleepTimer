package com.example.mysleeptimer

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.content.Context
import android.os.Build
import android.util.Log
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings



// Use a unique ID for the Notification Channel (required on Android 8.0+)
private const val CHANNEL_ID = "MySleepTimer"
private const val CHANNEL_NAME = "Timer Notifications" //Settings > Apps > Your App > Notifications"
private const val CHANNEL_DESCRIPTION = "Notifications for when the timer triggers"

// Use a unique integer ID for the Notification itself.
// This ID is CRITICAL for canceling / editing (deleting) the notification later.
private const val NOTIFICATION_ID = 101

private  const val TAG = "NotificationUtils"


fun createNotificationChannel(context: Context) {
    Log.d(TAG, "Creating Notification Channel")
    // Check if the device is running Android 8.0 (API 26) or higher
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * Step 2: CREATE and SHOW THE NOTIFICATION
 * Uses NotificationCompat.Builder for backward compatibility.
 */
fun showTimerNotification(context: Context, durationMinutes: Long) {
    // Build the notification content.
    val offTime = calculateFutureTimeFromNow(durationMinutes)
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.clock_icon) // Mandatory: Replace with your icon drawable.
        .setContentTitle("It will triggered on $offTime")
        .setContentText("This notification will be deleted by an in-app trigger.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        // setAutoCancel(false) makes the notification persist. It won't be dismissed when the user taps it.
        // It will only be removed by our app calling cancel() or by the user swiping it away.
        .setAutoCancel(false)
        .setOngoing(true)

    // Get the NotificationManagerCompat instance.
    // 'with' is a concise way to call notify on the manager.
    with(NotificationManagerCompat.from(context)) {
        // Show the notification. The NOTIFICATION_ID is crucial for updating or canceling it later.
        notify(NOTIFICATION_ID, builder.build())
    }
    Log.d(TAG, "Show Timer Notification")
}

fun showAccessibilityNotification(context: Context) {
    // 1. Define the action: Open Accessibility Settings
    val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        // Set necessary flags for starting an Activity from a Service
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    // 2. Create a PendingIntent to execute the action when the notification is tapped
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        settingsIntent,
        PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE for modern Android versions
    )

    // 3. Build the Notification
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.clock_icon) // Use a proper icon
        .setContentTitle("Permission Required")
        .setContentText("Tap to enable 'MySleepTimer' Accessibility Service.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent) // Set the intent to run when tapped
        .setAutoCancel(true) // Automatically removes the notification when tapped
        .build()

    // 4. Show the Notification
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(NOTIFICATION_ID, notification)

    Log.d(TAG, "Show Accessibility Notification")
}


/**
 * Step 3: DELETE (CANCEL) THE NOTIFICATION
 * This function will remove the notification from the status bar.
 */
fun deleteNotification(context: Context) {
    Log.d(TAG, "Deleting Notification")
    val notificationManager = NotificationManagerCompat.from(context)
    // To cancel the notification, you use the same ID that you used to create it.
    notificationManager.cancel(NOTIFICATION_ID)
}

fun calculateFutureTimeFromNow(xMinutes: Long): String {
    // 1. Get the device's local time zone ID.
    val localZone = ZoneId.systemDefault()
    // 2. Get the current date and time in the device's time zone.
    val now = ZonedDateTime.now(localZone)
    // 3. Add the specified number of minutes (X).
    val futureTime = now.plusMinutes(xMinutes)
    // 4. Define a clear formatter for the output.
    val formatter = DateTimeFormatter.ofPattern("EEEE 'at' HH:mm", Locale.getDefault())
    // 5. Return the formatted future time string.
    return futureTime.format(formatter)
}