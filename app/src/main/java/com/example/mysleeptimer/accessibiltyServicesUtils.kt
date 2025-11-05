package com.example.mysleeptimer

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.Log


fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
    val service = context.packageName + "/" + serviceClass.canonicalName
    var accessibilityEnabled = 0
    try {
        accessibilityEnabled = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        )
    } catch (e: Settings.SettingNotFoundException) {
        Log.e("ACCESSIBILITY", "Error finding setting: ${e.message}")
    }

    if (accessibilityEnabled == 1) {
        val settingValue = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (settingValue != null) {
            val splitter = TextUtils.SimpleStringSplitter(':')
            splitter.setString(settingValue)
            while (splitter.hasNext()) {
                val enabledService = splitter.next()
                if (enabledService.equals(service, ignoreCase = true)) {
                    Log.i("ACCESSIBILITY", ":) :) Accessibility Service ENABLED")
                    return true
                }
            }
        }
    }
    Log.i("ACCESSIBILITY", ":( :( Accessibility Service DISABLED")
    return false
}