package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AppItem
import com.example.ui.viewmodels.LauncherSettings
import com.example.ui.viewmodels.LauncherTask

@Composable
fun MinusOneScreen(
    settings: LauncherSettings,
    tasks: List<LauncherTask> = emptyList(),
    apps: List<AppItem> = emptyList(),
    onAddTask: (String) -> Unit = {},
    onToggleTask: (String) -> Unit = {},
    onDeleteTask: (String) -> Unit = {},
    onAppClick: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onAiClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = if (settings.fullscreenMode) 24.dp else 52.dp,
            bottom = 220.dp // Ample clearance to guarantee no dock overlap in any operation
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Widgets Bar (Date, Weather, Clock, Battery)
        if (settings.showWeatherWidget || settings.showDateWidget || settings.showClockWidget || settings.showBatteryWidget) {
            item(key = "top_widgets_bar") {
                TopWidgetsBar(
                    settings = settings,
                    onAiClick = onAiClick,
                    onSettingsClick = onSettingsClick
                )
            }
        }

        // 3. Automated Dashboard Widgets (Battery, Music, Tasks, App Shortcuts)
        if (settings.showDeviceCardWidget) {
            item(key = "device_battery_card") {
                DeviceBatteryCard(
                    settings = settings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (settings.showMusicWidget) {
            item(key = "music_player_card") {
                MusicPlayerCard(
                    settings = settings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (settings.showTasksWidget) {
            item(key = "tasks_card") {
                TasksCard(
                    tasks = tasks,
                    onAddTask = onAddTask,
                    onToggleTask = onToggleTask,
                    onDeleteTask = onDeleteTask,
                    settings = settings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (settings.showAppShortcutsWidget) {
            item(key = "app_shortcuts_card") {
                AppShortcutsCard(
                    apps = apps,
                    settings = settings,
                    onAppClick = onAppClick,
                    onOpenDrawer = onOpenDrawer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

    }
}

