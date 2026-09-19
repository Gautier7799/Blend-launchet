package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onReorderWidget: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onDeleteWidget: (widgetKey: String) -> Unit = {},
    onOpenManageWidgets: () -> Unit = {},
    onResetWidgets: () -> Unit = {},
    onUpdateSettings: (LauncherSettings) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()

    var isEditMode by remember { mutableStateOf(false) }

    // Gentle iOS Jiggle Animation during Edit Mode
    val infiniteTransition = rememberInfiniteTransition(label = "widget_wobble")
    val wobbleAngle by infiniteTransition.animateFloat(
        initialValue = -0.8f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "widget_wobble_angle"
    )

    // Build the ordered list of currently visible widgets based on user configuration
    data class ActiveWidgetItem(
        val key: String,
        val title: String,
        val composable: @Composable () -> Unit
    )

    val activeWidgets = remember(settings, tasks, apps) {
        val list = mutableListOf<ActiveWidgetItem>()
        for (key in settings.widgetOrder) {
            when (key) {
                "battery" -> {
                    if (settings.showDeviceCardWidget) {
                        list.add(
                            ActiveWidgetItem(
                                key = "battery",
                                title = "Batterie & Appareil",
                                composable = { DeviceBatteryCard(settings = settings, modifier = Modifier.fillMaxWidth()) }
                            )
                        )
                    }
                }
                "music" -> {
                    if (settings.showMusicWidget) {
                        list.add(
                            ActiveWidgetItem(
                                key = "music",
                                title = "Lecteur Musique",
                                composable = { MusicPlayerCard(settings = settings, modifier = Modifier.fillMaxWidth()) }
                            )
                        )
                    }
                }
                "tasks" -> {
                    if (settings.showTasksWidget) {
                        list.add(
                            ActiveWidgetItem(
                                key = "tasks",
                                title = "Tâches & Notes",
                                composable = {
                                    TasksCard(
                                        tasks = tasks,
                                        onAddTask = onAddTask,
                                        onToggleTask = onToggleTask,
                                        onDeleteTask = onDeleteTask,
                                        settings = settings,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                        )
                    }
                }
                "shortcuts" -> {
                    if (settings.showAppShortcutsWidget) {
                        list.add(
                            ActiveWidgetItem(
                                key = "shortcuts",
                                title = "Raccourcis Apps",
                                composable = {
                                    AppShortcutsCard(
                                        apps = apps,
                                        settings = settings,
                                        onAppClick = onAppClick,
                                        onOpenDrawer = onOpenDrawer,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                        )
                    }
                }
                "controls" -> {
                    if (settings.showQuickControlsWidget) {
                        list.add(
                            ActiveWidgetItem(
                                key = "controls",
                                title = "Centre de contrôle iOS",
                                composable = { QuickControlsCard(settings = settings, modifier = Modifier.fillMaxWidth()) }
                            )
                        )
                    }
                }
                "weather_glance" -> {
                    if (settings.showWeatherGlanceWidget) {
                        list.add(
                            ActiveWidgetItem(
                                key = "weather_glance",
                                title = "Radar Météo",
                                composable = { WeatherGlanceCard(settings = settings, modifier = Modifier.fillMaxWidth()) }
                            )
                        )
                    }
                }
            }
        }
        list
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = if (settings.fullscreenMode) 24.dp else 52.dp,
            bottom = 48.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. iOS 27 Widgets Dashboard Header with Restore, Add & Edit Controls (Search bar removed)
        item(key = "widgets_dashboard_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Widgets iOS 27",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (isEditMode) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Modification",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isEditMode) "Glissez pour magnétiser ou touchez (-) pour supprimer" else "Appui long ou touchez Modifier pour réorganiser",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isEditMode) {
                        // "Done" Button
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isEditMode = false
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Terminé", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // 1. Restore Default Widgets Icon Button (Moved to Top as requested)
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onResetWidgets()
                            },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Restaurer les widgets par défaut",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // 2. Add / Manage Widgets Icon Button
                        Surface(
                            onClick = onOpenManageWidgets,
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ajouter un widget",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 3. Edit / Reorder Button
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isEditMode = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Modifier l'ordre des widgets",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Modifier",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1.5. iOS 17 Pair / Quad Battery Widgets (Weather 29° + Battery Ring) in Secondary Screen
        if (settings.showIosHomeWidgets && (settings.widgetPlacement == "secondary" || settings.widgetPlacement == "both")) {
            item(key = "ios_home_widgets_row") {
                IosHomeWidgetsRow(
                    settings = settings,
                    onOpenSettings = onSettingsClick,
                    isEditMode = isEditMode,
                    onToggleEditMode = { isEditMode = !isEditMode },
                    onDeleteWidget = { onDeleteWidget("ios_home_widgets") },
                    onToggleStyle = {
                        val nextStyle = if (settings.iosWidgetStyle == "pair") "quad_battery" else "pair"
                        onUpdateSettings(settings.copy(iosWidgetStyle = nextStyle))
                    },
                    onLowerWidget = {
                        onUpdateSettings(settings.copy(widgetTopSpacingDp = (settings.widgetTopSpacingDp + 8).coerceIn(8, 70)))
                    },
                    onRaiseWidget = {
                        onUpdateSettings(settings.copy(widgetTopSpacingDp = (settings.widgetTopSpacingDp - 8).coerceIn(8, 70)))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. Dynamically Ordered Reorderable & Deletable Widgets with Magnetic Snapping
        itemsIndexed(
            items = activeWidgets,
            key = { _, item -> item.key }
        ) { index, item ->
            ReorderableWidgetWrapper(
                widgetKey = item.key,
                title = item.title,
                index = index,
                totalCount = activeWidgets.size,
                isEditMode = isEditMode,
                wobbleAngle = wobbleAngle,
                onDelete = { onDeleteWidget(item.key) },
                onMoveUp = {
                    if (index > 0) {
                        onReorderWidget(index, index - 1)
                    }
                },
                onMoveDown = {
                    if (index < activeWidgets.size - 1) {
                        onReorderWidget(index, index + 1)
                    }
                },
                onLongPress = {
                    isEditMode = true
                }
            ) {
                item.composable()
            }
        }

        // 3. Optional Empty State if all widgets are deleted
        if (activeWidgets.isEmpty()) {
            item(key = "empty_widgets_notice") {
                Surface(
                    onClick = onResetWidgets,
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White.copy(alpha = if (isDark) 0.08f else 0.18f),
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = if (isDark) 0.20f else 0.40f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Aucun widget • Toucher pour restaurer",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
