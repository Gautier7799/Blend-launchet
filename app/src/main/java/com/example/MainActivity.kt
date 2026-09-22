package com.example

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BlendLauncherMasterApp()
            }
        }
    }
}

// 1. نظام الصفحات الأربع المتكاملة (Today View -> Spotlight Search -> Home Screen -> App Library)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlendLauncherMasterApp() {
    // تبدأ الواجهة من الصفحة الرئيسية (الصفحة index 2)
    val pagerState = rememberPagerState(initialPage = 2, pageCount = { 4 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> TodayWidgetsScreen()      // العنصر 5: صفحة الودجات الجانبية Today View
            1 -> SpotlightSearchScreen()    // العنصر 2 و 3: شاشة البحث السريع والقائمة العمودية
            2 -> HomeScreenWithWidgets()   // العنصر 4: الشاشة الرئيسية بالودجات و Dock
            3 -> AppLibraryRealScreen()     // العنصر 1: مكتبة التطبيقات بالمجلدات الزجاجية
        }
    }
}

// ==========================================
// العنصر 5: صفحة الودجات الجانبية (Today View / Widgets Page)
// ==========================================
@Composable
fun TodayWidgetsScreen() {
    val context = LocalContext.current
    val installedApps = remember { getInstalledApps(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5A758D))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ودجت الطقس الممتد
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1B6B93).copy(alpha = 0.85f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tataouine", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text("22°", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Text("Clear Sky • H:22° L:17°", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }

        // ودجت اقتراحات التطبيقات (Shorcuts Widget)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SUGGESTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    installedApps.take(4).forEach { app ->
                        val bitmap = remember(app.icon) { app.icon.toBitmap(100, 100).asImageBitmap() }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { launchApp(context, app.packageName) }
                        ) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = app.label, fontSize = 10.sp, color = Color.Black, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// العنصر 4: الشاشة الرئيسية بالودجات والشريط السفلي
// ==========================================
@Composable
fun HomeScreenWithWidgets() {
    val context = LocalContext.current
    val installedApps = remember { getInstalledApps(context) }
    val batteryLevel = remember { getBatteryLevel(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8BA2B5))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1B6B93).copy(alpha = 0.85f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tataouine", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("22°", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        Text("Broken clouds\nH:22° L:17°", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { batteryLevel / 100f },
                                modifier = Modifier.size(52.dp),
                                color = Color(0xFF4CAF50),
                                trackColor = Color.LightGray.copy(alpha = 0.4f),
                                strokeWidth = 6.dp
                            )
                        }
                        Text(text = "$batteryLevel%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(installedApps.take(12)) { app ->
                    HomeScreenAppItem(app = app, onClick = { launchApp(context, app.packageName) })
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(36.dp),
            color = Color.White.copy(alpha = 0.35f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                installedApps.take(4).forEach { app ->
                    val bitmap = remember(app.icon) { app.icon.toBitmap(100, 100).asImageBitmap() }
                    Image(
                        bitmap = bitmap,
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable { launchApp(context, app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenAppItem(app: RealAppModel, onClick: () -> Unit) {
    val bitmap = remember(app.icon) { app.icon.toBitmap(100, 100).asImageBitmap() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = app.label,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = app.label, fontSize = 11.sp, color = Color.White, maxLines = 1, textAlign = TextAlign.Center)
    }
}

// ==========================================
// العنصر 2 و 3: البحث السريع Spotlight Search
// ==========================================
@Composable
fun SpotlightSearchScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val installedApps = remember { getInstalledApps(context) }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isEmpty()) installedApps
        else installedApps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6B8A99))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Apps...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.85f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredApps) { app ->
                val bitmap = remember(app.icon) { app.icon.toBitmap(100, 100).asImageBitmap() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { launchApp(context, app.packageName) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = app.label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ==========================================
// العنصر 1: مكتبة التطبيقات App Library
// ==========================================
@Composable
fun AppLibraryRealScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val installedApps = remember { getInstalledApps(context) }
    val categories = remember(installedApps) { groupAppsIntoCategories(installedApps) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC7DAE5))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.55f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF4A5568), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("App Library", color = Color(0xFF4A5568), fontSize = 15.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF4A5568), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF4A5568), modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { folder ->
                RealFolderCard(
                    folder = folder,
                    onAppClick = { app -> launchApp(context, app.packageName) }
                )
            }
        }
    }
}

@Composable
fun RealFolderCard(
    folder: RealCategoryFolder,
    onAppClick: (RealAppModel) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    RealAppIconSlot(app = folder.apps.getOrNull(0), onAppClick = onAppClick, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    RealAppIconSlot(app = folder.apps.getOrNull(1), onAppClick = onAppClick, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.weight(1f)) {
                    RealAppIconSlot(app = folder.apps.getOrNull(2), onAppClick = onAppClick, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    RealAppIconSlot(app = folder.apps.getOrNull(3), onAppClick = onAppClick, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = folder.categoryName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3748),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RealAppIconSlot(
    app: RealAppModel?,
    onAppClick: (RealAppModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (app != null) {
        val bitmap = remember(app.icon) { app.icon.toBitmap(120, 120).asImageBitmap() }
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onAppClick(app) },
            contentAlignment = Alignment.Center
        ) {
            Image(bitmap = bitmap, contentDescription = app.label, modifier = Modifier.fillMaxSize())
        }
    } else {
        Spacer(modifier = modifier.fillMaxSize())
    }
}

// ==========================================
// إدارة التطبيقات والأذونات
// ==========================================
data class RealAppModel(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val category: String
)

data class RealCategoryFolder(
    val categoryName: String,
    val apps: List<RealAppModel>
)

fun getInstalledApps(context: Context): List<RealAppModel> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

    val list = mutableListOf<RealAppModel>()
    for (info in resolveInfos) {
        val packageName = info.activityInfo.packageName
        if (packageName == context.packageName) continue

        val label = info.loadLabel(pm).toString()
        val icon = info.loadIcon(pm)
        val category = determineCategory(packageName, label)

        list.add(RealAppModel(packageName, label, icon, category))
    }
    return list
}

fun determineCategory(packageName: String, label: String): String {
    val pkg = packageName.lowercase()
    return when {
        pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("whatsapp") ||
        pkg.contains("twitter") || pkg.contains("tiktok") || pkg.contains("telegram") || pkg.contains("snapchat") -> "Social"

        pkg.contains("youtube") || pkg.contains("spotify") || pkg.contains("music") || pkg.contains("suno") -> "Entertainment"

        pkg.contains("google") || pkg.contains("chrome") || pkg.contains("drive") ||
        pkg.contains("docs") || pkg.contains("github") || pkg.contains("calendar") -> "Productivity"

        pkg.contains("camera") || pkg.contains("calculator") || pkg.contains("clock") ||
        pkg.contains("settings") || pkg.contains("weather") -> "Utilities"

        pkg.contains("map") || pkg.contains("uber") || pkg.contains("travel") -> "Travel"

        else -> "Other"
    }
}

fun groupAppsIntoCategories(apps: List<RealAppModel>): List<RealCategoryFolder> {
    return apps.groupBy { it.category }.map { (cat, appList) ->
        RealCategoryFolder(cat, appList)
    }
}

fun launchApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    }
}

fun getBatteryLevel(context: Context): Int {
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    return if (level != -1 && scale != -1) ((level / scale.toFloat()) * 100).toInt() else 85
}
