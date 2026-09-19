package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
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
 * iOS 27 Flagship Reorderable & Deletable Widget Container
 * Supports touch dragging, magnetic snapping into position, deletion badges, and accessible controls.
 */
@Composable
fun ReorderableWidgetWrapper(
    widgetKey: String,
    title: String,
    index: Int,
    totalCount: Int,
    isEditMode: Boolean,
    wobbleAngle: Float,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }
    var visualOffsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val animatedOffsetY by animateFloatAsState(
        targetValue = visualOffsetY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "widget_drag_y"
    )
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1f,
        label = "widget_drag_scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 16.dp else 0.dp,
        label = "widget_drag_elev"
    )

    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
            .scale(scale)
            .shadow(elevation, shape = RoundedCornerShape(26.dp))
            .rotate(if (isEditMode && !isDragging) wobbleAngle else 0f)
            .pointerInput(isEditMode, index, totalCount) {
                if (isEditMode) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            accumulatedDragY = 0f
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDragY += dragAmount.y
                            visualOffsetY += dragAmount.y

                            val snapThreshold = 75f
                            if (accumulatedDragY > snapThreshold && index < totalCount - 1) {
                                // Magnetic snap downwards
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                accumulatedDragY = 0f
                                visualOffsetY -= snapThreshold
                                onMoveDown()
                            } else if (accumulatedDragY < -snapThreshold && index > 0) {
                                // Magnetic snap upwards
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                accumulatedDragY = 0f
                                visualOffsetY += snapThreshold
                                onMoveUp()
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            accumulatedDragY = 0f
                            visualOffsetY = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            accumulatedDragY = 0f
                            visualOffsetY = 0f
                        }
                    )
                } else {
                    detectTapGestures(
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        }
                    )
                }
            }
    ) {
        // Main Widget Content
        content()

        // iOS Style Delete Badge & Drag/Reorder Handles in Edit Mode
        if (isEditMode) {
            // Delete Badge (-) at Top End
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-8).dp)
                    .size(48.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Supprimer le widget $title"
                    }
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Supprimer $title",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEF4444),
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Quick Up/Down Reorder Pills & Drag Grip indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 8.dp, y = (-8).dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f),
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    // Move Up
                    if (index > 0) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onMoveUp()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Déplacer $title vers le haut",
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Drag Indicator
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Glisser pour réorganiser",
                        tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(16.dp)
                    )

                    // Move Down
                    if (index < totalCount - 1) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onMoveDown()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Déplacer $title vers le bas",
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. iOS 27 Flagship Quick Controls Capsule Widget
 * Interactive control center module featuring Torch, Sound mode, Wi-Fi, and Bluetooth shortcuts.
 */
@Composable
fun QuickControlsCard(
    settings: LauncherSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Torch / Flashlight state
    val cameraManager = remember {
        try {
            context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        } catch (_: Exception) {
            null
        }
    }
    var isTorchOn by remember { mutableStateOf(false) }

    // Sound / Ringer state
    val audioManager = remember {
        try {
            context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } catch (_: Exception) {
            null
        }
    }
    var ringerMode by remember {
        mutableIntStateOf(audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL)
    }

    val cardBg = if (isDark) Color(0xFF1E222A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val textPrimary = if (isDark) Color.White else Color(0xFF1E211E)
    val textSecondary = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF49454F)

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Centre de contrôle iOS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Raccourcis rapides & matériel",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
                ) {
                    Text(
                        text = "iOS 27",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control Grid (4 Quick Buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Torch / Flashlight
                QuickControlButton(
                    icon = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    label = "Torche",
                    status = if (isTorchOn) "Activée" else "Éteinte",
                    isActive = isTorchOn,
                    activeColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        try {
                            val camId = cameraManager?.cameraIdList?.firstOrNull()
                            if (camId != null) {
                                isTorchOn = !isTorchOn
                                cameraManager.setTorchMode(camId, isTorchOn)
                            }
                        } catch (_: Exception) {}
                    }
                )

                // 2. Sound Mode (Normal / Vibrate / Silent)
                val (soundIcon, soundLabel, soundActive) = when (ringerMode) {
                    AudioManager.RINGER_MODE_SILENT -> Triple(Icons.Default.VolumeMute, "Silencieux", false)
                    AudioManager.RINGER_MODE_VIBRATE -> Triple(Icons.Default.Vibration, "Vibreur", true)
                    else -> Triple(Icons.Default.VolumeUp, "Sonnerie", true)
                }
                QuickControlButton(
                    icon = soundIcon,
                    label = "Audio",
                    status = soundLabel,
                    isActive = soundActive,
                    activeColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        try {
                            if (audioManager != null) {
                                val nextMode = when (ringerMode) {
                                    AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                                    AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                                    else -> AudioManager.RINGER_MODE_NORMAL
                                }
                                audioManager.ringerMode = nextMode
                                ringerMode = nextMode
                            }
                        } catch (_: Exception) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            } catch (_: Exception) {}
                        }
                    }
                )

                // 3. Wi-Fi Shortcut
                QuickControlButton(
                    icon = Icons.Default.Wifi,
                    label = "Wi-Fi",
                    status = "Paramètres",
                    isActive = true,
                    activeColor = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        } catch (_: Exception) {}
                    }
                )

                // 4. Bluetooth Shortcut
                QuickControlButton(
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth",
                    status = "Appareils",
                    isActive = true,
                    activeColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        } catch (_: Exception) {}
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    status: String,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val btnBg = if (isActive) {
        activeColor.copy(alpha = if (isDark) 0.25f else 0.15f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    }
    val iconColor = if (isActive) activeColor else (if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f))
    val textColor = if (isDark) Color.White else Color(0xFF1E211E)
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = btnBg,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.4f)) else null,
        modifier = modifier.height(84.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = status,
                fontSize = 9.sp,
                color = subTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 6. iOS 27 Weather Radar & Hourly Forecast Glance Widget
 */
@Composable
fun WeatherGlanceCard(
    settings: LauncherSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val cardBg = if (isDark) Color(0xFF1E2430).copy(alpha = 0.85f) else Color(0xFFF0F6FF).copy(alpha = 0.90f)
    val cardBorder = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.20f) else Color(0xFF93C5FD).copy(alpha = 0.40f)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://weather.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Main Weather Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Paris",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ensoleillé • Max 26° Min 16°",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "23°",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Soleil",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hourly pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    Triple("Maintenant", "23°", Icons.Default.WbSunny),
                    Triple("15:00", "24°", Icons.Default.WbSunny),
                    Triple("17:00", "23°", Icons.Default.WbCloudy),
                    Triple("19:00", "21°", Icons.Default.WbCloudy),
                    Triple("21:00", "18°", Icons.Default.NightsStay)
                ).forEach { (hour, temp, icon) ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = hour,
                                fontSize = 9.sp,
                                color = textSecondary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (icon == Icons.Default.WbSunny) Color(0xFFF59E0B) else Color(0xFF60A5FA),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = temp,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 7. Add / Manage Home Widgets Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHomeWidgetsBottomSheet(
    settings: LauncherSettings,
    onUpdateSettings: (LauncherSettings) -> Unit,
    onResetOrder: () -> Unit = {},
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
                .padding(bottom = 36.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
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
                        text = "Gérer les widgets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onResetOrder) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Rétablir", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Widgets iOS 17 Écran d'accueil
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    WidgetToggleRow(
                        title = "Widgets iOS 17 sur l'écran d'accueil",
                        subtitle = "Afficher les widgets météo et batterie sur l'écran principal",
                        checked = settings.showIosHomeWidgets,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(showIosHomeWidgets = it))
                        }
                    )

                    if (settings.showIosHomeWidgets) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            text = "Format des widgets",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = settings.iosWidgetStyle == "pair",
                                onClick = { onUpdateSettings(settings.copy(iosWidgetStyle = "pair")) },
                                label = { Text("Météo + Batterie") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = settings.iosWidgetStyle == "quad_battery",
                                onClick = { onUpdateSettings(settings.copy(iosWidgetStyle = "quad_battery")) },
                                label = { Text("Batterie 4 Appareils") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        WidgetToggleRow(
                            title = "Étiquettes 'Weather' et 'Battery'",
                            subtitle = "Afficher les textes descriptifs sous les widgets",
                            checked = settings.showWidgetLabels,
                            onCheckedChange = {
                                onUpdateSettings(settings.copy(showWidgetLabels = it))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    WidgetToggleRow(
                        title = "Centre de contrôle iOS 27",
                        subtitle = "Raccourcis lampe torche, audio, Wi-Fi et Bluetooth",
                        checked = settings.showQuickControlsWidget,
                        onCheckedChange = {
                            val newOrder = if (it && !settings.widgetOrder.contains("controls")) {
                                settings.widgetOrder + "controls"
                            } else settings.widgetOrder
                            onUpdateSettings(settings.copy(showQuickControlsWidget = it, widgetOrder = newOrder))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    WidgetToggleRow(
                        title = "Radar Météo & Prévisions (iOS 27)",
                        subtitle = "Carte météo détaillée avec prévisions heure par heure",
                        checked = settings.showWeatherGlanceWidget,
                        onCheckedChange = {
                            val newOrder = if (it && !settings.widgetOrder.contains("weather_glance")) {
                                settings.widgetOrder + "weather_glance"
                            } else settings.widgetOrder
                            onUpdateSettings(settings.copy(showWeatherGlanceWidget = it, widgetOrder = newOrder))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    WidgetToggleRow(
                        title = "Batterie du téléphone (Pixel style)",
                        subtitle = "Affiche la carte de l'état de la batterie et de la charge",
                        checked = settings.showDeviceCardWidget,
                        onCheckedChange = {
                            val newOrder = if (it && !settings.widgetOrder.contains("battery")) {
                                settings.widgetOrder + "battery"
                            } else settings.widgetOrder
                            onUpdateSettings(settings.copy(showDeviceCardWidget = it, widgetOrder = newOrder))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    WidgetToggleRow(
                        title = "Lecteur de musique (Pixel Music)",
                        subtitle = "Contrôle rapide de la lecture et piste suivante",
                        checked = settings.showMusicWidget,
                        onCheckedChange = {
                            val newOrder = if (it && !settings.widgetOrder.contains("music")) {
                                settings.widgetOrder + "music"
                            } else settings.widgetOrder
                            onUpdateSettings(settings.copy(showMusicWidget = it, widgetOrder = newOrder))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    WidgetToggleRow(
                        title = "Mes tâches & Notes rapides",
                        subtitle = "Liste de tâches avec cases à cocher et ajout rapide",
                        checked = settings.showTasksWidget,
                        onCheckedChange = {
                            val newOrder = if (it && !settings.widgetOrder.contains("tasks")) {
                                settings.widgetOrder + "tasks"
                            } else settings.widgetOrder
                            onUpdateSettings(settings.copy(showTasksWidget = it, widgetOrder = newOrder))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    WidgetToggleRow(
                        title = "Raccourcis d'applications",
                        subtitle = "Affiche la barre d'accès rapide aux applications favorites",
                        checked = settings.showAppShortcutsWidget,
                        onCheckedChange = {
                            val newOrder = if (it && !settings.widgetOrder.contains("shortcuts")) {
                                settings.widgetOrder + "shortcuts"
                            } else settings.widgetOrder
                            onUpdateSettings(settings.copy(showAppShortcutsWidget = it, widgetOrder = newOrder))
                        }
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
