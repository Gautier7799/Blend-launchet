package com.example.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveNotificationModel(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val isClearable: Boolean,
    val pendingIntent: PendingIntent? = null
)

class BlendNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        updateActiveCounts()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) {
            instance = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateActiveCounts()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateActiveCounts()
    }

    private fun updateActiveCounts() {
        try {
            val notifications = activeNotifications ?: emptyArray()
            val counts = mutableMapOf<String, Int>()
            val list = mutableListOf<ActiveNotificationModel>()
            val pm = packageManager
            for (sbn in notifications) {
                // Ignore ongoing/sticky notifications like media players or foreground services if clearable
                if (sbn != null && sbn.isClearable) {
                    val pkg = sbn.packageName
                    if (!pkg.isNullOrEmpty() && pkg != packageName) {
                        counts[pkg] = (counts[pkg] ?: 0) + 1
                        val extras = sbn.notification?.extras
                        val title = extras?.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
                        val text = extras?.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
                            ?: extras?.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString()
                            ?: ""
                        val appLabel = try {
                            val ai = pm.getApplicationInfo(pkg, 0)
                            pm.getApplicationLabel(ai).toString()
                        } catch (_: Exception) {
                            pkg
                        }
                        if (title.isNotEmpty() || text.isNotEmpty()) {
                            list.add(
                                ActiveNotificationModel(
                                    key = sbn.key,
                                    packageName = pkg,
                                    appName = appLabel,
                                    title = title,
                                    text = text,
                                    postTime = sbn.postTime,
                                    isClearable = sbn.isClearable,
                                    pendingIntent = sbn.notification?.contentIntent
                                )
                            )
                        }
                    }
                }
            }
            _notificationCounts.value = counts
            _activeNotifications.value = list.sortedByDescending { it.postTime }.take(25)
        } catch (_: Exception) {
        }
    }

    companion object {
        private var instance: BlendNotificationListenerService? = null
        private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val notificationCounts: StateFlow<Map<String, Int>> = _notificationCounts.asStateFlow()

        private val _activeNotifications = MutableStateFlow<List<ActiveNotificationModel>>(emptyList())
        val activeNotificationsList: StateFlow<List<ActiveNotificationModel>> = _activeNotifications.asStateFlow()

        fun isConnected(): Boolean {
            return instance != null
        }

        fun cancelNotificationByKey(key: String) {
            try {
                instance?.cancelNotification(key)
                instance?.updateActiveCounts()
            } catch (_: Exception) {}
        }

        fun cancelAllActiveNotifications() {
            try {
                instance?.cancelAllNotifications()
                instance?.updateActiveCounts()
            } catch (_: Exception) {}
        }

        fun isNotificationAccessGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":").toTypedArray()
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null && TextUtils.equals(packageName, cn.packageName)) {
                        return true
                    }
                }
            }
            return false
        }

        fun openNotificationAccessSettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallback)
                } catch (_: Exception) {
                }
            }
        }
    }
}
