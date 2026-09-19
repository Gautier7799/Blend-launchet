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
    AppCategoryInfo("social", "التواصل", "Social", "💬", 1),
    AppCategoryInfo("utilities", "الأدوات", "Utilities", "🛠️", 2),
    AppCategoryInfo("productivity", "الإنتاجية", "Productivity", "💼", 3),
    AppCategoryInfo("media", "الترفيه والوسائط", "Entertainment", "🎬", 4),
    AppCategoryInfo("games", "الألعاب", "Games", "🎮", 5),
    AppCategoryInfo("navigation", "الملاحة والسفر", "Travel", "🧭", 6),
    AppCategoryInfo("shopping", "التسوق", "Shopping", "🛍️", 7),
    AppCategoryInfo("browsing", "المعلومات", "Information", "🌐", 8),
    AppCategoryInfo("other", "أخرى", "Other", "📦", 9)
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

    // Frosted Glass styling matching iOS App Library
    val drawerBgColor = Color.Black.copy(alpha = 0.20f)
    val drawerBorderColor = Color.White.copy(alpha = 0.18f)
    val handleColor = Color.White.copy(alpha = 0.45f)
    val pillBgColor = Color.White.copy(alpha = 0.20f)
    val pillBorderColor = Color.White.copy(alpha = 0.35f)
    val iconTint = Color.White.copy(alpha = 0.85f)
    val appItemTextColor = Color.White

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
        border = BorderStroke(1.dp, drawerBorderColor),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Drag handle at top (dismiss gesture only on header to avoid lag in grid scroll)
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
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(handleColor)
                )
            }

            // --- iOS App Library Pill Search Bar ---
            Surface(
                shape = CircleShape,
                color = pillBgColor,
                border = BorderStroke(1.dp, pillBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = iconTint,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    val searchPlaceholder = if (java.util.Locale.getDefault().language == "ar") "مكتبة التطبيقات" else "App Library"

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        searchPlaceholder,
                                        color = Color.White.copy(alpha = 0.65f),
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
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Recherche vocale",
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = iconTint,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Content View: Search Results OR iOS App Library Category Boxes ---
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
                            shadow = true,
                            showLabel = settings.showLabels,
                            iconSize = settings.iconSizeDp.dp,
                            onClick = { onAppClick(app.packageName) },
                            onLongClick = { onAppLongClick(app) }
                        )
                    }
                }
            } else {
                // --- Pure iOS App Library Category Boxes (2 columns) ---
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
    val boxBgColor = Color.White.copy(alpha = if (isDarkTheme) 0.14f else 0.24f)
    val boxBorderColor = Color.White.copy(alpha = if (isDarkTheme) 0.22f else 0.42f)
    val currentLocale = java.util.Locale.getDefault().language
    val categoryTitle = if (currentLocale == "ar") category.titleAr else category.titleEn

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = boxBgColor,
            border = BorderStroke(1.dp, boxBorderColor),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(26.dp))
                .clickable { onExpandCategory() }
        ) {
            val directApps = apps.take(if (apps.size <= 4) 4 else 3)
            val hasMoreCluster = apps.size > 4
            val remainingApps = if (hasMoreCluster) apps.drop(3).take(4) else emptyList()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(11.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1 (Items 0 and 1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (directApps.isNotEmpty()) {
                        MiniAppSlot(
                            app = directApps[0],
                            badgeCount = notificationCounts[directApps[0].packageName] ?: 0,
                            onClick = { onAppClick(directApps[0].packageName) },
                            onLongClick = { onAppLongClick(directApps[0]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(52.dp))
                    }

                    if (directApps.size > 1) {
                        MiniAppSlot(
                            app = directApps[1],
                            badgeCount = notificationCounts[directApps[1].packageName] ?: 0,
                            onClick = { onAppClick(directApps[1].packageName) },
                            onLongClick = { onAppLongClick(directApps[1]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(52.dp))
                    }
                }

                // Row 2 (Items 2 and 3 OR Cluster Folder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (directApps.size > 2) {
                        MiniAppSlot(
                            app = directApps[2],
                            badgeCount = notificationCounts[directApps[2].packageName] ?: 0,
                            onClick = { onAppClick(directApps[2].packageName) },
                            onLongClick = { onAppLongClick(directApps[2]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(52.dp))
                    }

                    if (directApps.size == 4) {
                        // Exactly 4 apps: show 4th app directly
                        MiniAppSlot(
                            app = directApps[3],
                            badgeCount = notificationCounts[directApps[3].packageName] ?: 0,
                            onClick = { onAppClick(directApps[3].packageName) },
                            onLongClick = { onAppLongClick(directApps[3]) }
                        )
                    } else if (hasMoreCluster) {
                        // More than 4 apps: show iOS 2x2 folder thumbnail cluster
                        ClusterFolderSlot(
                            apps = remainingApps,
                            isDarkTheme = isDarkTheme,
                            onClick = onExpandCategory
                        )
                    } else {
                        Spacer(modifier = Modifier.size(52.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Clean Category Label below the box
        Text(
            text = categoryTitle,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.70f),
                    blurRadius = 4f
                )
            )
        )
    }
}

// Single App Slot inside iOS Box (No text label, clean iOS squircle)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MiniAppSlot(
    app: AppItem,
    badgeCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
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

        if (badgeCount > 0) {
            Surface(
                color = Color(0xFFFF3B30),
                shape = CircleShape,
                border = BorderStroke(1.2.dp, Color.White),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-2).dp)
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
}

// Mini 2x2 Cluster Folder Thumbnail inside iOS Box (No text label, clean frosted mini-folder)
@Composable
private fun ClusterFolderSlot(
    apps: List<AppItem>,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clusterBg = Color.White.copy(alpha = if (isDarkTheme) 0.16f else 0.25f)
    val clusterBorder = Color.White.copy(alpha = if (isDarkTheme) 0.20f else 0.40f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = clusterBg,
        border = BorderStroke(0.8.dp, clusterBorder),
        modifier = modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (apps.isNotEmpty()) {
                        MiniThumb(apps[0])
                    } else {
                        Spacer(modifier = Modifier.size(20.dp))
                    }
                    if (apps.size > 1) {
                        MiniThumb(apps[1])
                    } else {
                        Spacer(modifier = Modifier.size(20.dp))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (apps.size > 2) {
                        MiniThumb(apps[2])
                    } else {
                        Spacer(modifier = Modifier.size(20.dp))
                    }
                    if (apps.size > 3) {
                        MiniThumb(apps[3])
                    } else {
                        Spacer(modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
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
                    val currentLang = java.util.Locale.getDefault().language
                    val folderTitle = if (currentLang == "ar") category.titleAr else category.titleEn
                    val subtitle = if (currentLang == "ar") "${apps.size} تطبيقات" else "${apps.size} apps"

                    Column {
                        Text(
                            text = "${category.iconEmoji} $folderTitle",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Text(
                            text = subtitle,
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
