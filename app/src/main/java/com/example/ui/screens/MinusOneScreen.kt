package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
            bottom = 180.dp // Ample clearance to guarantee no dock overlap in any operation
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Google Web Search Bar
        item(key = "google_search_bar") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        contentDescription = "Rechercher sur Google"
                    }
                    .clickable { 
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Rechercher sur le Web & Google...",
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                }
            }
        }

        // 2. Compact Top Widgets Bar (Date, Weather, Clock, Battery)
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

        // 4. Google News Section Header
        item(key = "news_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Actualités",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://news.google.com")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                ) {
                    Text(
                        text = "Voir tout",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 5. Google News Feed Items
        items(5, key = { "news_item_$it" }) { index ->
            NewsCard(index = index)
        }
    }
}

@Composable
fun NewsCard(index: Int) {
    val context = LocalContext.current
    val titles = listOf(
        "Découvrez les dernières nouveautés d'Android 15 pour votre téléphone",
        "L'Intelligence Artificielle transforme notre façon d'interagir avec les applications",
        "Les 10 astuces indispensables pour optimiser la batterie de votre smartphone",
        "Nouveaux modèles de téléphones : ce qu'il faut attendre cette année",
        "Comment la réalité augmentée s'intègre dans la vie quotidienne"
    )
    val sources = listOf("TechRadar", "Le Monde Tech", "FrAndroid", "01net", "Numerama")
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://news.google.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titles.getOrElse(index) { "Actualité..." },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${sources.getOrElse(index) { "Google News" }} • Il y a 2h",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
