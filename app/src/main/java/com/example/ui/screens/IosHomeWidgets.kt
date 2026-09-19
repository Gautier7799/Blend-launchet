package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

/**
 * Main Container for iOS 17 Home Screen Widgets
 * Supports:
 * - "pair": Square Weather Widget (2x2) + Square Battery Ring Widget (2x2) side-by-side
 * - "quad_battery": Wide Multi-Device Battery Widget (4 rings: Phone, Watch, AirPods, Battery Pack)
 */
@Composable
fun IosHomeWidgetsRow(
    settings: LauncherSettings,
    onOpenSettings: () -> Unit,
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
        mutableStateOf(
            com.example.util.LiveWeatherData(
                city = if (settings.iosWidgetCity.isNotBlank()) settings.iosWidgetCity else com.example.util.LocationWeatherHelper.detectDeviceCity(context),
                temperature = "24°",
                condition = "Ensoleillé",
                highLow = "H:26°  L:18°",
                isSunny = true
            )
        )
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        when (settings.iosWidgetStyle) {
            "quad_battery" -> {
                // Wide Multi-Device Battery Widget (4 Devices) from Screenshot 2
                IosQuadBatteryWidget(
                    batteryPercent = batteryPercent,
                    isCharging = isCharging,
                    showLabels = settings.showWidgetLabels,
                    onOpenBatterySettings = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        launchBatterySettings(context)
                    }
                )
            }
            else -> {
                // Default: Pair of Square 2x2 Widgets (Weather + Battery) from Screenshot 1 & 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. iOS Weather Square Widget (2x2)
                    Box(modifier = Modifier.weight(1f)) {
                        IosWeatherSquareWidget(
                            weather = liveWeather,
                            showLabels = settings.showWidgetLabels,
                            onWeatherClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                launchWeatherApp(context)
                            }
                        )
                    }

                    // 2. iOS Battery Ring Square Widget (2x2)
                    Box(modifier = Modifier.weight(1f)) {
                        IosBatterySquareWidget(
                            batteryPercent = batteryPercent,
                            isCharging = isCharging,
                            showLabels = settings.showWidgetLabels,
                            onBatteryClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                launchBatterySettings(context)
                            }
                        )
                    }
                }
            }
        }
    }
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
                .height(152.dp)
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

        // Subtitle Label below widget: "Weather"
        if (showLabels) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Weather",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.shadow(2.dp)
            )
        }
    }
}

/**
 * 2. iOS Square Battery Ring Widget (2x2)
 * Matches Screenshot 1 & 3:
 * Translucent frosted card, circular green battery ring with phone icon, "100%" text, "Battery" label below.
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
                .height(152.dp)
                .clip(RoundedCornerShape(24.dp))
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = "Widget Batterie, $batteryPercent pourcent, ${if (isCharging) "En charge" else "Sur batterie"}"
                }
                .clickable(
                    role = Role.Button,
                    onClick = onBatteryClick
                )
        ) {
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

        // Subtitle Label below widget: "Battery"
        if (showLabels) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Battery",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.shadow(2.dp)
            )
        }
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

        // Subtitle Label below widget: "Battery"
        if (showLabels) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Battery",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.shadow(2.dp)
            )
        }
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
