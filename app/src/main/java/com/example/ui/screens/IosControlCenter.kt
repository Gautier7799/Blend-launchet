package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IosControlCenterSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val context = LocalContext.current
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager }

    // Torch state
    var isTorchOn by remember { mutableStateOf(false) }

    // Brightness state (0f to 1f)
    var brightness by remember {
        mutableFloatStateOf(
            activity?.window?.attributes?.screenBrightness?.let { if (it < 0f) 0.65f else it } ?: 0.65f
        )
    }

    // Volume state (0f to 1f)
    var volumeFraction by remember {
        val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 7
        val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        mutableFloatStateOf(if (max > 0) current.toFloat() / max.toFloat() else 0.5f)
    }

    // DND / Silent Mode
    var isSilentMode by remember {
        mutableStateOf(audioManager?.ringerMode != AudioManager.RINGER_MODE_NORMAL)
    }

    // Wi-Fi & Bluetooth visual state
    var isWifiActive by remember { mutableStateOf(true) }
    var isBtActive by remember { mutableStateOf(true) }
    var isAirplaneMode by remember { mutableStateOf(false) }

    // Clock formatted string
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    // Fullscreen Glass Scrim
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.50f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        // Control Center Card sliding down
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 40.dp)
                .align(Alignment.TopCenter)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume clicks inside */ }
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.6f)
                )
                .clip(RoundedCornerShape(36.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E202A).copy(alpha = 0.88f),
                            Color(0xFF0F1018).copy(alpha = 0.94f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.10f)
                        )
                    ),
                    shape = RoundedCornerShape(36.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Time & Close Handle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentTimeString,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Drag Pill / Close Button
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.35f))
                            .clickable { onDismiss() }
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer le Centre de Contrôle",
                            tint = Color.White.copy(alpha = 0.70f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Row 1: Connectivity (2x2) + Now Playing Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Connectivity 2x2 Glass Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(26.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Airplane Mode
                                ControlCircleButton(
                                    icon = Icons.Default.AirplanemodeActive,
                                    isActive = isAirplaneMode,
                                    activeColor = Color(0xFFFF9500),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isAirplaneMode = !isAirplaneMode
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (_: Exception) {}
                                    }
                                )

                                // Wi-Fi Button
                                ControlCircleButton(
                                    icon = Icons.Default.Wifi,
                                    isActive = isWifiActive,
                                    activeColor = Color(0xFF007AFF),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isWifiActive = !isWifiActive
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (_: Exception) {}
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Cellular Data
                                ControlCircleButton(
                                    icon = Icons.Default.SignalCellularAlt,
                                    isActive = true,
                                    activeColor = Color(0xFF34C759),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (_: Exception) {}
                                    }
                                )

                                // Bluetooth Button
                                ControlCircleButton(
                                    icon = Icons.Default.Bluetooth,
                                    isActive = isBtActive,
                                    activeColor = Color(0xFF007AFF),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isBtActive = !isBtActive
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (_: Exception) {}
                                    }
                                )
                            }
                        }
                    }

                    // Now Playing Glass Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(26.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFF2D55).copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Lecteur Musique",
                                        tint = Color(0xFFFF2D55),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Musique",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Non diffusé",
                                        color = Color.White.copy(alpha = 0.50f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }) {
                                    Icon(Icons.Default.SkipPrevious, "Précédent", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // Launch music player
                                    try {
                                        val intent = Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }) {
                                    Icon(Icons.Default.PlayArrow, "Lecture", tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }) {
                                    Icon(Icons.Default.SkipNext, "Suivant", tint = Color.White)
                                }
                            }
                        }
                    }
                }

                // Row 2: Vertical Sliders (Brightness + Volume) + Focus & Rotation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left 2 Tiles: Screen Orientation Lock + DND / Silent
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // DND / Silent Mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(if (isSilentMode) Color(0xFF5856D6).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isSilentMode = !isSilentMode
                                    try {
                                        if (audioManager != null) {
                                            audioManager.ringerMode = if (isSilentMode) AudioManager.RINGER_MODE_SILENT else AudioManager.RINGER_MODE_NORMAL
                                        }
                                    } catch (_: Exception) {}
                                }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isSilentMode) Icons.Default.NotificationsOff else Icons.Default.Bedtime,
                                contentDescription = "Ne pas déranger",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = if (isSilentMode) "Silencieux" else "Concentration",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Auto-Rotate Lock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        })
                                    } catch (_: Exception) {}
                                }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Verrouillage Orientation",
                                tint = Color(0xFFFF453A),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Rotation",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Brightness Vertical Slider
                    VerticalControlSlider(
                        value = brightness,
                        onValueChange = { newBrightness ->
                            brightness = newBrightness
                            activity?.let { act ->
                                val lp = act.window.attributes
                                lp.screenBrightness = newBrightness.coerceIn(0.05f, 1f)
                                act.window.attributes = lp
                            }
                        },
                        icon = Icons.Default.LightMode,
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                    )

                    // Volume Vertical Slider
                    VerticalControlSlider(
                        value = volumeFraction,
                        onValueChange = { newVolume ->
                            volumeFraction = newVolume
                            audioManager?.let { am ->
                                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val target = (newVolume * max).toInt().coerceIn(0, max)
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                            }
                        },
                        icon = if (volumeFraction > 0.05f) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                    )
                }

                // Row 3: Quick Utilities (Flashlight, Calculator, Timer, Camera)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Flashlight
                    UtilitySquareButton(
                        icon = Icons.Default.FlashlightOn,
                        label = "Torche",
                        isActive = isTorchOn,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            try {
                                if (cameraManager != null) {
                                    val cameraId = cameraManager.cameraIdList.firstOrNull()
                                    if (cameraId != null) {
                                        val newState = !isTorchOn
                                        cameraManager.setTorchMode(cameraId, newState)
                                        isTorchOn = newState
                                    }
                                }
                            } catch (_: Exception) {
                                isTorchOn = !isTorchOn
                            }
                        }
                    )

                    // 2. Calculator
                    UtilitySquareButton(
                        icon = Icons.Default.Calculate,
                        label = "Calculatrice",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                            try {
                                val intent = Intent().apply {
                                    action = Intent.ACTION_MAIN
                                    addCategory(Intent.CATEGORY_APP_CALCULATOR)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val pm = context.packageManager
                                val calcApp = listOf("com.google.android.calculator", "com.sec.android.app.popupcalculator")
                                    .firstOrNull { pkg -> pm.getLaunchIntentForPackage(pkg) != null }
                                if (calcApp != null) {
                                    context.startActivity(pm.getLaunchIntentForPackage(calcApp))
                                }
                            }
                        }
                    )

                    // 3. Timer / Alarm
                    UtilitySquareButton(
                        icon = Icons.Default.Timer,
                        label = "Minuteur",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                            try {
                                val intent = Intent(AlarmClock.ACTION_SHOW_TIMERS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )

                    // 4. Camera
                    UtilitySquareButton(
                        icon = Icons.Default.CameraAlt,
                        label = "Appareil photo",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                            try {
                                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlCircleButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else Color.White.copy(alpha = 0.20f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun VerticalControlSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var sliderHeightPx by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount / sliderHeightPx
                        val next = (value + delta).coerceIn(0f, 1f)
                        onValueChange(next)
                    }
                )
            }
    ) {
        // Track the container layout height
        androidx.compose.ui.layout.Layout(
            content = {},
            modifier = Modifier.fillMaxSize()
        ) { _, constraints ->
            sliderHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
            layout(constraints.maxWidth, constraints.maxHeight) {}
        }

        // Animated fill from bottom
        val animatedFraction by animateFloatAsState(
            targetValue = value.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 80, easing = LinearEasing),
            label = "slider_fill"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animatedFraction)
                .align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.90f))
        )

        // Bottom Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (value > 0.18f) Color(0xFF1C1C1E) else Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(24.dp)
        )
    }
}

@Composable
private fun UtilitySquareButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(20.dp))
                .background(if (isActive) Color.White else Color.White.copy(alpha = 0.16f))
                .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF1C1C1E) else Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
