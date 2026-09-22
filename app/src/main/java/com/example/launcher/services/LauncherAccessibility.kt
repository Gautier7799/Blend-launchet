package com.example.launcher.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class LauncherAccessibility : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun openNotificationPanel() {
        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }
}
