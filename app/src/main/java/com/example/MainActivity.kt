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
                    onSettingsClick = { isSettingsOpen = true }
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
        if (!isDrawerOpen) {
            var dockDragAmount by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
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
                        Box(
                            modifier = Modifier
                                .size((settings.iconSizeDp - 6).dp)
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.45f),
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

    var drawerDragY by remember { mutableFloatStateOf(0f) }

    val isDarkTheme = isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled"
    val drawerBgColor = if (isDarkTheme) Color(0xEE111520) else Color(0xF5F6F8FB)
    val drawerBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val handleColor = if (isDarkTheme) Color.White.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.25f)
    val trayBgColor = if (isDarkTheme) Color(0xFF232731).copy(alpha = 0.85f) else Color(0xFFE8ECF2).copy(alpha = 0.95f)
    val trayBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
    val trayContentColor = if (isDarkTheme) Color.White else Color(0xFF1E2125)
    val appItemTextColor = if (isDarkTheme) Color.White else Color(0xFF1F2124)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { drawerDragY = 0f },
                    onDragEnd = {
                        if (drawerDragY > 35f) {
                            onClose()
                        }
                        drawerDragY = 0f
                    },
                    onDragCancel = { drawerDragY = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        drawerDragY += dragAmount
                        if (drawerDragY > 45f) {
                            change.consume()
                            onClose()
                        }
                    }
                )
            },
        color = drawerBgColor,
        border = BorderStroke(1.dp, drawerBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Drag handle pill at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
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

            // Compact Top Bar: Integrated Sleek Search Bar Tray with Settings Gear Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // In-Drawer Live Search Field Capsule / Tray
                Surface(
                    shape = CircleShape,
                    color = trayBgColor,
                    border = BorderStroke(1.dp, trayBorderColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Further reduced search icon size to 15.dp, cleanly aligned with the tray
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = trayContentColor.copy(alpha = 0.65f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = trayContentColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(trayContentColor),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Rechercher (${filteredApps.size})...",
                                            color = trayContentColor.copy(alpha = 0.55f),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Effacer la recherche",
                                    tint = trayContentColor.copy(alpha = 0.70f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Compact Settings Gear Icon Button (Tray styled, height 46.dp matching search tray)
                Surface(
                    onClick = onSettingsClick,
                    shape = CircleShape,
                    color = trayBgColor,
                    border = BorderStroke(1.dp, trayBorderColor),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = trayContentColor.copy(alpha = 0.80f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

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
                        textColor = appItemTextColor,
                        shadow = false,
                        showLabel = settings.showLabels,
                        iconSize = settings.iconSizeDp.dp,
                        themedIcon = false, // Keep vibrant, genuine app icon colors
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

    val themedColorFilter: ColorFilter? = null

    Column(
        modifier = Modifier
            .padding(6.dp)
            .scale(scale)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = app.label
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = (iconSize / 2) + 8.dp),
                role = Role.Button,
                onClickLabel = "Ouvrir ${app.label}",
                onLongClickLabel = if (onLongClick != null) "Options de ${app.label}" else null,
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
        }
    }
}

