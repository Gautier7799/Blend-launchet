package com.example.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.viewmodels.LauncherSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main Container for iOS 17 Home Screen Widgets
 * Supports:
 * - "pair": Square Weather Widget (2x2) + Square Battery Ring Widget (2x2) side-by-side
 * - "quad_battery": Wide Multi-Device Battery Widget (4 rings: Phone, Watch, AirPods, Battery Pack)
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun IosHomeWidgetsRow(
    settings: LauncherSettings,
    onOpenSettings: () -> Unit,
    isEditMode: Boolean = false,
    onToggleEditMode: () -> Unit = {},
    onDeleteWidget: () -> Unit = {},
    onToggleStyle: () -> Unit = {},
    onLowerWidget: () -> Unit = {},
    onRaiseWidget: () -> Unit = {},
    onOpenTouchControls: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!settings.showIosHomeWidgets) return

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Real-time live battery status
    var batteryPercent by remember { mutableIntStateOf(85) }
    var isCharging by remember { mutableStateOf(false) }

    // Live weather connected to real device location / region
    var liveWeather by remember {
        mutableStateOf(com.example.util.LocationWeatherHelper.getCachedWeather())
    }

    LaunchedEffect(settings.iosWidgetCity) {
        val weather = com.example.util.LocationWeatherHelper.fetchLiveWeather(
            context,
            customCity = settings.iosWidgetCity.ifBlank { null }
        )
        liveWeather = weather
    }

    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                    if (level >= 0 && scale > 0) {
                        batteryPercent = (level * 100) / scale
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        // Read initial state
        try {
            val statusIntent = context.registerReceiver(null, filter)
            val level = statusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = statusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = statusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            if (level >= 0 && scale > 0) {
                batteryPercent = (level * 100) / scale
            }
        } catch (_: Exception) {}

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // Outer Container with Touch/Long-Press handling
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floating "Supprimer" / Delete Pill above widget in edit mode (as in Screenshot 1)
            androidx.compose.animation.AnimatedVisibility(
                visible = isEditMode,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, Color(0xFFC486EB).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDeleteWidget()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Supprimer",
                            tint = Color(0xFF1E2125),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Supprimer",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2125)
                        )
                    }
                }
            }

            // Widget Content Box with optional Resize handles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isEditMode) {
                            Modifier
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFFC486EB),
                                    shape = RoundedCornerShape(30.dp)
                                )
                                .padding(6.dp)
                        } else {
                            Modifier
                        }
                    )
                    .combinedClickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (isEditMode) {
                                onToggleEditMode()
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleEditMode()
                        }
                    )
            ) {
                when (settings.iosWidgetStyle) {
                    "quad_battery" -> {
                        // Wide Multi-Device Battery Widget (4 Devices) from Screenshot 2
                        IosQuadBatteryWidget(
                            batteryPercent = batteryPercent,
                            isCharging = isCharging,
                            showLabels = settings.showWidgetLabels,
                            onOpenBatterySettings = {
                                if (!isEditMode) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    launchBatterySettings(context)
                                } else {
                                    onToggleEditMode()
                                }
                            }
                        )
                    }
                    else -> {
                        // iOS Smart Stacks (Weather/Calendar/Tasks & Battery/System/Health) with Vertical Swipe
                        var leftStackIndex by remember { mutableIntStateOf(0) }
                        var rightStackIndex by remember { mutableIntStateOf(0) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Left Smart Stack (Weather / Calendar / Tasks)
                            Box(modifier = Modifier.weight(1f)) {
                                IosSmartStackContainer(
                                    totalCards = 3,
                                    currentIndex = leftStackIndex,
                                    onIndexChange = { leftStackIndex = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) { cardIndex ->
                                    when (cardIndex) {
                                        0 -> IosWeatherSquareWidget(
                                            weather = liveWeather,
                                            showLabels = settings.showWidgetLabels,
                                            onWeatherClick = {
                                                if (!isEditMode) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    launchWeatherApp(context)
                                                } else {
                                                    onToggleEditMode()
                                                }
                                            }
                                        )
                                        1 -> IosCalendarSquareWidget(
                                            showLabels = settings.showWidgetLabels,
                                            onClick = {
                                                if (!isEditMode) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    try {
                                                        val intent = Intent(Intent.ACTION_MAIN).apply {
                                                            addCategory(Intent.CATEGORY_APP_CALENDAR)
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                } else {
                                                    onToggleEditMode()
                                                }
                                            }
                                        )
                                        else -> IosTasksSquareWidget(
                                            showLabels = settings.showWidgetLabels,
                                            onClick = {
                                                if (isEditMode) onToggleEditMode()
                                            }
                                        )
                                    }
                                }
                            }

                            // 2. Right Smart Stack (Battery Ring / System Health / Battery Grid)
                            Box(modifier = Modifier.weight(1f)) {
                                IosSmartStackContainer(
                                    totalCards = 2,
                                    currentIndex = rightStackIndex,
                                    onIndexChange = { rightStackIndex = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) { cardIndex ->
                                    when (cardIndex) {
                                        0 -> IosBatterySquareWidget(
                                            batteryPercent = batteryPercent,
                                            isCharging = isCharging,
                                            showLabels = settings.showWidgetLabels,
                                            onBatteryClick = {
                                                if (!isEditMode) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    launchBatterySettings(context)
                                                } else {
                                                    onToggleEditMode()
                                                }
                                            }
                                        )
                                        else -> IosSystemHealthWidget(
                                            showLabels = settings.showWidgetLabels,
                                            onClick = {
                                                if (!isEditMode) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    try {
                                                        context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        })
                                                    } catch (_: Exception) {}
                                                } else {
                                                    onToggleEditMode()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }

            // Quick Actions Strip in Edit Mode (Style switch, delete, done - touch resize disabled)
            androidx.compose.animation.AnimatedVisibility(
                visible = isEditMode,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161922).copy(alpha = 0.90f))
                        .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleStyle()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("تغيير الشكل", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.25f)))

                    TextButton(
                        onClick = onToggleEditMode,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("تم ✓", color = Color(0xFF34C759), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Circular resize handle dot (as in Screenshot 1)
 */
@Composable
private fun ResizeHandleDot(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .size(16.dp)
            .shadow(4.dp, CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color(0xFFF3E8FC),
        border = BorderStroke(2.dp, Color(0xFFC486EB))
    ) {}
}

/**
 * 1. iOS Square Weather Widget (2x2)
 * Matches Screenshot 1 & 3:
 * Blue gradient card, Location with arrow, Large temperature, condition & High/Low, "Weather" label below.
 */
@Composable
fun IosWeatherSquareWidget(
    weather: com.example.util.LiveWeatherData,
    showLabels: Boolean,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val skyGradient = remember(weather.isRainy) {
        if (weather.isRainy) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF37474F),
                    Color(0xFF263238),
                    Color(0xFF1B2428)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2979FF),
                    Color(0xFF1E60D4),
                    Color(0xFF1548A6)
                )
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(RoundedCornerShape(24.dp))
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = "Widget Météo ${weather.city}, ${weather.temperature}, ${weather.condition}"
                }
                .clickable(
                    role = Role.Button,
                    onClick = onWeatherClick
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(skyGradient)
                    .padding(14.dp)
            ) {
                // Background subtle cloud glow
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .offset(x = 60.dp, y = (-20).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: City & Location Arrow
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = weather.city,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Middle: Big Temperature
                    Text(
                        text = weather.temperature,
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Light,
                        lineHeight = 40.sp
                    )

                    // Bottom: Condition icon & High / Low
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            val weatherIcon = when {
                                weather.isRainy -> Icons.Default.WaterDrop
                                weather.isCloudy -> Icons.Default.Cloud
                                else -> Icons.Default.WbSunny
                            }
                            val iconTint = if (weather.isSunny) Color(0xFFFFD54F) else Color.White
                            Icon(
                                imageVector = weatherIcon,
                                contentDescription = weather.condition,
                                tint = iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = weather.condition,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = weather.highLow,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
        // Subtitle label "Weather" permanently removed as requested by user (red X)
    }
}

/**
 * 2. iOS Square Battery Ring Widget (2x2)
 * High-intensity frosted glass blur with green charging ring & percentage.
 * Subtitle label "Battery" permanently removed as requested by user (red X).
 */
@Composable
fun IosBatterySquareWidget(
    batteryPercent: Int,
    isCharging: Boolean,
    showLabels: Boolean,
    onBatteryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High-intensity Frosted Blur Card matching user request ("مع زيادة حدة blur في ايقونة البطارية")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0x35000000),
                    ambientColor = Color.Black.copy(alpha = 0.22f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) listOf(
                            Color(0x65FFFFFF),
                            Color(0x32FFFFFF),
                            Color(0x18151D30),
                            Color(0x3E0A0F1A)
                        ) else listOf(
                            Color(0xB5FFFFFF),
                            Color(0x75FFFFFF),
                            Color(0x58E2E8F0),
                            Color(0x80CBD5E1)
                        )
                    )
                )
                .border(
                    width = 1.4.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.72f else 0.94f),
                            Color.White.copy(alpha = if (isDark) 0.20f else 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = "Widget Batterie, $batteryPercent pourcent, ${if (isCharging) "En charge" else "Sur batterie"}"
                }
                .clickable(
                    role = Role.Button,
                    onClick = onBatteryClick
                )
        ) {
            // Intense frosted specular sheen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.35f else 0.55f),
                                Color.Transparent,
                                Color.White.copy(alpha = if (isDark) 0.10f else 0.25f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Circular Ring with Phone icon
                    IosCircularBatteryGauge(
                        percent = batteryPercent,
                        isCharging = isCharging,
                        icon = Icons.Default.PhoneAndroid,
                        size = 68.dp,
                        strokeWidth = 7.dp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Percentage Text
                    Text(
                        text = "$batteryPercent%",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        // Subtitle label "Battery" permanently removed as requested by user (red X)
    }
}

/**
 * 3. iOS Multi-Device Wide Battery Widget (4 Circular Gauges)
 * Matches Screenshot 2 (circled in red by user):
 * 4 circular battery indicators: Phone (73%), Watch (40%), AirPods (100%), MagSafe/Pack (14%).
 */
@Composable
fun IosQuadBatteryWidget(
    batteryPercent: Int,
    isCharging: Boolean,
    showLabels: Boolean,
    onOpenBatterySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0x35FFFFFF) else Color(0x75FFFFFF)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.5f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, cardBorder),
            color = cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp)
                .clip(RoundedCornerShape(24.dp))
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = "Widget Batterie multi-appareils iOS"
                }
                .clickable(
                    role = Role.Button,
                    onClick = onOpenBatterySettings
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Device 1: Phone (Live system battery)
                QuadDeviceBatteryItem(
                    label = "$batteryPercent%",
                    percent = batteryPercent,
                    isCharging = isCharging,
                    icon = Icons.Default.PhoneAndroid
                )

                // Device 2: Apple Watch / Smartwatch (e.g., 40% as shown in screenshot)
                QuadDeviceBatteryItem(
                    label = "40%",
                    percent = 40,
                    isCharging = false,
                    icon = Icons.Default.Watch
                )

                // Device 3: AirPods / Earbuds (e.g., 100% as shown in screenshot)
                QuadDeviceBatteryItem(
                    label = "100%",
                    percent = 100,
                    isCharging = false,
                    icon = Icons.Default.Headphones
                )

                // Device 4: Battery Pack / Case (e.g., 14% as shown in screenshot)
                QuadDeviceBatteryItem(
                    label = "14%",
                    percent = 14,
                    isCharging = false,
                    icon = Icons.Default.BatteryChargingFull,
                    accentColor = Color(0xFFFF3B30) // Red warning below 20% as in screenshot
                )
            }
        }
        // Subtitle label "Battery" permanently removed as requested by user (red X)
    }
}

@Composable
private fun QuadDeviceBatteryItem(
    label: String,
    percent: Int,
    isCharging: Boolean,
    icon: ImageVector,
    accentColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IosCircularBatteryGauge(
            percent = percent,
            isCharging = isCharging,
            icon = icon,
            size = 54.dp,
            strokeWidth = 6.dp,
            overrideColor = accentColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Reusable iOS Circular Battery Progress Gauge
 */
@Composable
fun IosCircularBatteryGauge(
    percent: Int,
    isCharging: Boolean,
    icon: ImageVector,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    overrideColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val targetFraction = (percent.coerceIn(0, 100) / 100f)
    val animatedSweep by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(600),
        label = "battery_sweep"
    )

    // iOS Battery Colors: Green normally, Amber <= 20%, Red <= 10%
    val ringColor = overrideColor ?: when {
        isCharging -> Color(0xFF34C759)
        percent <= 10 -> Color(0xFFFF3B30)
        percent <= 20 -> Color(0xFFFF9500)
        else -> Color(0xFF34C759)
    }

    val trackColor = ringColor.copy(alpha = 0.22f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val radius = (this.size.minDimension - strokePx) / 2
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            val arcSize = Size(radius * 2, radius * 2)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Foreground Progress Arc
            if (animatedSweep > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Icon inside ring
        Icon(
            imageVector = if (isCharging) Icons.Default.Bolt else icon,
            contentDescription = null,
            tint = if (isCharging) Color(0xFFFFD54F) else Color.White,
            modifier = Modifier.size(size * 0.42f)
        )
    }
}

// System Helpers
private fun launchWeatherApp(context: Context) {
    val weatherPackages = listOf(
        "com.google.android.apps.weather",
        "com.sec.android.daemonapp",
        "com.xiaomi.weather2",
        "com.coloros.weather2"
    )
    for (pkg in weatherPackages) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(launchIntent)
                return
            } catch (_: Exception) {}
        }
    }
    // Fallback to Google Search Weather
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=meteo")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun launchBatterySettings(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallback = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (_: Exception) {
            try {
                val general = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(general)
            } catch (_: Exception) {}
        }
    }
}

/**
 * iOS Smart Stack Container with Vertical Swipe & Pagination Dots
 */
@Composable
fun IosSmartStackContainer(
    totalCards: Int,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(totalCards, currentIndex) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = { dragAccumulator = 0f },
                    onDragCancel = { dragAccumulator = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount
                        if (dragAccumulator < -40f) {
                            // Swipe up -> Next card
                            dragAccumulator = 0f
                            val next = (currentIndex + 1) % totalCards
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onIndexChange(next)
                        } else if (dragAccumulator > 40f) {
                            // Swipe down -> Prev card
                            dragAccumulator = 0f
                            val prev = if (currentIndex > 0) currentIndex - 1 else totalCards - 1
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onIndexChange(prev)
                        }
                    }
                )
            }
    ) {
        // Active Widget Card
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                } else {
                    (slideInVertically { height -> -height } + fadeIn()).togetherWith(slideOutVertically { height -> height } + fadeOut())
                }
            },
            label = "smart_stack_card"
        ) { page ->
            content(page)
        }

        // Vertical iOS Pagination Dots on the right edge
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 5.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.20f))
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (i in 0 until totalCards) {
                val isActive = i == currentIndex
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(if (isActive) 10.dp else 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isActive) Color.White else Color.White.copy(alpha = 0.35f))
                )
            }
        }
    }
}

/**
 * iOS Calendar Square Widget (Card in Left Smart Stack)
 */
@Composable
fun IosCalendarSquareWidget(
    showLabels: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfWeek = remember { SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()).replaceFirstChar { it.uppercase() } }
    val dayOfMonth = remember { SimpleDateFormat("d", Locale.getDefault()).format(Date()) }
    val monthName = remember { SimpleDateFormat("MMMM", Locale.getDefault()).format(Date()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2C2C2E).copy(alpha = 0.88f),
                            Color(0xFF1C1C1E).copy(alpha = 0.94f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(26.dp))
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Day of Week in red iOS accent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayOfWeek.uppercase(),
                        color = Color(0xFFFF453A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.40f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Center: Big Day Number
                Text(
                    text = dayOfMonth,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 44.sp
                )

                // Bottom: Month and Event
                Column {
                    Text(
                        text = monthName,
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Aucun événement prévu",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (showLabels) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Calendrier",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * iOS Quick Tasks Square Widget (Card in Left Smart Stack)
 */
@Composable
fun IosTasksSquareWidget(
    showLabels: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2C2C2E).copy(alpha = 0.88f),
                            Color(0xFF1C1C1E).copy(alpha = 0.94f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(26.dp))
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rappels",
                        color = Color(0xFF007AFF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Organisation iOS",
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFF34C759), CircleShape)
                                .background(Color(0xFF34C759))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Objectifs terminés",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = "2 tâches en attente",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.sp
                )
            }
        }

        if (showLabels) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rappels",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * iOS System Health & Storage Widget (Card in Right Smart Stack)
 */
@Composable
fun IosSystemHealthWidget(
    showLabels: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var freeStorageGb by remember { mutableFloatStateOf(32.5f) }
    var freeRamPercent by remember { mutableIntStateOf(65) }

    LaunchedEffect(Unit) {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            freeStorageGb = (availableBytes.toFloat() / (1024f * 1024f * 1024f))

            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            if (memInfo.totalMem > 0) {
                freeRamPercent = ((memInfo.availMem.toFloat() / memInfo.totalMem.toFloat()) * 100).toInt()
            }
        } catch (_: Exception) {}
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2C2C2E).copy(alpha = 0.88f),
                            Color(0xFF1C1C1E).copy(alpha = 0.94f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(26.dp))
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Système",
                        color = Color(0xFF34C759),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${freeStorageGb.toInt()} Go",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Espace libre",
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 10.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$freeRamPercent%",
                            color = Color(0xFF34C759),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "RAM dispo",
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    text = "Performance optimale",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.sp
                )
            }
        }

        if (showLabels) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stockage",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

