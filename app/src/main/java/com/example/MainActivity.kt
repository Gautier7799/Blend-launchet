package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.ui.screens.IconResizeTouchBar
import com.example.ui.screens.PinchZoomHudPill
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.example.ui.screens.AppDrawer
import com.example.ui.screens.AddAppsBottomSheet
import com.example.ui.screens.AiAssistantBottomSheet
import com.example.ui.screens.AppActionBottomSheet
import com.example.ui.screens.AppShortcutsCard
import com.example.ui.screens.DeviceBatteryCard
import com.example.ui.screens.IosHomeWidgetsRow
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_MAIN) {
            viewModel.requestCloseOverlays()
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
    val notificationCounts by viewModel.notificationCounts.collectAsState()
    val activeNotifications by viewModel.activeNotifications.collectAsState()

    var isDrawerOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isAddAppsOpen by remember { mutableStateOf(false) }
    var isManageWidgetsOpen by remember { mutableStateOf(false) }
    var isReorderingMode by remember { mutableStateOf(false) }
    var selectedActionApp by remember { mutableStateOf<AppItem?>(null) }
    var isAiAssistantOpen by remember { mutableStateOf(false) }

    // Location Permission launcher for real-time accurate weather sync ("ma position")
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.refreshWeather()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // React to Home button or system home intent
    LaunchedEffect(viewModel) {
        viewModel.closeOverlaysTrigger.collect {
            isDrawerOpen = false
            isSettingsOpen = false
            isAddAppsOpen = false
            isManageWidgetsOpen = false
            selectedActionApp = null
            isAiAssistantOpen = false
            isReorderingMode = false
        }
    }

    // Android Back Handler: ensures back gesture/button dismisses open overlays smoothly
    BackHandler(
        enabled = isDrawerOpen || isSettingsOpen || isAddAppsOpen || isManageWidgetsOpen ||
                selectedActionApp != null || isAiAssistantOpen || isReorderingMode
    ) {
        when {
            selectedActionApp != null -> selectedActionApp = null
            isAiAssistantOpen -> isAiAssistantOpen = false
            isManageWidgetsOpen -> isManageWidgetsOpen = false
            isAddAppsOpen -> isAddAppsOpen = false
            isSettingsOpen -> isSettingsOpen = false
            isReorderingMode -> isReorderingMode = false
            isDrawerOpen -> isDrawerOpen = false
        }
    }

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
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    var isTouchResizeBarOpen by remember { mutableStateOf(false) }
    var isWidgetEditMode by remember { mutableStateOf(false) }
    var showPinchHud by remember { mutableStateOf(false) }
    var pinchZoomLevel by remember { mutableFloatStateOf(settings.iconSizeDp.toFloat()) }

    LaunchedEffect(settings.iconSizeDp) {
        pinchZoomLevel = settings.iconSizeDp.toFloat()
    }

    LaunchedEffect(showPinchHud, settings.iconSizeDp) {
        if (showPinchHud) {
            delay(2600)
            showPinchHud = false
        }
    }

    // Dynamic Day & Night Blurred Sky Canvas colors
    val isSystemDark = isSystemInDarkTheme()
    val currentHour = remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }
    val isNightTime = isSystemDark || currentHour !in 6..18 || settings.wallpaperType == "dark_amoled"

    val bgTopColor by animateColorAsState(
        targetValue = if (isNightTime) Color(0xFF0D1322) else Color(0xFF4B85DA),
        animationSpec = tween(1200),
        label = "bg_top"
    )
    val bgMidColor by animateColorAsState(
        targetValue = if (isNightTime) Color(0xFF161E33) else Color(0xFF6DA4F2),
        animationSpec = tween(1200),
        label = "bg_mid"
    )
    val bgBottomColor by animateColorAsState(
        targetValue = if (isNightTime) Color(0xFF080C16) else Color(0xFFCBE0FA),
        animationSpec = tween(1200),
        label = "bg_bottom"
    )
    val ambientGlowColor by animateColorAsState(
        targetValue = if (isNightTime) Color(0xFF4F46E5).copy(alpha = 0.28f) else Color(0xFFFFD54F).copy(alpha = 0.32f),
        animationSpec = tween(1200),
        label = "ambient_glow"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Dynamic Day / Night Blurred Atmosphere Canvas ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bgTopColor, bgMidColor, bgBottomColor)
                    )
                )
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Ambient Sun / Moon Glow Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ambientGlowColor, Color.Transparent),
                    center = Offset(size.width * 0.75f, size.height * 0.22f),
                    radius = size.width * 0.85f
                )
            )
            // Soft Secondary Nebula / Horizon Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (isNightTime) Color(0xFF6D28D9) else Color(0xFF93C5FD)).copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.20f, size.height * 0.70f),
                    radius = size.width * 0.75f
                )
            )
        }
        // Frosted blur glass scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isNightTime) Color.Black.copy(alpha = 0.16f)
                    else Color.White.copy(alpha = 0.10f)
                )
        )

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
                                } else if (accumulatedDrag > 35f && !isDrawerOpen) {
                                    if (settings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    viewModel.expandNotificationPanel(context)
                                }
                                accumulatedDrag = 0f
                            },
                            onDragCancel = { accumulatedDrag = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                accumulatedDrag += dragAmount
                                if (accumulatedDrag < -30f) {
                                    change.consume()
                                    isDrawerOpen = true
                                } else if (accumulatedDrag > 45f && !isDrawerOpen) {
                                    change.consume()
                                    if (settings.hapticFeedback) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    viewModel.expandNotificationPanel(context)
                                    accumulatedDrag = 0f
                                } else if (accumulatedDrag > 30f && isDrawerOpen) {
                                    change.consume()
                                    isDrawerOpen = false
                                }
                            }
                        )
                    }
                }
        )
        
        // --- Horizontal Pager: Page 0 & 2 = Secondary Dashboard, Page 1 = Main Home Screen ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page == 0 || page == 2) {
                // Secondary Dashboard (Minus-One Screen with Google News, Smart Firestore Search, Widgets)
                MinusOneScreen(
                    settings = settings,
                    tasks = tasks,
                    apps = apps,
                    activeNotifications = activeNotifications,
                    onAddTask = { viewModel.addTask(it) },
                    onToggleTask = { viewModel.toggleTask(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onAppClick = { viewModel.launchApp(it) },
                    onOpenDrawer = { isDrawerOpen = true },
                    onAiClick = { isAiAssistantOpen = true },
                    onSettingsClick = { isSettingsOpen = true },
                    onDismissNotification = { viewModel.dismissNotification(it) },
                    onDismissAllNotifications = { viewModel.dismissAllNotifications() },
                    onOpenNotificationSettings = { viewModel.openNotificationAccessSettings(context) },
                    onExpandNotificationShade = { viewModel.expandNotificationPanel(context) },
                    onReorderWidget = { from, to -> viewModel.reorderWidgets(from, to) },
                    onDeleteWidget = { widgetKey ->
                        if (widgetKey == "ios_home_widgets") {
                            viewModel.updateSettings(settings.copy(showIosHomeWidgets = false))
                        } else {
                            viewModel.deleteWidget(widgetKey)
                        }
                    },
                    onOpenManageWidgets = { isManageWidgetsOpen = true },
                    onResetWidgets = { viewModel.resetWidgetsToDefault() },
                    onUpdateSettings = { viewModel.updateSettings(it) }
                )
            } else {
                // Main Home Screen Content (Page 1)
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
                            if (isWidgetEditMode) {
                                isWidgetEditMode = false
                            }
                        }
                        .padding(
                            top = run {
                                val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                                val cutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
                                val safeTop = maxOf(statusBarTop, cutoutTop, 48.dp)
                                if (settings.showIosHomeWidgets && (settings.widgetPlacement == "home" || settings.widgetPlacement == "both")) {
                                    // Generous spacing safely below capsule island + user adjustment
                                    safeTop + 18.dp + settings.widgetTopSpacingDp.dp
                                } else {
                                    safeTop + 24.dp
                                }
                            },
                            start = 14.dp,
                            end = 14.dp
                        )
                ) {
                    // Top Widgets Area (iOS 17 Widgets: Weather + Battery ascending to top)
                    if (!isReorderingMode) {
                        if (settings.showIosHomeWidgets && (settings.widgetPlacement == "home" || settings.widgetPlacement == "both")) {
                            IosHomeWidgetsRow(
                                settings = settings,
                                onOpenSettings = { isSettingsOpen = true },
                                isEditMode = isWidgetEditMode,
                                onToggleEditMode = { isWidgetEditMode = !isWidgetEditMode },
                                onDeleteWidget = {
                                    viewModel.updateSettings(settings.copy(showIosHomeWidgets = false))
                                    isWidgetEditMode = false
                                },
                                onToggleStyle = {
                                    val nextStyle = if (settings.iosWidgetStyle == "pair") "quad_battery" else "pair"
                                    viewModel.updateSettings(settings.copy(iosWidgetStyle = nextStyle))
                                },
                                onLowerWidget = {
                                    viewModel.setWidgetTopSpacing(settings.widgetTopSpacingDp + 8)
                                },
                                onRaiseWidget = {
                                    viewModel.setWidgetTopSpacing(settings.widgetTopSpacingDp - 8)
                                },
                                onOpenTouchControls = { }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Home Screen Grid with dynamic reordering (strict boundary to guarantee no overlap with dock)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(settings.gridColumns),
                        contentPadding = PaddingValues(top = 2.dp, start = 4.dp, end = 4.dp, bottom = 140.dp),
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
                        glassIcon = settings.glassIcons,
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

        // --- iOS Page Indicator Dots (Home vs Secondary Screens) ---
        AnimatedVisibility(
            visible = !isDrawerOpen && !isTouchResizeBarOpen,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (pagerState.currentPage == 1) 124.dp else 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isNightTime) Color.Black.copy(alpha = 0.25f)
                        else Color.White.copy(alpha = 0.42f)
                    )
                    .border(
                        0.8.dp,
                        if (isNightTime) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.60f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    if (isNightTime) Color.White else Color(0xFF1C1C1E)
                                } else {
                                    if (isNightTime) Color.White.copy(alpha = 0.38f) else Color.Black.copy(alpha = 0.25f)
                                }
                            )
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }
        }

        // --- iOS: Ultra-Premium Glassmorphism Dock with Swipe Up to Open Drawer ---
        // Displayed on Main Home screen (page 1), hidden when on secondary screens or drawer open
        AnimatedVisibility(
            visible = !isDrawerOpen && pagerState.currentPage == 1 && !isTouchResizeBarOpen,
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
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = if (isNightTime) Color.Black.copy(alpha = 0.5f) else Color(0xFF1E3A8A).copy(alpha = 0.14f)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = if (isNightTime) {
                                listOf(
                                    Color(0xFF2C2C2E).copy(alpha = (settings.dockOpacity + 0.25f).coerceIn(0.50f, 0.85f)),
                                    Color(0xFF1C1C1E).copy(alpha = (settings.dockOpacity + 0.15f).coerceIn(0.40f, 0.75f))
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = (settings.dockOpacity + 0.35f).coerceIn(0.55f, 0.88f)),
                                    Color.White.copy(alpha = (settings.dockOpacity + 0.15f).coerceIn(0.35f, 0.65f))
                                )
                            }
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (isNightTime) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(32.dp)
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
                            glassIcon = settings.glassIcons,
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

        // --- Android: Heavy Dimmed Frosted Scrim for App Drawer ---
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isNightTime) Color.Black.copy(alpha = 0.70f)
                        else Color.Black.copy(alpha = 0.40f)
                    )
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
                isDarkTheme = isNightTime,
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
                onResizeIconsClick = {
                    isTouchResizeBarOpen = true
                },
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

        // --- Floating Pinch Zoom HUD Indicator ---
        AnimatedVisibility(
            visible = showPinchHud && !isTouchResizeBarOpen && !isDrawerOpen,
            enter = fadeIn(tween(180)) + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut(tween(180)) + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 46.dp)
        ) {
            PinchZoomHudPill(
                iconSize = settings.iconSizeDp,
                onOpenFullControls = {
                    showPinchHud = false
                    isTouchResizeBarOpen = true
                }
            )
        }

        // --- Floating Touch Icon & Widget Sizing Controller Bar ---
        AnimatedVisibility(
            visible = isTouchResizeBarOpen && !isDrawerOpen,
            enter = fadeIn(tween(220)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            exit = fadeOut(tween(180)) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
        ) {
            IconResizeTouchBar(
                currentIconSize = settings.iconSizeDp,
                currentWidgetSpacing = settings.widgetTopSpacingDp,
                onIconSizeChange = { viewModel.setIconSize(it) },
                onWidgetSpacingChange = { viewModel.setWidgetTopSpacing(it) },
                onDismiss = { isTouchResizeBarOpen = false }
            )
        }
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

/**
 * Frosted Glass Squircle Icon Container with specular sheen, blur effect and soft glow
 */
@Composable
fun GlassIconSquircle(
    iconSize: Dp,
    modifier: Modifier = Modifier,
    isGlass: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!isGlass) {
        Box(
            modifier = modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    } else {
        val cornerRadius = (iconSize * 0.28f).coerceAtLeast(14.dp)
        Box(
            modifier = modifier
                .size(iconSize)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = Color.White.copy(alpha = 0.35f),
                    ambientColor = Color.Black.copy(alpha = 0.20f)
                )
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.10f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.75f),
                            Color.White.copy(alpha = 0.20f)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Diagonal specular glass highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.36f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier.size(iconSize * 0.78f),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
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
    glassIcon: Boolean = true,
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
                GlassIconSquircle(
                    iconSize = iconSize,
                    isGlass = glassIcon
                ) {
                    if (app.iconBitmap != null) {
                        Image(
                            bitmap = app.iconBitmap,
                            contentDescription = app.label,
                            colorFilter = themedColorFilter,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(if (glassIcon) 13.dp else 18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = app.icon,
                            contentDescription = app.label,
                            colorFilter = themedColorFilter,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(if (glassIcon) 13.dp else 18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
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
    glassIcon: Boolean = true,
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
                GlassIconSquircle(
                    iconSize = iconSize,
                    isGlass = glassIcon
                ) {
                    if (app.iconBitmap != null) {
                        Image(
                            bitmap = app.iconBitmap,
                            contentDescription = app.label,
                            colorFilter = themedColorFilter,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(if (glassIcon) 13.dp else 18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = app.icon,
                            contentDescription = app.label,
                            colorFilter = themedColorFilter,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(if (glassIcon) 13.dp else 18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
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

