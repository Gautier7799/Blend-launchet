package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class BlendAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        private var instance: BlendAccessibilityService? = null

        fun isServiceRunning(): Boolean {
            return instance != null
        }

        fun lockScreen(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                instance?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) ?: false
            } else {
                false
            }
        }

        fun expandNotificationPanel(context: Context): Boolean {
            // Priority 1: Instant smooth system action via accessibility service
            val accessibilitySuccess = instance?.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) ?: false
            if (accessibilitySuccess) return true

            // Priority 2: Direct StatusBarManager reflection fallback
            return try {
                val sbservice = context.getSystemService("statusbar")
                val statusbarManager = Class.forName("android.app.StatusBarManager")
                val showsb = statusbarManager.getMethod("expandNotificationsPanel")
                showsb.invoke(sbservice)
                true
            } catch (_: Exception) {
                try {
                    val statusbarManager = Class.forName("android.app.StatusBarManager")
                    val showsb = statusbarManager.getMethod("expand")
                    showsb.invoke(context.getSystemService("statusbar"))
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }

        fun openQuickSettings(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS) ?: false
        }

        fun openRecentApps(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_RECENTS) ?: false
        }

        fun openAccessibilitySettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }
}
