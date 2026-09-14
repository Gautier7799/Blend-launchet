package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
            .semantics {
                role = Role.Button
                contentDescription = "Carte batterie : $deviceName, $batteryPercent% ${if (isCharging) ", en charge" else ""}"
            }
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
                        contentDescription = "Téléphone $deviceName",
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
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(darkCircleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCharging) Icons.Default.Bolt else Icons.Default.BatteryChargingFull,
                        contentDescription = if (isCharging) "Batterie en charge" else "Batterie à $batteryPercent%",
                        tint = Color(0xFFD3E8D0),
                        modifier = Modifier.size(14.dp)
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
    val audioManager = remember {
        try {
            context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } catch (_: Exception) {
            null
        }
    }
    var isPlaying by remember { mutableStateOf(audioManager?.isMusicActive == true) }

    fun sendMediaKey(keyCode: Int) {
        try {
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        } catch (_: Exception) {}
    }

    val isDark = isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled"
    val mediaCardBg = if (isDark) Color(0xFF232731).copy(alpha = 0.90f) else Color(0xFFF9FAF7)
    val mediaCardText = if (isDark) Color.White else Color(0xFF1E211E)
    val mediaIconBg = if (isDark) Color(0xFF323846) else Color(0xFFE2E6DF)
    val mediaIconTint = if (isDark) Color.White else Color(0xFF2E332D)
    val playPillBg = if (isDark) Color(0xFF4C6B50) else Color(0xFFD4E8D0)
    val playPillTint = if (isDark) Color.White else Color(0xFF1E211E)
    val nextPillBg = if (isDark) Color(0xFF323846) else Color(0xFFE2E6DF)
    val nextPillTint = if (isDark) Color.White else Color(0xFF1E211E)

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
        color = mediaCardBg,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .semantics {
                role = Role.Button
                contentDescription = "Lecteur de musique Pixel Music"
            }
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
                    .background(mediaIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Musique",
                    tint = mediaIconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = if (isPlaying) "Pixel Music • En lecture" else "Pixel Music",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = mediaCardText,
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
                    sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                    isPlaying = !isPlaying
                },
                shape = RoundedCornerShape(14.dp),
                color = playPillBg,
                modifier = Modifier
                    .size(48.dp)
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Mettre en pause" else "Lire la musique",
                        tint = playPillTint,
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
                    sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                },
                shape = RoundedCornerShape(14.dp),
                color = nextPillBg,
                modifier = Modifier
                    .size(48.dp)
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Piste suivante",
                        tint = nextPillTint,
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

    val isDark = isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled"
    val cardColor = if (isDark) Color(0xFF1E2A20) else Color(0xFFE5F1E2)
    val onCardColor = if (isDark) Color(0xFFE2EEDF) else Color(0xFF1B2E1D)
    val taskCheckTint = if (isDark) Color(0xFF81C995) else Color(0xFF385239)
    val inputFieldBg = if (isDark) Color(0xFF28362B) else Color.White

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
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .semantics {
                        role = Role.Button
                        contentDescription = "Mes tâches, appuyer pour ajouter une tâche"
                    }
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Ajouter une tâche",
                        onClick = { showAddDialog = true }
                    ),
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
                        contentDescription = "Ouvrir l'ajout de tâche",
                        tint = onCardColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
                            focusedContainerColor = inputFieldBg,
                            unfocusedContainerColor = inputFieldBg,
                            focusedTextColor = onCardColor,
                            unfocusedTextColor = onCardColor,
                            focusedBorderColor = taskCheckTint,
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
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Valider l'ajout de la tâche",
                            tint = taskCheckTint
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
                                .defaultMinSize(minHeight = 48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .semantics(mergeDescendants = true) {
                                    role = Role.Checkbox
                                    contentDescription = "${task.text}, ${if (task.isDone) "terminée" else "non terminée"}"
                                }
                                .clickable(
                                    role = Role.Checkbox,
                                    onClickLabel = if (task.isDone) "Marquer non terminée" else "Marquer comme terminée",
                                    onClick = {
                                        if (settings.hapticFeedback) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        onToggleTask(task.id)
                                    }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (task.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (task.isDone) "Tâche terminée" else "Tâche non terminée",
                                tint = if (task.isDone) taskCheckTint else onCardColor.copy(alpha = 0.6f),
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
                                modifier = Modifier
                                    .size(36.dp)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Supprimer la tâche : ${task.text}",
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

    val isDark = isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled"
    val shortcutsCardBg = if (isDark) Color(0xFF232731).copy(alpha = 0.90f) else Color(0xFFF9FAF7)
    val shortcutsCardText = if (isDark) Color.White else Color(0xFF1E211E)
    val seeAllColor = if (isDark) Color(0xFF90B38C) else Color(0xFF385239)
    val iconBoxBg = if (isDark) Color(0xFF323846) else Color.White
    val iconBoxBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E6DF)
    val headerIconBg = if (isDark) Color(0xFF4C6B50) else Color(0xFFD4E8D0)
    val headerIconTint = if (isDark) Color.White else Color(0xFF1E211E)

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = shortcutsCardBg,
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
                            .background(headerIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = headerIconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Raccourcis d'applications",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = shortcutsCardText
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
                        color = seeAllColor
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
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .semantics(mergeDescendants = true) {
                                role = Role.Button
                                contentDescription = "Raccourci ${app.label}"
                            }
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Ouvrir ${app.label}",
                                onClick = {
                                    if (settings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    onAppClick(app.packageName)
                                }
                            )
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(iconBoxBg)
                                .border(
                                    width = 1.dp,
                                    color = iconBoxBorder,
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
                            color = shortcutsCardText,
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
                        contentDescription = "Icône gestion des widgets",
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
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .semantics(mergeDescendants = true) {
                role = Role.Switch
            }
            .clickable(
                role = Role.Switch,
                onClickLabel = if (checked) "Désactiver $title" else "Activer $title",
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 4.dp),
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
