package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.domain.AppItem
import com.example.ui.viewmodels.LargeFolderModel
import kotlinx.coroutines.delay

/**
 * Authentic iOS-style Passcode Verification Screen
 */
@Composable
fun IosPasscodeDialog(
    isOpen: Boolean,
    title: String = "Saisissez le code d'accès",
    subtitle: String = "Application verrouillée",
    expectedPin: String = "1234",
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val haptic = LocalHapticFeedback.current
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Shake animation on error
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            if (enteredPin == expectedPin) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSuccess()
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isError = true
                shakeOffset.animateTo(
                    targetValue = 20f,
                    animationSpec = keyframes {
                        durationMillis = 350
                        0f at 0
                        -20f at 70
                        20f at 140
                        -15f at 210
                        15f at 280
                        0f at 350
                    }
                )
                delay(200)
                enteredPin = ""
                isError = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0E14).copy(alpha = 0.95f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume clicks */ },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .offset(x = shakeOffset.value.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Lock Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verrouillé",
                        tint = if (isError) Color(0xFFFF453A) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isError) "Code erroné. Réessayez." else subtitle,
                    color = if (isError) Color(0xFFFF453A) else Color.White.copy(alpha = 0.60f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 4 Passcode Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError -> Color(0xFFFF453A)
                                        isFilled -> Color.White
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isError) Color(0xFFFF453A) else Color.White.copy(alpha = 0.65f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Keypad numbers 1 to 9, and 0
                val keypadButtons = listOf(
                    listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                    listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                    listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                    listOf("" to "", "0" to "", "DEL" to "")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    keypadButtons.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.forEach { (digit, sub) ->
                                if (digit.isEmpty()) {
                                    Box(modifier = Modifier.size(72.dp))
                                } else if (digit == "DEL") {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Effacer",
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (enteredPin.length < 4) {
                                                    enteredPin += digit
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = digit,
                                                color = Color.White,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (sub.isNotEmpty()) {
                                                Text(
                                                    text = sub,
                                                    color = Color.White.copy(alpha = 0.50f),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Annuler",
                        color = Color.White.copy(alpha = 0.80f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * iOS Large Folder Component (2x2) for Home Screen
 * Displays top 3 apps in large clickable format, and a 4th quadrant mini grid
 */
@Composable
fun IosLargeFolderItem(
    folder: LargeFolderModel,
    appsMap: Map<String, AppItem>,
    iconSize: Dp,
    onAppClick: (String) -> Unit,
    onFolderOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val folderApps = remember(folder.packageNames, appsMap) {
        folder.packageNames.mapNotNull { appsMap[it] }
    }

    val primaryApps = folderApps.take(3)
    val remainingApps = folderApps.drop(3)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(6.dp)
    ) {
        // Frosted Glass Large Folder Container
        Box(
            modifier = Modifier
                .size(iconSize * 2.1f)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.38f),
                            Color.White.copy(alpha = 0.18f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.25f)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(8.dp)
        ) {
            // 2x2 Grid inside the Large Folder
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: App 0 & App 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // App 0
                    if (primaryApps.isNotEmpty()) {
                        LargeFolderAppSlot(
                            app = primaryApps[0],
                            size = iconSize * 0.85f,
                            onClick = { onAppClick(primaryApps[0].packageName) }
                        )
                    } else {
                        Box(modifier = Modifier.size(iconSize * 0.85f))
                    }

                    // App 1
                    if (primaryApps.size > 1) {
                        LargeFolderAppSlot(
                            app = primaryApps[1],
                            size = iconSize * 0.85f,
                            onClick = { onAppClick(primaryApps[1].packageName) }
                        )
                    } else {
                        Box(modifier = Modifier.size(iconSize * 0.85f))
                    }
                }

                // Bottom Row: App 2 & Mini Quadrant 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // App 2
                    if (primaryApps.size > 2) {
                        LargeFolderAppSlot(
                            app = primaryApps[2],
                            size = iconSize * 0.85f,
                            onClick = { onAppClick(primaryApps[2].packageName) }
                        )
                    } else {
                        Box(modifier = Modifier.size(iconSize * 0.85f))
                    }

                    // 4th Quadrant: Mini 2x2 Grid of remaining apps
                    Box(
                        modifier = Modifier
                            .size(iconSize * 0.85f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable(onClick = onFolderOpen)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (remainingApps.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    remainingApps.take(2).forEach { app ->
                                        MiniFolderAppIcon(app = app, size = (iconSize * 0.32f))
                                    }
                                }
                                if (remainingApps.size > 2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        remainingApps.drop(2).take(2).forEach { app ->
                                            MiniFolderAppIcon(app = app, size = (iconSize * 0.32f))
                                        }
                                    }
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Ouvrir dossier",
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = folder.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LargeFolderAppSlot(
    app: AppItem,
    size: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = app.icon,
                contentDescription = app.label,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun MiniFolderAppIcon(
    app: AppItem,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = app.icon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Fullscreen Folder Detail Sheet when opening a folder
 */
@Composable
fun IosFolderDetailSheet(
    folder: LargeFolderModel?,
    appsMap: Map<String, AppItem>,
    onDismiss: () -> Unit,
    onAppClick: (String) -> Unit
) {
    if (folder == null) return

    val folderApps = remember(folder.packageNames, appsMap) {
        folder.packageNames.mapNotNull { appsMap[it] }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E202B).copy(alpha = 0.88f),
                                Color(0xFF12141F).copy(alpha = 0.94f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(32.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Inside clicks */ }
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = folder.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.heightIn(max = 380.dp)
                    ) {
                        items(folderApps, key = { it.packageName }) { app ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        onDismiss()
                                        onAppClick(app.packageName)
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                ) {
                                    if (app.iconBitmap != null) {
                                        Image(
                                            bitmap = app.iconBitmap,
                                            contentDescription = app.label,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        AsyncImage(
                                            model = app.icon,
                                            contentDescription = app.label,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = app.label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
