package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.domain.AppItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LauncherSettings(
    val iconSizeDp: Int = 60,
    val showLabels: Boolean = true,
    val gridColumns: Int = 4,
    val dockCount: Int = 4,
    val themedIcons: Boolean = false,
    val dockOpacity: Float = 0.35f
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AppRepository(application)
    private val prefs = application.getSharedPreferences("blend_launcher_prefs", Context.MODE_PRIVATE)

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    init {
        loadApps()
    }

    private fun loadSettings(): LauncherSettings {
        return LauncherSettings(
            iconSizeDp = prefs.getInt("icon_size", 60),
            showLabels = prefs.getBoolean("show_labels", true),
            gridColumns = prefs.getInt("grid_columns", 4),
            dockCount = prefs.getInt("dock_count", 4),
            themedIcons = prefs.getBoolean("themed_icons", false),
            dockOpacity = prefs.getFloat("dock_opacity", 0.35f)
        )
    }

    fun updateSettings(newSettings: LauncherSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putInt("icon_size", newSettings.iconSizeDp)
            .putBoolean("show_labels", newSettings.showLabels)
            .putInt("grid_columns", newSettings.gridColumns)
            .putInt("dock_count", newSettings.dockCount)
            .putBoolean("themed_icons", newSettings.themedIcons)
            .putFloat("dock_opacity", newSettings.dockOpacity)
            .apply()
    }

    fun loadApps() {
        viewModelScope.launch {
            _installedApps.value = repository.getInstalledApps()
        }
    }

    fun launchApp(packageName: String) {
        val pm = getApplication<Application>().packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(launchIntent)
        }
    }

    fun openDefaultLauncherSettings(context: Context) {
        val actions = listOf(
            Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS,
            Settings.ACTION_HOME_SETTINGS,
            Settings.ACTION_SETTINGS
        )
        for (action in actions) {
            try {
                val intent = Intent(action).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                break
            } catch (_: Exception) {
                // Try next fallback
            }
        }
    }
}
