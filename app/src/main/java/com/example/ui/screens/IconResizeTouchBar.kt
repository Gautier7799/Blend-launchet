package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.LauncherSettings

/**
 * Floating Touch-based Icon and Widget Sizing Controller
 * Allows real-time interactive enlargement/shrinking of icons and widget positioning.
 */
@Composable
fun IconResizeTouchBar(
    currentIconSize: Int,
    currentWidgetSpacing: Int,
    onIconSizeChange: (Int) -> Unit,
    onWidgetSpacingChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Icons, 1 = Widget Position

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .shadow(24.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF1E222B).copy(alpha = 0.94f),
        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Tabs and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabPill(
                        title = "حجم الأيقونات",
                        isSelected = selectedTab == 0,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTab = 0
                        }
                    )
                    TabPill(
                        title = "موضع الودجت",
                        isSelected = selectedTab == 1,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTab = 1
                        }
                    )
                }

                // Done Button
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34C759))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "تم",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // --- ICON SIZING CONTROLLER ---
                // Value indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "التحكم باللمس في حجم الأيقونات",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${currentIconSize} dp",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Stepper + Slider Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Minus Step Button
                    IconButton(
                        onClick = {
                            val next = (currentIconSize - 2).coerceAtLeast(40)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onIconSizeChange(next)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "تصغير",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Fluid Slider
                    Slider(
                        value = currentIconSize.toFloat(),
                        onValueChange = { newVal ->
                            val rounded = newVal.toInt()
                            if (rounded != currentIconSize) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onIconSizeChange(rounded)
                            }
                        },
                        valueRange = 40f..82f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF007AFF),
                            inactiveTrackColor = Color.White.copy(alpha = 0.22f)
                        )
                    )

                    // Plus Step Button
                    IconButton(
                        onClick = {
                            val next = (currentIconSize + 2).coerceAtMost(82)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onIconSizeChange(next)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "تكبير",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Presets Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SizePresetChip("صغير (48)", 48, currentIconSize) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onIconSizeChange(48)
                    }
                    SizePresetChip("عادي (58)", 58, currentIconSize) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onIconSizeChange(58)
                    }
                    SizePresetChip("كبير (68)", 68, currentIconSize) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onIconSizeChange(68)
                    }
                    SizePresetChip("ضخم (78)", 78, currentIconSize) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onIconSizeChange(78)
                    }
                }
            } else {
                // --- WIDGET POSITION CONTROLLER ("انزل الودجت") ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المسافة العلوية وتنزيل الودجت",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${currentWidgetSpacing} dp",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Move Up
                    IconButton(
                        onClick = {
                            val next = (currentWidgetSpacing - 4).coerceAtLeast(6)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onWidgetSpacingChange(next)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "رفع",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Spacing Slider
                    Slider(
                        value = currentWidgetSpacing.toFloat(),
                        onValueChange = { newVal ->
                            val rounded = newVal.toInt()
                            if (rounded != currentWidgetSpacing) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onWidgetSpacingChange(rounded)
                            }
                        },
                        valueRange = 6f..64f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFFFF9500),
                            inactiveTrackColor = Color.White.copy(alpha = 0.22f)
                        )
                    )

                    // Move Down
                    IconButton(
                        onClick = {
                            val next = (currentWidgetSpacing + 4).coerceAtMost(64)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onWidgetSpacingChange(next)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "إنزال",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SizePresetChip("قريب (10)", 10, currentWidgetSpacing) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onWidgetSpacingChange(10)
                    }
                    SizePresetChip("افتراضي (26)", 26, currentWidgetSpacing) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onWidgetSpacingChange(26)
                    }
                    SizePresetChip("منزل أكثر (38)", 38, currentWidgetSpacing) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onWidgetSpacingChange(38)
                    }
                    SizePresetChip("منخفض جداً (52)", 52, currentWidgetSpacing) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onWidgetSpacingChange(52)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.60f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SizePresetChip(
    label: String,
    value: Int,
    currentValue: Int,
    onClick: () -> Unit
) {
    val isSelected = kotlin.math.abs(value - currentValue) <= 2
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

/**
 * Floating HUD Pill that displays during pinch-to-zoom gestures
 */
@Composable
fun PinchZoomHudPill(
    iconSize: Int,
    onOpenFullControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onOpenFullControls),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF161922).copy(alpha = 0.90f),
        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.30f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🔍 حجم الأيقونات: ${iconSize}dp",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = CircleShape,
                color = Color(0xFF007AFF)
            ) {
                Text(
                    text = "تحكم باللمس",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
