package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.os.Build
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.grid.GridItemSpan
import coil.compose.AsyncImage
import com.example.domain.AppItem
import com.example.ui.screens.AddAppsBottomSheet
import com.example.ui.screens.AiAssistantBottomSheet
import com.example.ui.screens.AppActionBottomSheet
import com.example.ui.screens.AppShortcutsCard
import com.example.ui.screens.DeviceBatteryCard
import com.example.ui.screens.LauncherSettingsBottomSheet
import com.example.ui.screens.ManageHomeWidgetsBottomSheet
import com.example.ui.screens.MusicPlayerCard
import com.example.ui.screens.TasksCard
import com.example.ui.screens.TopWidgetsBar
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.LauncherSettings
import com.example.ui.viewmodels.LauncherViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove status and navigation bar scrims and shadows
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            MyApplicationTheme {
                BlendLauncherScreen(viewModel = viewModel)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back button from closing launcher
    }
}

@Composable
fun BlendLauncherScreen(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val activity = context as? ComponentActivity
    val apps by viewModel.installedApps.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val homeAppPackages by viewModel.homeAppPackages.collectAsState()
    val dockAppPackages by viewModel.dockAppPackages.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    var isDrawerOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isAddAppsOpen by remember { mutableStateOf(false) }
    var isManageWidgetsOpen by remember { mutableStateOf(false) }
    var isReorderingMode by remember { mutableStateOf(false) }
    var selectedActionApp by remember { mutableStateOf<AppItem?>(null) }
    var isAiAssistantOpen by remember { mutableStateOf(false) }

    // Fullscreen Immersive Mode: Hides top status bar and bottom navigation bar
    LaunchedEffect(settings.fullscreenMode, activity) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (settings.fullscreenMode) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
                insetsController.show(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    val appMap = remember(apps) { apps.associateBy { it.packageName } }
    val homeApps = remember(homeAppPackages, appMap) {
        homeAppPackages.mapNotNull { appMap[it] }
    }
    val dockApps = remember(dockAppPackages, appMap, settings.dockCount) {
        dockAppPackages.take(settings.dockCount).mapNotNull { appMap[it] }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wobble_transition")
    val wobbleAngle by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(130, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble_angle"
    )

    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Background gesture detector (placed behind everything)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(settings.doubleTapToSleep, settings.hapticFeedback, isReorderingMode) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (settings.doubleTapToSleep && !isReorderingMode) {
                                if (settings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.performSleep(context)
                            }
                        },
                        onLongPress = {
                            if (settings.hapticFeedback) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            isSettingsOpen = true
                        }
                    )
                }
                .pointerInput(isReorderingMode) {
                    if (!isReorderingMode) {
                        detectVerticalDragGestures(
                            onDragStart = { accumulatedDrag = 0f },
                            onDragEnd = {
                                if (accumulatedDrag < -25f) {
                                    isDrawerOpen = true
                                }
                                accumulatedDrag = 0f
                            },
                            onDragCancel = { accumulatedDrag = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                accumulatedDrag += dragAmount
                                if (accumulatedDrag < -30f) {
                                    change.consume()
                                    isDrawerOpen = true
                                } else if (accumulatedDrag > 30f && isDrawerOpen) {
                                    change.consume()
                                    isDrawerOpen = false
                                }
                            }
                        )
                    }
                }
        )
        
        // --- Home Screen Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (settings.fullscreenMode) 24.dp else 52.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            // Reordering Mode Notification Banner
            if (isReorderingMode) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OpenWith,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Glissez les icônes pour changer d'ordre",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Button(
                            onClick = { isReorderingMode = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Terminer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Top Widgets Row (AI Assistant, Date, Weather, Battery, Clock - customizable from Settings)
            if (!isReorderingMode) {
                TopWidgetsBar(
                    settings = settings,
                    onAiClick = { isAiAssistantOpen = true },
                    onSettingsClick = { isSettingsOpen = true }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Home Screen Grid with dynamic reordering and interactive home widgets
            LazyVerticalGrid(
                columns = GridCells.Fixed(settings.gridColumns),
                contentPadding = PaddingValues(bottom = 145.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 1. Device Battery Card Widget (Pixel 8 style from screenshot)
                if (!isReorderingMode && settings.showDeviceCardWidget) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        DeviceBatteryCard(
                            settings = settings,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                    }
                }

                // 2. Music Player Widget Card (Pixel Music style from screenshot)
                if (!isReorderingMode && settings.showMusicWidget) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        MusicPlayerCard(
                            settings = settings,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                    }
                }

                // 3. Quick Tasks Widget Card ("Mes tâches" style from screenshot)
                if (!isReorderingMode && settings.showTasksWidget) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        TasksCard(
                            tasks = tasks,
                            onAddTask = { viewModel.addTask(it) },
                            onToggleTask = { viewModel.toggleTask(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            settings = settings,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                    }
                }

                // 4. App Shortcuts Widget Card (اختصارات التطبيقات المتوفرة)
                if (!isReorderingMode && settings.showAppShortcutsWidget) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        AppShortcutsCard(
                            apps = apps,
                            settings = settings,
                            onAppClick = { viewModel.launchApp(it) },
                            onOpenDrawer = { isDrawerOpen = true },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                    }
                }

                // Spacing separator if widgets are displayed
                if (!isReorderingMode && (settings.showDeviceCardWidget || settings.showMusicWidget || settings.showTasksWidget || settings.showAppShortcutsWidget)) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // App Grid Items
                itemsIndexed(
                    items = homeApps,
                    key = { _, app -> app.packageName }
                ) { index, app ->
                    ReorderableHomeAppItem(
                        app = app,
                        index = index,
                        totalCount = homeApps.size,
                        isReordering = isReorderingMode,
                        wobbleAngle = if (index % 2 == 0) wobbleAngle else -wobbleAngle,
                        showLabel = settings.showLabels,
                        showTextShadows = settings.showTextShadows,
                        iconSize = settings.iconSizeDp.dp,
                        themedIcon = settings.themedIcons,
                        onClick = {
                            if (!isReorderingMode) {
                                viewModel.launchApp(app.packageName)
                            }
                        },
                        onLongClick = {
                            if (!isReorderingMode) {
                                if (settings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                selectedActionApp = app
                            }
                        },
                        onMoveLeft = {
                            if (index > 0) {
                                if (settings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.reorderHomeApps(index, index - 1)
                            }
                        },
                        onMoveRight = {
                            if (index < homeApps.size - 1) {
                                if (settings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.reorderHomeApps(index, index + 1)
                            }
                        },
                        onMoveToDock = {
                            if (settings.hapticFeedback) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            viewModel.removeAppFromHome(app.packageName)
                            viewModel.addAppToDock(app.packageName)
                        },
                        onRemove = {
                            if (settings.hapticFeedback) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            viewModel.removeAppFromHome(app.packageName)
                        }
                    )
                }

                // Add Apps "+" Button Slot
                if (!isReorderingMode) {
                    item {
                        Column(
                            modifier = Modifier
                                .padding(6.dp)
                                .clickable { isAddAppsOpen = true },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(settings.iconSizeDp.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(18.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ajouter",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            if (settings.showLabels) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ajouter",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    style = if (settings.showTextShadows) {
                                        androidx.compose.ui.text.TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.7f),
                                                blurRadius = 6f
                                            )
                                        )
                                    } else {
                                        androidx.compose.ui.text.TextStyle()
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Manage / Add Widgets Button Slot removed as per user request
                }
            }
        }

        // --- iOS: Ultra-Premium Glassmorphism Dock with Swipe Up to Open Drawer ---
        if (!isDrawerOpen) {
            var dockDragAmount by remember { mutableFloatStateOf(0f) }

            // Gesture handle pill above dock indicating swipe up
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isReorderingMode) 138.dp else 124.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.55f))
                    .clickable { isDrawerOpen = true }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(if (isReorderingMode) 108.dp else 94.dp)
                    .pointerInput(isReorderingMode) {
                        if (!isReorderingMode) {
                            detectVerticalDragGestures(
                                onDragStart = { dockDragAmount = 0f },
                                onDragEnd = {
                                    if (dockDragAmount < -20f) {
                                        isDrawerOpen = true
                                    }
                                    dockDragAmount = 0f
                                },
                                onDragCancel = { dockDragAmount = 0f },
                                onVerticalDrag = { change, dragAmount ->
                                    dockDragAmount += dragAmount
                                    if (dockDragAmount < -25f) {
                                        change.consume()
                                        isDrawerOpen = true
                                    }
                                }
                            )
                        }
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = settings.dockOpacity),
                                Color.White.copy(alpha = settings.dockOpacity * 0.5f)
                            )
                        )
                    )
                    .then(
                        if (settings.showDockLines) {
                            Modifier.border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.65f),
                                        Color.White.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(32.dp)
                            )
                        } else Modifier
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockApps.forEachIndexed { index, app ->
                        ReorderableDockAppItem(
                            app = app,
                            index = index,
                            totalCount = dockApps.size,
                            isReordering = isReorderingMode,
                            wobbleAngle = wobbleAngle,
                            iconSize = (settings.iconSizeDp - 6).dp,
                            themedIcon = settings.themedIcons,
                            onClick = { viewModel.launchApp(app.packageName) },
                            onLongClick = {
                                if (settings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                selectedActionApp = app
                            },
                            onMoveLeft = {
                                if (index > 0) {
                                    if (settings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    viewModel.reorderDockApps(index, index - 1)
                                }
                            },
                            onMoveRight = {
                                if (index < dockApps.size - 1) {
                                    if (settings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    viewModel.reorderDockApps(index, index + 1)
                                }
                            },
                            onMoveToHome = {
                                if (settings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.removeAppFromDock(app.packageName)
                                viewModel.addAppToHome(app.packageName)
                            },
                            onRemove = {
                                if (settings.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.removeAppFromDock(app.packageName)
                            }
                        )
                    }

                    if (isReorderingMode && dockApps.size < settings.dockCount) {
                        Box(
                            modifier = Modifier
                                .size((settings.iconSizeDp - 6).dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { isAddAppsOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ajouter au Dock",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Android: Enhanced App Drawer with Search ---
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 280)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 280)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AppDrawer(
                apps = apps,
                settings = settings,
                onAppClick = { viewModel.launchApp(it) },
                onAppLongClick = { selectedActionApp = it },
                onClose = { isDrawerOpen = false },
                onSettingsClick = { isSettingsOpen = true }
            )
        }

        // --- App Action Bottom Sheet (Move / Remove / Add / Info) ---
        selectedActionApp?.let { app ->
            val homeIndex = homeAppPackages.indexOf(app.packageName)
            val isOnHome = homeIndex != -1
            val dockIndex = dockAppPackages.indexOf(app.packageName)
            val isOnDock = dockIndex != -1

            AppActionBottomSheet(
                app = app,
                viewModel = viewModel,
                isOnHome = isOnHome,
                canMoveLeft = isOnHome && homeIndex > 0,
                canMoveRight = isOnHome && homeIndex < homeAppPackages.size - 1,
                isOnDock = isOnDock,
                canMoveDockLeft = isOnDock && dockIndex > 0,
                canMoveDockRight = isOnDock && dockIndex < dockAppPackages.size - 1,
                onStartReorder = { isReorderingMode = true },
                onDismissRequest = { selectedActionApp = null }
            )
        }

        // --- Add Apps Bottom Sheet ---
        if (isAddAppsOpen) {
            AddAppsBottomSheet(
                viewModel = viewModel,
                apps = apps,
                homeAppPackages = homeAppPackages,
                dockAppPackages = dockAppPackages,
                onDismissRequest = { isAddAppsOpen = false }
            )
        }

        // --- Launcher Settings Sheet ---
        if (isSettingsOpen) {
            LauncherSettingsBottomSheet(
                viewModel = viewModel,
                settings = settings,
                onDismissRequest = { isSettingsOpen = false },
                onManageWidgetsClick = {
                    isSettingsOpen = false
                    isManageWidgetsOpen = true
                }
            )
        }

        // --- AI Assistant Sheet ---
        if (isAiAssistantOpen) {
            AiAssistantBottomSheet(
                onDismissRequest = { isAiAssistantOpen = false }
            )
        }

        // --- Manage Home Widgets Sheet ---
        if (isManageWidgetsOpen) {
            ManageHomeWidgetsBottomSheet(
                settings = settings,
                onUpdateSettings = { viewModel.updateSettings(it) },
                onDismissRequest = { isManageWidgetsOpen = false }
            )
        }
    }
}

@Composable
fun AppDrawer(
    apps: List<AppItem>,
    settings: LauncherSettings,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onClose: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) apps
        else apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFF121418), // 100% OPAQUE - completely hides home screen widgets and prevents ghosting
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (!settings.hideDrawerHeader) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Applications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                text = "${filteredApps.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Paramètres",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = Color.White
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // In-Drawer Live Search Field
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher dans les applications...", color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Effacer",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Grid of Filtered Apps with stable keys to prevent lag
            LazyVerticalGrid(
                columns = GridCells.Fixed(settings.gridColumns),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = filteredApps,
                    key = { it.packageName }
                ) { app ->
                    AppIconItem(
                        app = app,
                        textColor = Color.White,
                        shadow = false,
                        showLabel = settings.showLabels,
                        iconSize = settings.iconSizeDp.dp,
                        themedIcon = settings.themedIcons,
                        onClick = { onAppClick(app.packageName) },
                        onLongClick = { onAppLongClick(app) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
    app: AppItem,
    showLabel: Boolean = true,
    textColor: Color = Color.White,
    shadow: Boolean = false,
    iconSize: Dp = 60.dp,
    themedIcon: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "icon_press_scale"
    )

    val themedColorFilter = if (themedIcon) {
        ColorFilter.tint(MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Column(
        modifier = Modifier
            .padding(6.dp)
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                colorFilter = themedColorFilter,
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = app.icon,
                contentDescription = app.label,
                colorFilter = themedColorFilter,
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            val textStyle = if (shadow) {
                androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        blurRadius = 6f
                    )
                )
            } else {
                androidx.compose.ui.text.TextStyle()
            }

            Text(
                text = app.label,
                fontSize = 11.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = textStyle,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableHomeAppItem(
    app: AppItem,
    index: Int,
    totalCount: Int,
    isReordering: Boolean,
    wobbleAngle: Float,
    showLabel: Boolean,
    showTextShadows: Boolean = false,
    iconSize: Dp,
    themedIcon: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveToDock: () -> Unit,
    onRemove: () -> Unit
) {
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }
    var visualOffsetX by remember { mutableFloatStateOf(0f) }
    var visualOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = visualOffsetX, label = "home_reorder_x")
    val animatedOffsetY by animateFloatAsState(targetValue = visualOffsetY, label = "home_reorder_y")

    val themedColorFilter = if (themedIcon) {
        ColorFilter.tint(MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Box(
        modifier = Modifier
            .padding(6.dp)
            .offset { IntOffset(animatedOffsetX.roundToInt(), animatedOffsetY.roundToInt()) }
            .rotate(if (isReordering) wobbleAngle else 0f)
            .then(
                if (isReordering) {
                    Modifier.pointerInput(index) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDragX += dragAmount.x
                                accumulatedDragY += dragAmount.y
                                visualOffsetX += dragAmount.x
                                visualOffsetY += dragAmount.y
                                
                                if (accumulatedDragX > 65f) {
                                    accumulatedDragX = 0f
                                    visualOffsetX -= 65f
                                    onMoveRight()
                                } else if (accumulatedDragX < -65f) {
                                    accumulatedDragX = 0f
                                    visualOffsetX += 65f
                                    onMoveLeft()
                                }
                                
                                if (accumulatedDragY > 150f) {
                                    accumulatedDragY = 0f
                                    visualOffsetY -= 150f
                                    onMoveToDock()
                                }
                            },
                            onDragEnd = { 
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                                visualOffsetX = 0f
                                visualOffsetY = 0f
                            },
                            onDragCancel = { 
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                                visualOffsetX = 0f
                                visualOffsetY = 0f
                            }
                        )
                    }
                } else {
                    Modifier.combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.label,
                    colorFilter = themedColorFilter,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = app.icon,
                    contentDescription = app.label,
                    colorFilter = themedColorFilter,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (showLabel) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = app.label,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = if (showTextShadows) {
                        androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.7f),
                                blurRadius = 6f
                            )
                        )
                    } else {
                        androidx.compose.ui.text.TextStyle()
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // In Reorder Mode: Top-Right Remove Badge (-)
        if (isReordering) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-4).dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Supprimer",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableDockAppItem(
    app: AppItem,
    index: Int,
    totalCount: Int,
    isReordering: Boolean,
    wobbleAngle: Float,
    iconSize: Dp,
    themedIcon: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveToHome: () -> Unit,
    onRemove: () -> Unit
) {
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }
    var visualOffsetX by remember { mutableFloatStateOf(0f) }
    var visualOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = visualOffsetX, label = "dock_reorder_x")
    val animatedOffsetY by animateFloatAsState(targetValue = visualOffsetY, label = "dock_reorder_y")

    val themedColorFilter = if (themedIcon) {
        ColorFilter.tint(MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .offset { IntOffset(animatedOffsetX.roundToInt(), animatedOffsetY.roundToInt()) }
            .rotate(if (isReordering) wobbleAngle else 0f)
            .then(
                if (isReordering) {
                    Modifier.pointerInput(index) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDragX += dragAmount.x
                                accumulatedDragY += dragAmount.y
                                visualOffsetX += dragAmount.x
                                visualOffsetY += dragAmount.y
                                if (accumulatedDragX > 50f) {
                                    accumulatedDragX = 0f
                                    visualOffsetX -= 50f
                                    onMoveRight()
                                } else if (accumulatedDragX < -50f) {
                                    accumulatedDragX = 0f
                                    visualOffsetX += 50f
                                    onMoveLeft()
                                }
                                
                                if (accumulatedDragY < -150f) {
                                    accumulatedDragY = 0f
                                    visualOffsetY += 150f
                                    onMoveToHome()
                                }
                            },
                            onDragEnd = { 
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                                visualOffsetX = 0f
                                visualOffsetY = 0f
                            },
                            onDragCancel = { 
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                                visualOffsetX = 0f
                                visualOffsetY = 0f
                            }
                        )
                    }
                } else {
                    Modifier.combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.label,
                    colorFilter = themedColorFilter,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = app.icon,
                    contentDescription = app.label,
                    colorFilter = themedColorFilter,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (isReordering) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (index > 0) {
                        IconButton(
                            onClick = onMoveLeft,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Déplacer à gauche",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    if (index < totalCount - 1) {
                        IconButton(
                            onClick = onMoveRight,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Déplacer à droite",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // In Reorder Mode: Top-Right Remove Badge (-)
        if (isReordering) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Supprimer du dock",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

