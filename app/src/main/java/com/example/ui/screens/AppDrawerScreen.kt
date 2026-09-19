package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.speech.RecognizerIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.AppCategoryInfo
import com.example.domain.AppItem
import com.example.ui.viewmodels.LauncherSettings

// Definitions for iOS-style categories
private val categoryMetadata = listOf(
    AppCategoryInfo("social", "التواصل والرسائل", "Social", "💬", 1),
    AppCategoryInfo("utilities", "الأدوات والنظام", "Utilities", "🛠️", 2),
    AppCategoryInfo("productivity", "الإنتاجية والعمل", "Productivity", "💼", 3),
    AppCategoryInfo("media", "الترفيه والوسائط", "Media", "🎬", 4),
    AppCategoryInfo("games", "الألعاب", "Games", "🎮", 5),
    AppCategoryInfo("navigation", "الملاحة والسفر", "Navigation", "🧭", 6),
    AppCategoryInfo("shopping", "التسوق ونمط الحياة", "Shopping", "🛍️", 7),
    AppCategoryInfo("browsing", "المعلومات والتصفح", "Information", "🌐", 8),
    AppCategoryInfo("other", "تطبيقات متنوعة", "Other", "📦", 9)
)

enum class DrawerDisplayMode {
    IOS_BOXES,
    ALL_APPS_GRID
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
    var displayMode by remember { mutableStateOf(DrawerDisplayMode.IOS_BOXES) }
    var expandedCategory by remember { mutableStateOf<Pair<AppCategoryInfo, List<AppItem>>?>(null) }

    // Instant search filtering
    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) apps
        else apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    // Cached iOS smart category groupings
    val categorizedApps = remember(apps) {
        val map = mutableMapOf<String, MutableList<AppItem>>()
        categoryMetadata.forEach { map[it.id] = mutableListOf() }
        for (app in apps) {
            val list = map[app.category] ?: map["other"]!!
            list.add(app)
        }
        categoryMetadata.mapNotNull { cat ->
            val list = map[cat.id] ?: emptyList()
            if (list.isNotEmpty()) cat to list else null
        }
    }

    val isDarkTheme = isSystemInDarkTheme() || settings.wallpaperType == "dark_amoled"

    // Frosted Glass Blur styling
    val drawerBgColor = if (isDarkTheme) Color(0xFF131620).copy(alpha = 0.84f) else Color(0xFFE9EDF5).copy(alpha = 0.82f)
    val drawerBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.65f)
    val handleColor = if (isDarkTheme) Color(0xFF90939E) else Color(0xFF4A4E58)
    val pillBgColor = if (isDarkTheme) Color(0xFF242834).copy(alpha = 0.90f) else Color(0xFFF4F6FC).copy(alpha = 0.95f)
    val pillBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.07f)
    val iconTint = if (isDarkTheme) Color(0xFFC4C7D0) else Color(0xFF49454F)
    val appItemTextColor = if (isDarkTheme) Color.White else Color(0xFF1F2328)

    // Rounded drawer sheet with frosted blur styling
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = drawerBgColor,
        border = BorderStroke(1.2.dp, drawerBorderColor),
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            // Drag handle at top (dismiss gesture only on header to avoid lag in grid scroll)
            var headerDragY by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp)
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
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(handleColor)
                )
            }

            // --- Pill Search Bar (Google G, Search, Voice, Lens, Settings) ---
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
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_g),
                        contentDescription = "Google",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { launchGoogleSearch(context) }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            // --- Segmented Mode Switcher (Only visible when not actively searching) ---
            if (searchQuery.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDarkTheme) Color(0xFF222632).copy(alpha = 0.70f) else Color(0xFFE2E6F0).copy(alpha = 0.70f),
                        border = BorderStroke(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.50f)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // iOS Boxes Mode
                            val isIosActive = displayMode == DrawerDisplayMode.IOS_BOXES
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isIosActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { displayMode = DrawerDisplayMode.IOS_BOXES }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🗂️ صناديق iOS",
                                        fontSize = 12.sp,
                                        fontWeight = if (isIosActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isIosActive) MaterialTheme.colorScheme.onPrimary else if (isDarkTheme) Color(0xFFC4C7D0) else Color(0xFF4A4E58)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // All Apps Grid Mode
                            val isGridActive = displayMode == DrawerDisplayMode.ALL_APPS_GRID
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isGridActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { displayMode = DrawerDisplayMode.ALL_APPS_GRID }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📱 كل التطبيقات (${apps.size})",
                                        fontSize = 12.sp,
                                        fontWeight = if (isGridActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isGridActive) MaterialTheme.colorScheme.onPrimary else if (isDarkTheme) Color(0xFFC4C7D0) else Color(0xFF4A4E58)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Content View: Search Results OR iOS Category Boxes OR Classic Grid ---
            if (searchQuery.isNotEmpty()) {
                // Search Results Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(settings.gridColumns),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
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
                            onClick = { onAppClick(app.packageName) },
                            onLongClick = { onAppLongClick(app) }
                        )
                    }
                }
            } else when (displayMode) {
                DrawerDisplayMode.IOS_BOXES -> {
                    // --- iOS App Library Category Boxes (2 columns) ---
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 90.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = categorizedApps,
                            key = { it.first.id },
                            contentType = { "category_box" }
                        ) { (category, categoryAppList) ->
                            IosCategoryBox(
                                category = category,
                                apps = categoryAppList,
                                isDarkTheme = isDarkTheme,
                                notificationCounts = notificationCounts,
                                onAppClick = onAppClick,
                                onAppLongClick = onAppLongClick,
                                onExpandCategory = {
                                    expandedCategory = category to categoryAppList
                                }
                            )
                        }
                    }
                }
                DrawerDisplayMode.ALL_APPS_GRID -> {
                    // --- Classic A-Z Grid with Ultra-smooth 120 FPS performance ---
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(settings.gridColumns),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = apps,
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
                                onClick = { onAppClick(app.packageName) },
                                onLongClick = { onAppLongClick(app) }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Expanded iOS Category Folder Dialog ---
    expandedCategory?.let { (cat, catApps) ->
        CategoryFolderDialog(
            category = cat,
            apps = catApps,
            isDarkTheme = isDarkTheme,
            notificationCounts = notificationCounts,
            onAppClick = {
                expandedCategory = null
                onAppClick(it)
            },
            onAppLongClick = {
                expandedCategory = null
                onAppLongClick(it)
            },
            onDismissRequest = { expandedCategory = null }
        )
    }
}

// --- iOS Style Category Box Component ---
@Composable
fun IosCategoryBox(
    category: AppCategoryInfo,
    apps: List<AppItem>,
    isDarkTheme: Boolean,
    notificationCounts: Map<String, Int>,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onExpandCategory: () -> Unit
) {
    val boxBgColor = if (isDarkTheme) Color(0xFF1E222D).copy(alpha = 0.70f) else Color.White.copy(alpha = 0.55f)
    val boxBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.60f)
    val titleColor = if (isDarkTheme) Color(0xFFF0F2F7) else Color(0xFF1A1D24)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = boxBgColor,
        border = BorderStroke(1.dp, boxBorderColor),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            // Category Header with click to expand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onExpandCategory() }
                    .padding(vertical = 3.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${category.iconEmoji} ${category.titleAr}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = CircleShape,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.07f)
                ) {
                    Text(
                        text = "${apps.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor.copy(alpha = 0.75f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2x2 Grid of Apps inside the Box (iOS App Library style)
            val directApps = apps.take(if (apps.size <= 4) 4 else 3)
            val hasMoreCluster = apps.size > 4
            val remainingApps = if (hasMoreCluster) apps.drop(3).take(4) else emptyList()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1 (Items 0 and 1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (directApps.isNotEmpty()) {
                        MiniAppSlot(
                            app = directApps[0],
                            badgeCount = notificationCounts[directApps[0].packageName] ?: 0,
                            isDarkTheme = isDarkTheme,
                            onClick = { onAppClick(directApps[0].packageName) },
                            onLongClick = { onAppLongClick(directApps[0]) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (directApps.size > 1) {
                        MiniAppSlot(
                            app = directApps[1],
                            badgeCount = notificationCounts[directApps[1].packageName] ?: 0,
                            isDarkTheme = isDarkTheme,
                            onClick = { onAppClick(directApps[1].packageName) },
                            onLongClick = { onAppLongClick(directApps[1]) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Row 2 (Items 2 and 3 OR Cluster Folder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (directApps.size > 2) {
                        MiniAppSlot(
                            app = directApps[2],
                            badgeCount = notificationCounts[directApps[2].packageName] ?: 0,
                            isDarkTheme = isDarkTheme,
                            onClick = { onAppClick(directApps[2].packageName) },
                            onLongClick = { onAppLongClick(directApps[2]) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (directApps.size == 4) {
                        // Exactly 4 apps: show 4th app directly
                        MiniAppSlot(
                            app = directApps[3],
                            badgeCount = notificationCounts[directApps[3].packageName] ?: 0,
                            isDarkTheme = isDarkTheme,
                            onClick = { onAppClick(directApps[3].packageName) },
                            onLongClick = { onAppLongClick(directApps[3]) },
                            modifier = Modifier.weight(1f)
                        )
                    } else if (hasMoreCluster) {
                        // More than 4 apps: show iOS 2x2 folder thumbnail cluster
                        ClusterFolderSlot(
                            apps = remainingApps,
                            totalRemaining = apps.size - 3,
                            isDarkTheme = isDarkTheme,
                            onClick = onExpandCategory,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// Single App Slot inside iOS Box
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MiniAppSlot(
    app: AppItem,
    badgeCount: Int,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkTheme) Color.White.copy(alpha = 0.90f) else Color(0xFF1E2126)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = app.icon,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (badgeCount > 0) {
                Surface(
                    color = Color(0xFFFF3B30),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.offset(x = 4.dp, y = (-2).dp)
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else "$badgeCount",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = app.label,
            fontSize = 10.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// Mini 2x2 Cluster Folder Thumbnail inside iOS Box for remaining apps
@Composable
private fun ClusterFolderSlot(
    apps: List<AppItem>,
    totalRemaining: Int,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkTheme) Color.White.copy(alpha = 0.90f) else Color(0xFF1E2126)
    val clusterBg = if (isDarkTheme) Color(0xFF282C3A).copy(alpha = 0.85f) else Color(0xFFE2E6EE).copy(alpha = 0.90f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = clusterBg,
            border = BorderStroke(0.8.dp, if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.50f)),
            modifier = Modifier.size(46.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                // 2x2 mini icons
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (apps.isNotEmpty()) {
                            MiniThumb(apps[0])
                        }
                        if (apps.size > 1) {
                            MiniThumb(apps[1])
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (apps.size > 2) {
                            MiniThumb(apps[2])
                        }
                        if (apps.size > 3) {
                            MiniThumb(apps[3])
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "+$totalRemaining المزيد",
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MiniThumb(app: AppItem) {
    if (app.iconBitmap != null) {
        Image(
            bitmap = app.iconBitmap,
            contentDescription = null,
            modifier = Modifier
                .size(17.dp)
                .clip(RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            model = app.icon,
            contentDescription = null,
            modifier = Modifier
                .size(17.dp)
                .clip(RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

// Expanded Full Category Folder Dialog (like iOS expanding a category box)
@Composable
fun CategoryFolderDialog(
    category: AppCategoryInfo,
    apps: List<AppItem>,
    isDarkTheme: Boolean,
    notificationCounts: Map<String, Int>,
    onAppClick: (String) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onDismissRequest: () -> Unit
) {
    var dialogSearch by remember { mutableStateOf("") }
    val filtered = remember(dialogSearch, apps) {
        if (dialogSearch.isBlank()) apps
        else apps.filter { it.label.contains(dialogSearch, ignoreCase = true) }
    }

    val dialogBg = if (isDarkTheme) Color(0xFF161922).copy(alpha = 0.94f) else Color(0xFFF2F5FA).copy(alpha = 0.96f)
    val titleColor = if (isDarkTheme) Color.White else Color(0xFF1E2125)

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = dialogBg,
            border = BorderStroke(1.2.dp, if (isDarkTheme) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.70f)),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .clip(RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${category.iconEmoji} ${category.titleAr}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Text(
                            text = "${apps.size} تطبيقات",
                            fontSize = 12.sp,
                            color = titleColor.copy(alpha = 0.60f)
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = titleColor,
                                modifier = Modifier.padding(6.dp).size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search inside folder if there are many apps
                if (apps.size > 6) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDarkTheme) Color(0xFF242834) else Color(0xFFE5E9F2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = titleColor.copy(alpha = 0.50f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = dialogSearch,
                                onValueChange = { dialogSearch = it },
                                singleLine = true,
                                textStyle = TextStyle(color = titleColor, fontSize = 13.sp),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (dialogSearch.isEmpty()) {
                                            Text(
                                                "بحث في ${category.titleAr}...",
                                                color = titleColor.copy(alpha = 0.45f),
                                                fontSize = 13.sp
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Grid of all apps in this category
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = filtered,
                        key = { it.packageName },
                        contentType = { "app_icon" }
                    ) { app ->
                        AppIconItem(
                            app = app,
                            badgeCount = notificationCounts[app.packageName] ?: 0,
                            textColor = titleColor,
                            shadow = false,
                            showLabel = true,
                            iconSize = 54.dp,
                            onClick = { onAppClick(app.packageName) },
                            onLongClick = { onAppLongClick(app) }
                        )
                    }
                }
            }
        }
    }
}

// Search & Lens Helper Functions
private fun launchVoiceSearch(context: Context) {
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

private fun launchGoogleLens(context: Context) {
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

private fun launchGoogleSearch(context: Context) {
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
                TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        blurRadius = 6f
                    )
                )
            } else {
                TextStyle()
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
