package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import com.example.ui.screens.MinusOneScreen
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
        applyImmersiveMode()

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyImmersiveMode()
        }
    }

    private fun applyImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
    val notificationCounts by viewModel.notificationCounts.collectAsState()

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
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })

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
                        onTap = {
                            if (isReorderingMode) {
                                isReorderingMode = false
                            }
                        },
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
        
        // --- Horizontal Pager: Page 0 = Secondary Dashboard, Page 1 = Main Home Screen ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page == 0) {
                // Secondary Dashboard (Minus-One Screen with Google News, Smart Firestore Search, Widgets)
                MinusOneScreen(
                    settings = settings,
                    tasks = tasks,
                    apps = apps,
                    onAddTask = { viewModel.addTask(it) },
                    onToggleTask = { viewModel.toggleTask(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onAppClick = { viewModel.launchApp(it) },
                    onOpenDrawer = { isDrawerOpen = true },
                    onAiClick = { isAiAssistantOpen = true },
                    onSettingsClick = { isSettingsOpen = true },
                    onReorderWidget = { from, to -> viewModel.reorderWidgets(from, to) },
                    onDeleteWidget = { widgetKey -> viewModel.deleteWidget(widgetKey) },
                    onOpenManageWidgets = { isManageWidgetsOpen = true },
                    onResetWidgets = { viewModel.resetWidgetsToDefault() }
                )
            } else {
                // Main Home Screen Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isReorderingMode) {
                                isReorderingMode = false
                            }
                        }
                        .padding(
                            top = if (settings.fullscreenMode) 24.dp else 52.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                ) {
                    // Top Widgets Row (AI Assistant, Date, Weather, Battery, Clock - customizable from Settings)
                    if (!isReorderingMode) {
                        TopWidgetsBar(
                            settings = settings,
                            onAiClick = { isAiAssistantOpen = true },
                            onSettingsClick = { isSettingsOpen = true }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Home Screen Grid with dynamic reordering (strict boundary to guarantee no overlap with dock)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(settings.gridColumns),
                        contentPadding = PaddingValues(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 170.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // App Grid Items
                itemsIndexed(
                    items = homeApps,
                    key = { _, app -> app.packageName }
                ) { index, app ->
                    ReorderableHomeAppItem(
                        app = app,
                        index = index,
                        totalCount = homeApps.size,
                        badgeCount = notificationCounts[app.packageName] ?: 0,
                        isReordering = isReorderingMode,
                        wobbleAngle = if (index % 2 == 0) wobbleAngle else -wobbleAngle,
                        showLabel = settings.showLabels,
                        showTextShadows = settings.showTextShadows,
                        iconSize = settings.iconSizeDp.dp,
                        themedIcon = settings.themedIcons,
                        onClick = {
                            if (!isReorderingMode) {
                                viewModel.launchApp(app.packageName)
                            } else {
                                isReorderingMode = false
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
                        onTapToExit = {
                            isReorderingMode = false
                        }
                    )
                }

                // Add Apps "+" Button Slot
                if (!isReorderingMode) {
                    item {
                        Column(
                            modifier = Modifier
                                .padding(6.dp)
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .semantics(mergeDescendants = true) {
                                    role = Role.Button
                                    contentDescription = "Ajouter des applications à l'écran d'accueil"
                                }
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Ajouter des applications",
                                    onClick = { isAddAppsOpen = true }
                                ),
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
                                    contentDescription = "Ajouter à l'écran d'accueil",
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
    }
}

        // --- iOS: Ultra-Premium Glassmorphism Dock with Swipe Up to Open Drawer ---
        // Hidden when on the Widget screen (page 0) to maximize space for widgets, and when Drawer is open
        AnimatedVisibility(
            visible = !isDrawerOpen && pagerState.currentPage != 0,
            enter = fadeIn(tween(220)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            exit = fadeOut(tween(180)) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            var dockDragAmount by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(92.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (isReorderingMode) {
                            isReorderingMode = false
                        }
                    }
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
                            colors = if (isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled") {
                                listOf(
                                    Color(0xFF242832).copy(alpha = (settings.dockOpacity + 0.35f).coerceAtMost(0.88f)),
                                    Color(0xFF161920).copy(alpha = (settings.dockOpacity + 0.20f).coerceAtMost(0.68f))
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = (settings.dockOpacity + 0.35f).coerceAtMost(0.88f)),
                                    Color.White.copy(alpha = (settings.dockOpacity + 0.15f).coerceAtMost(0.62f))
                                )
                            }
                        )
                    )
                    .then(
                        if (settings.showDockLines) {
                            val isDark = isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled"
                            Modifier.border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = if (isDark) {
                                        listOf(
                                            Color.White.copy(alpha = 0.30f),
                                            Color.White.copy(alpha = 0.08f)
                                        )
                                    } else {
                                        listOf(
                                            Color.Black.copy(alpha = 0.16f),
                                            Color.Black.copy(alpha = 0.05f)
                                        )
                                    }
                                ),
                                shape = RoundedCornerShape(32.dp)
                            )
                        } else {
                            val isDark = isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled"
                            Modifier.border(
                                width = 1.dp,
                                color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(32.dp)
                            )
                        }
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
                            badgeCount = notificationCounts[app.packageName] ?: 0,
                            isReordering = isReorderingMode,
                            wobbleAngle = wobbleAngle,
                            iconSize = (settings.iconSizeDp - 6).dp,
                            themedIcon = settings.themedIcons,
                            onClick = { 
                                if (!isReorderingMode) {
                                    viewModel.launchApp(app.packageName)
                                } else {
                                    isReorderingMode = false
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
                            onTapToExit = {
                                isReorderingMode = false
                            }
                        )
                    }

                    if (isReorderingMode && dockApps.size < settings.dockCount) {
                        val isDarkTheme = isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled"
                        val plusButtonBg = if (isDarkTheme) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.10f)
                        val plusButtonBorder = if (isDarkTheme) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.20f)
                        val plusIconColor = if (isDarkTheme) Color.White else Color(0xFF1E2125)

                        Box(
                            modifier = Modifier
                                .size((settings.iconSizeDp - 6).dp)
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(plusButtonBg)
                                .border(
                                    width = 1.dp,
                                    color = plusButtonBorder,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Ajouter une application au dock"
                                }
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Ajouter au dock",
                                    onClick = { isAddAppsOpen = true }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ajouter au dock",
                                tint = plusIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Android: Dimmed Backdrop Scrim for App Drawer ---
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isDrawerOpen = false }
                    )
            )
        }

        // --- Android: Enhanced App Drawer with Search ---
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AppDrawer(
                apps = apps,
                settings = settings,
                notificationCounts = notificationCounts,
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
                onResetOrder = { viewModel.resetWidgetsToDefault() },
                onDismissRequest = { isManageWidgetsOpen = false }
            )
        }
    }
}

@Composable
fun AppDrawer(
    apps: List<AppItem>,
    settings: LauncherSettings,
    notificationCounts: Map<String, Int> = emptyMap(),
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onClose: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) apps
        else apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    val isDarkTheme = isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled"
    // Exact cool tone from screenshot
    val drawerBgColor = if (isDarkTheme) Color(0xFF181B22) else Color(0xFFE2E6EE)
    val drawerBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f)
    val handleColor = if (isDarkTheme) Color(0xFF8E9199) else Color(0xFF4A4E58)
    
    // Search pill color
    val pillBgColor = if (isDarkTheme) Color(0xFF282C36) else Color(0xFFF3F5FA)
    val pillBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)
    val iconTint = if (isDarkTheme) Color(0xFFC4C7D0) else Color(0xFF49454F)
    val appItemTextColor = if (isDarkTheme) Color.White else Color(0xFF1F2328)

    // Rounded drawer sheet with generous curved corners as in the screenshot
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = drawerBgColor,
        border = BorderStroke(1.dp, drawerBorderColor),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            // Top Drag Handle & Gesture Dismiss area (Only on header to eliminate lag in grid scroll)
            var headerDragY by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { headerDragY = 0f },
                            onDragEnd = {
                                if (headerDragY > 30f) onClose()
                                headerDragY = 0f
                            },
                            onDragCancel = { headerDragY = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                headerDragY += dragAmount
                                if (headerDragY > 40f) {
                                    change.consume()
                                    onClose()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(handleColor)
                )
            }

            // --- Integrated Search Pill Bar with Google G, Voice, Lens & Settings ---
            Surface(
                shape = CircleShape,
                color = pillBgColor,
                border = BorderStroke(1.dp, pillBorderColor),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Official 4-color Google "G" logo
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_g),
                        contentDescription = "Google",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable {
                                launchGoogleSearch(context)
                            }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Live Search Field
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = if (isDarkTheme) Color.White else Color(0xFF1F2328),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Rechercher...",
                                        color = if (isDarkTheme) Color(0xFF8E9199) else Color(0xFF74777F),
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Effacer la recherche",
                                tint = iconTint,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Microphone (Voice Search)
                    IconButton(
                        onClick = { launchVoiceSearch(context) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Recherche vocale",
                            tint = iconTint,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // Google Lens / Camera Viewfinder
                    IconButton(
                        onClick = { launchGoogleLens(context) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_lens),
                            contentDescription = "Google Lens",
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings Gear Button ("+ setinge")
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of Filtered Apps (Ultra-smooth 120 FPS performance with zero drag conflicts)
            LazyVerticalGrid(
                columns = GridCells.Fixed(settings.gridColumns),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = filteredApps,
                    key = { it.packageName },
                    contentType = { "app_icon" }
                ) { app ->
                    AppIconItem(
                        app = app,
                        badgeCount = notificationCounts[app.packageName] ?: 0,
                        textColor = appItemTextColor,
                        shadow = false,
                        showLabel = settings.showLabels,
                        iconSize = settings.iconSizeDp.dp,
                        themedIcon = false,
                        onClick = { onAppClick(app.packageName) },
                        onLongClick = { onAppLongClick(app) }
                    )
                }
            }
        }
    }
}

// Search & Lens Helper Functions
private fun launchVoiceSearch(context: android.content.Context) {
    try {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallback = Intent(RecognizerIntent.ACTION_WEB_SEARCH).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (_: Exception) {}
    }
}

private fun launchGoogleLens(context: android.content.Context) {
    try {
        val lensIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("googlelens://v1")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(lensIntent)
    } catch (_: Exception) {
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(cameraIntent)
        } catch (_: Exception) {}
    }
}

private fun launchGoogleSearch(context: android.content.Context) {
    try {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (_: Exception) {}
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
    app: AppItem,
    badgeCount: Int = 0,
    showLabel: Boolean = true,
    textColor: Color = Color.White,
    shadow: Boolean = false,
    iconSize: Dp = 60.dp,
    themedIcon: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .padding(vertical = 5.dp, horizontal = 4.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = app.label
            }
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                indication = ripple(bounded = false, radius = (iconSize / 2) + 8.dp),
                interactionSource = remember { MutableInteractionSource() },
                onClickLabel = "Ouvrir ${app.label}",
                onLongClickLabel = if (onLongClick != null) "Options de ${app.label}" else null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = app.icon,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (badgeCount > 0) {
                Surface(
                    color = Color(0xFFFF3B30),
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, Color.White),
                    modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else "$badgeCount",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
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
    badgeCount: Int = 0,
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
    onTapToExit: () -> Unit
) {
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }
    var visualOffsetX by remember { mutableFloatStateOf(0f) }
    var visualOffsetY by remember { mutableFloatStateOf(0f) }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = visualOffsetX, label = "home_reorder_x")
    val animatedOffsetY by animateFloatAsState(targetValue = visualOffsetY, label = "home_reorder_y")

    val themedColorFilter: ColorFilter? = null

    Box(
        modifier = Modifier
            .padding(6.dp)
            .offset { IntOffset(animatedOffsetX.roundToInt(), animatedOffsetY.roundToInt()) }
            .rotate(if (isReordering) wobbleAngle else 0f)
            .then(
                if (isReordering) {
                    Modifier.pointerInput(index) {
                        detectDragGestures(
                            onDragStart = {
                                dragDistance = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragDistance += kotlin.math.abs(dragAmount.x) + kotlin.math.abs(dragAmount.y)
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
                                if (dragDistance < 10f) {
                                    onTapToExit()
                                }
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
                    Modifier
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            contentDescription = app.label
                        }
                        .combinedClickable(
                            indication = ripple(bounded = false, radius = (iconSize / 2) + 8.dp),
                            interactionSource = remember { MutableInteractionSource() },
                            role = Role.Button,
                            onClickLabel = "Ouvrir ${app.label}",
                            onLongClickLabel = "Réorganiser l'écran d'accueil",
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
            Box(contentAlignment = Alignment.TopEnd) {
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

                if (badgeCount > 0) {
                    Surface(
                        color = Color(0xFFFF3B30),
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else "$badgeCount",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableDockAppItem(
    app: AppItem,
    index: Int,
    totalCount: Int,
    badgeCount: Int = 0,
    isReordering: Boolean,
    wobbleAngle: Float,
    iconSize: Dp,
    themedIcon: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveToHome: () -> Unit,
    onTapToExit: () -> Unit
) {
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }
    var visualOffsetX by remember { mutableFloatStateOf(0f) }
    var visualOffsetY by remember { mutableFloatStateOf(0f) }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = visualOffsetX, label = "dock_reorder_x")
    val animatedOffsetY by animateFloatAsState(targetValue = visualOffsetY, label = "dock_reorder_y")

    val themedColorFilter: ColorFilter? = null

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .offset { IntOffset(animatedOffsetX.roundToInt(), animatedOffsetY.roundToInt()) }
            .rotate(if (isReordering) wobbleAngle else 0f)
            .then(
                if (isReordering) {
                    Modifier.pointerInput(index) {
                        detectDragGestures(
                            onDragStart = {
                                dragDistance = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragDistance += kotlin.math.abs(dragAmount.x) + kotlin.math.abs(dragAmount.y)
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
                                if (dragDistance < 10f) {
                                    onTapToExit()
                                }
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
                    Modifier
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            contentDescription = "${app.label}, dock"
                        }
                        .combinedClickable(
                            indication = ripple(bounded = false, radius = (iconSize / 2) + 8.dp),
                            interactionSource = remember { MutableInteractionSource() },
                            role = Role.Button,
                            onClickLabel = "Ouvrir ${app.label}",
                            onLongClickLabel = "Réorganiser le dock",
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
            Box(contentAlignment = Alignment.TopEnd) {
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

                if (badgeCount > 0) {
                    Surface(
                        color = Color(0xFFFF3B30),
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else "$badgeCount",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

