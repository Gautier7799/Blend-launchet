package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.AppItem
import com.example.ui.viewmodels.LauncherSettings
import com.example.ui.viewmodels.LauncherTask

/**
 * 1. Device & Battery Widget Card (Pixel 8 Style)
 */
@Composable
fun DeviceBatteryCard(
    settings: LauncherSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var batteryPercent by remember { mutableIntStateOf(85) }
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val status = context.registerReceiver(null, filter)
            val level = status?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = status?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val chargeState = status?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = chargeState == BatteryManager.BATTERY_STATUS_CHARGING || chargeState == BatteryManager.BATTERY_STATUS_FULL
            if (level >= 0 && scale > 0) {
                batteryPercent = (level * 100) / scale
            }
        } catch (_: Exception) {}
    }

    val deviceName = remember {
        val model = Build.MODEL
        if (model.isNotBlank()) model else "Pixel 8"
    }

    // Modern M3 Soft Green/Pistachio Palette matching screenshot
    val cardColor = Color(0xFFD3E8D0)
    val onCardColor = Color(0xFF1B2E1D)
    val darkCircleColor = Color(0xFF132314)
    val progressActiveColor = Color(0xFF90B38C)

    Surface(
        onClick = {
            if (settings.hapticFeedback) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            try {
                val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        },
        shape = RoundedCornerShape(26.dp),
        color = cardColor,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Visual battery fill indicator
            val fraction = (batteryPercent / 100f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(progressActiveColor.copy(alpha = 0.35f))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = onCardColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = deviceName,
                            color = onCardColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$batteryPercent% ${if (isCharging) "• En charge" else ""}",
                            color = onCardColor.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(26.dp) // Shrunk from 34.dp
                        .clip(CircleShape)
                        .background(darkCircleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCharging) Icons.Default.Bolt else Icons.Default.BatteryChargingFull,
                        contentDescription = "Charge",
                        tint = Color(0xFFD3E8D0),
                        modifier = Modifier.size(14.dp) // Shrunk from 19.dp
                    )
                }
            }
        }
    }
}

/**
 * 2. Music Player Widget Card (Pixel Music Style)
 */
@Composable
fun MusicPlayerCard(
    settings: LauncherSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isPlaying by remember { mutableStateOf(false) }

    Surface(
        onClick = {
            if (settings.hapticFeedback) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            try {
                val intent = Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        },
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFF9FAF7),
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Note icon in circular pill
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E6DF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF2E332D),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = if (isPlaying) "Pixel Music • Lecture" else "Pixel Music",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E211E),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Play / Pause pill button
            Surface(
                onClick = {
                    if (settings.hapticFeedback) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    isPlaying = !isPlaying
                },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFD4E8D0),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Lecture",
                        tint = Color(0xFF1E211E),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Next button pill
            Surface(
                onClick = {
                    if (settings.hapticFeedback) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE2E6DF),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Suivant",
                        tint = Color(0xFF1E211E),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * 3. Quick Tasks Widget Card (Mes tâches)
 */
@Composable
fun TasksCard(
    tasks: List<LauncherTask>,
    onAddTask: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    settings: LauncherSettings,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskText by remember { mutableStateOf("") }

    val cardColor = Color(0xFFE5F1E2)
    val onCardColor = Color(0xFF1B2E1D)

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = cardColor,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Header Row: "Mes tâches"
            Row(
                modifier = Modifier.fillMaxWidth().clickable { showAddDialog = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Mes tâches",
                        color = onCardColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = onCardColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Removed the green '+' button as per request, tapping the header now opens the add dialog.
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inline Add Task Input
            AnimatedVisibility(visible = showAddDialog) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("Nouvelle tâche...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF385239),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newTaskText.isNotBlank()) {
                                    onAddTask(newTaskText)
                                    newTaskText = ""
                                    showAddDialog = false
                                }
                            }
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                onAddTask(newTaskText)
                                newTaskText = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Valider",
                            tint = Color(0xFF385239)
                        )
                    }
                }
            }

            // Tasks List or Empty State
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune tâche",
                        color = onCardColor.copy(alpha = 0.85f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.take(4).forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (settings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onToggleTask(task.id)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (task.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (task.isDone) Color(0xFF385239) else onCardColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = task.text,
                                color = if (task.isDone) onCardColor.copy(alpha = 0.45f) else onCardColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (settings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    onDeleteTask(task.id)
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Supprimer",
                                    tint = onCardColor.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. App Shortcuts Widget Card (اختصارات التطبيقات المتوفرة)
 * Matches the user request: "أضف widget الواجهة يمكن اضافتها على الواجعة الرئيسية فيها اختصرات التطبيقات المتوفرة"
 */
@Composable
fun AppShortcutsCard(
    apps: List<AppItem>,
    settings: LauncherSettings,
    onAppClick: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Pick top available apps or essential system tools
    val shortcutApps = remember(apps) {
        if (apps.isEmpty()) emptyList()
        else {
            val preferred = apps.filter { app ->
                val p = app.packageName.lowercase()
                p.contains("dialer") || p.contains("phone") ||
                p.contains("message") || p.contains("camera") ||
                p.contains("chrome") || p.contains("browser") ||
                p.contains("gallery") || p.contains("whatsapp")
            }
            if (preferred.size >= 4) preferred.take(6)
            else apps.take(6)
        }
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFFF9FAF7),
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD4E8D0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = Color(0xFF1E211E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Raccourcis d'applications",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E211E)
                    )
                }

                TextButton(
                    onClick = onOpenDrawer,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Voir tout",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF385239)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row of quick app shortcuts with real icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                shortcutApps.forEach { app ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (settings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onAppClick(app.packageName)
                            }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFE2E6DF),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (app.iconBitmap != null) {
                                Image(
                                    bitmap = app.iconBitmap,
                                    contentDescription = app.label,
                                    modifier = Modifier.size(34.dp)
                                )
                            } else {
                                AsyncImage(
                                    model = app.icon,
                                    contentDescription = app.label,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = app.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E211E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 52.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5. Add / Manage Home Widgets Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHomeWidgetsBottomSheet(
    settings: LauncherSettings,
    onUpdateSettings: (LauncherSettings) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD4E8D0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = Color(0xFF1E211E),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Gérer les widgets d'accueil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    WidgetToggleRow(
                        title = "Raccourcis d'applications",
                        subtitle = "Affiche la barre d'accès rapide aux applications favorites",
                        checked = settings.showAppShortcutsWidget,
                        onCheckedChange = { onUpdateSettings(settings.copy(showAppShortcutsWidget = it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    WidgetToggleRow(
                        title = "Batterie du téléphone (Pixel style)",
                        subtitle = "Affiche la carte de l'état de la batterie et de la charge",
                        checked = settings.showDeviceCardWidget,
                        onCheckedChange = { onUpdateSettings(settings.copy(showDeviceCardWidget = it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    WidgetToggleRow(
                        title = "Lecteur de musique (Pixel Music)",
                        subtitle = "Contrôle rapide de la lecture et piste suivante",
                        checked = settings.showMusicWidget,
                        onCheckedChange = { onUpdateSettings(settings.copy(showMusicWidget = it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    WidgetToggleRow(
                        title = "Mes tâches & Notes rapides",
                        subtitle = "Liste de tâches avec cases à cocher et ajout rapide",
                        checked = settings.showTasksWidget,
                        onCheckedChange = { onUpdateSettings(settings.copy(showTasksWidget = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
