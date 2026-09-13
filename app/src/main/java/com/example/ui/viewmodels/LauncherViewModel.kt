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

data class LauncherTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false
)

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
    val hideDrawerHeader: Boolean = false,
    val showAiWidget: Boolean = true,
    val showDateWidget: Boolean = true,
    val showWeatherWidget: Boolean = true,
    val showBatteryWidget: Boolean = true,
    val showClockWidget: Boolean = false,
    val showTorchWidget: Boolean = false,
    val showSettingsWidget: Boolean = true,
    val showTextShadows: Boolean = false,
    val showDockLines: Boolean = false,
    val showDeviceCardWidget: Boolean = true,
    val showMusicWidget: Boolean = true,
    val showTasksWidget: Boolean = true,
    val showAppShortcutsWidget: Boolean = true
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
            hideDrawerHeader = prefs.getBoolean("hide_drawer_header", false),
            showAiWidget = prefs.getBoolean("show_ai_widget", true),
            showDateWidget = prefs.getBoolean("show_date_widget", true),
            showWeatherWidget = prefs.getBoolean("show_weather_widget", true),
            showBatteryWidget = prefs.getBoolean("show_battery_widget", true),
            showClockWidget = prefs.getBoolean("show_clock_widget", false),
            showTorchWidget = prefs.getBoolean("show_torch_widget", false),
            showSettingsWidget = prefs.getBoolean("show_settings_widget", true),
            showTextShadows = prefs.getBoolean("show_text_shadows", false),
            showDockLines = prefs.getBoolean("show_dock_lines", false),
            showDeviceCardWidget = prefs.getBoolean("show_device_card_widget", true),
            showMusicWidget = prefs.getBoolean("show_music_widget", true),
            showTasksWidget = prefs.getBoolean("show_tasks_widget", true),
            showAppShortcutsWidget = prefs.getBoolean("show_app_shortcuts_widget", true)
        )
    }

    private val _tasks = MutableStateFlow<List<LauncherTask>>(loadTasks())
    val tasks: StateFlow<List<LauncherTask>> = _tasks.asStateFlow()

    private fun loadTasks(): List<LauncherTask> {
        val raw = prefs.getString("home_tasks_list", null) ?: return listOf(
            LauncherTask(text = "Appel important à 15h", isDone = false),
            LauncherTask(text = "Envoyer le rapport", isDone = true)
        )
        return try {
            raw.split(";;;").filter { it.isNotBlank() }.mapNotNull { item ->
                val parts = item.split(":::")
                if (parts.size >= 3) {
                    LauncherTask(id = parts[0], text = parts[1], isDone = parts[2] == "true")
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveTasks(list: List<LauncherTask>) {
        _tasks.value = list
        val serialized = list.joinToString(";;;") { "${it.id}:::${it.text}:::${it.isDone}" }
        prefs.edit().putString("home_tasks_list", serialized).apply()
    }

    fun addTask(text: String) {
        if (text.isNotBlank()) {
            val updated = _tasks.value + LauncherTask(text = text.trim())
            saveTasks(updated)
        }
    }

    fun toggleTask(id: String) {
        val updated = _tasks.value.map {
            if (it.id == id) it.copy(isDone = !it.isDone) else it
        }
        saveTasks(updated)
    }

    fun deleteTask(id: String) {
        val updated = _tasks.value.filter { it.id != id }
        saveTasks(updated)
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
            .putBoolean("show_ai_widget", newSettings.showAiWidget)
            .putBoolean("show_date_widget", newSettings.showDateWidget)
            .putBoolean("show_weather_widget", newSettings.showWeatherWidget)
            .putBoolean("show_battery_widget", newSettings.showBatteryWidget)
            .putBoolean("show_clock_widget", newSettings.showClockWidget)
            .putBoolean("show_torch_widget", newSettings.showTorchWidget)
            .putBoolean("show_settings_widget", newSettings.showSettingsWidget)
            .putBoolean("show_text_shadows", newSettings.showTextShadows)
            .putBoolean("show_dock_lines", newSettings.showDockLines)
            .putBoolean("show_device_card_widget", newSettings.showDeviceCardWidget)
            .putBoolean("show_music_widget", newSettings.showMusicWidget)
            .putBoolean("show_tasks_widget", newSettings.showTasksWidget)
            .putBoolean("show_app_shortcuts_widget", newSettings.showAppShortcutsWidget)
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
