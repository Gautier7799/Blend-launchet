package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.domain.AppItem
import com.example.service.BlendAccessibilityService
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
    val dockOpacity: Float = 0.35f,
    val doubleTapToSleep: Boolean = true,
    val dynamicIslandEnabled: Boolean = false,
    val hapticFeedback: Boolean = true,
    val fullscreenMode: Boolean = false,
    val hideDrawerHeader: Boolean = false
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AppRepository(application)
    private val prefs = application.getSharedPreferences("blend_launcher_prefs", Context.MODE_PRIVATE)

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    private val _homeAppPackages = MutableStateFlow<List<String>>(loadHomeAppPackages())
    val homeAppPackages: StateFlow<List<String>> = _homeAppPackages.asStateFlow()

    private val _dockAppPackages = MutableStateFlow<List<String>>(loadDockAppPackages())
    val dockAppPackages: StateFlow<List<String>> = _dockAppPackages.asStateFlow()

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
            dockOpacity = prefs.getFloat("dock_opacity", 0.35f),
            doubleTapToSleep = prefs.getBoolean("double_tap_sleep", true),
            dynamicIslandEnabled = false,
            hapticFeedback = prefs.getBoolean("haptic_feedback", true),
            fullscreenMode = prefs.getBoolean("fullscreen_mode", false),
            hideDrawerHeader = prefs.getBoolean("hide_drawer_header", false)
        )
    }

    fun updateSettings(newSettings: LauncherSettings) {
        _settings.value = newSettings.copy(dynamicIslandEnabled = false)
        prefs.edit()
            .putInt("icon_size", newSettings.iconSizeDp)
            .putBoolean("show_labels", newSettings.showLabels)
            .putInt("grid_columns", newSettings.gridColumns)
            .putInt("dock_count", newSettings.dockCount)
            .putBoolean("themed_icons", newSettings.themedIcons)
            .putFloat("dock_opacity", newSettings.dockOpacity)
            .putBoolean("double_tap_sleep", newSettings.doubleTapToSleep)
            .putBoolean("dynamic_island", false)
            .putBoolean("haptic_feedback", newSettings.hapticFeedback)
            .putBoolean("fullscreen_mode", newSettings.fullscreenMode)
            .putBoolean("hide_drawer_header", newSettings.hideDrawerHeader)
            .apply()
    }

    fun reorderHomeApps(fromIndex: Int, toIndex: Int) {
        val current = _homeAppPackages.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            saveHomeAppPackages(current)
        }
    }

    fun reorderDockApps(fromIndex: Int, toIndex: Int) {
        val current = _dockAppPackages.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            saveDockAppPackages(current)
        }
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        return BlendAccessibilityService.isServiceRunning()
    }

    fun performSleep(context: Context): Boolean {
        val locked = BlendAccessibilityService.lockScreen()
        if (!locked) {
            BlendAccessibilityService.openAccessibilitySettings(context)
        }
        return locked
    }

    private fun loadHomeAppPackages(): List<String> {
        val raw = prefs.getString("home_app_packages", null) ?: return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    private fun saveHomeAppPackages(list: List<String>) {
        _homeAppPackages.value = list
        prefs.edit().putString("home_app_packages", list.joinToString(",")).apply()
    }

    private fun loadDockAppPackages(): List<String> {
        val raw = prefs.getString("dock_app_packages", null) ?: return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    private fun saveDockAppPackages(list: List<String>) {
        _dockAppPackages.value = list
        prefs.edit().putString("dock_app_packages", list.joinToString(",")).apply()
    }

    fun addAppToDock(packageName: String) {
        val current = _dockAppPackages.value.toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            saveDockAppPackages(current)
        }
    }

    fun removeAppFromDock(packageName: String) {
        val current = _dockAppPackages.value.toMutableList()
        if (current.remove(packageName)) {
            saveDockAppPackages(current)
        }
    }

    fun moveAppOnDock(packageName: String, step: Int) {
        val current = _dockAppPackages.value.toMutableList()
        val index = current.indexOf(packageName)
        if (index == -1) return
        val newIndex = index + step
        if (newIndex in 0 until current.size) {
            current.removeAt(index)
            current.add(newIndex, packageName)
            saveDockAppPackages(current)
        }
    }

    fun isAppOnDock(packageName: String): Boolean {
        return _dockAppPackages.value.contains(packageName)
    }

    fun addAppToHome(packageName: String) {
        val current = _homeAppPackages.value.toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            saveHomeAppPackages(current)
        }
    }

    fun removeAppFromHome(packageName: String) {
        val current = _homeAppPackages.value.toMutableList()
        if (current.remove(packageName)) {
            saveHomeAppPackages(current)
        }
    }

    fun moveAppOnHome(packageName: String, step: Int) {
        val current = _homeAppPackages.value.toMutableList()
        val index = current.indexOf(packageName)
        if (index == -1) return
        val newIndex = index + step
        if (newIndex in 0 until current.size) {
            current.removeAt(index)
            current.add(newIndex, packageName)
            saveHomeAppPackages(current)
        }
    }

    fun isAppOnHome(packageName: String): Boolean {
        return _homeAppPackages.value.contains(packageName)
    }

    fun loadApps() {
        viewModelScope.launch {
            val apps = repository.getInstalledApps()
            _installedApps.value = apps

            // If dock apps list was never saved, initialize it with first dockCount apps
            if (_dockAppPackages.value.isEmpty() && apps.isNotEmpty()) {
                val initialDock = apps.take(settings.value.dockCount).map { it.packageName }
                saveDockAppPackages(initialDock)
            }

            // If home apps list was never saved, initialize it with non-dock apps
            if (_homeAppPackages.value.isEmpty() && apps.isNotEmpty()) {
                val dockSet = _dockAppPackages.value.toSet()
                val initialHome = apps.filterNot { dockSet.contains(it.packageName) }
                    .take(settings.value.gridColumns * 3)
                    .map { it.packageName }
                saveHomeAppPackages(initialHome)
            }
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

    fun openAppInfo(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
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
