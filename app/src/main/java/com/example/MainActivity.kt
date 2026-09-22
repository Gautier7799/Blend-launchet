package com.example

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BlendFullLauncherApp()
            }
        }
    }
}

// 1. التطبيق الكامل يدمج الرئيسية ومكتبة التطبيقات عبر السحب
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlendFullLauncherApp() {
    val pagerState = rememberPagerState(pageCount = { 2 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> HomeScreen() // الصفحة الرئيسية للتطبيق
            1 -> AppLibraryRealScreen() // صفحة مكتبة التطبيقات (عند السحب)
        }
    }
}

// 2. الشاشة الرئيسية للتطبيق (Home Screen)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val currentDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB0C4DE)) // خلفية اللانشر الرئيسية
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // الـ Widget العلوي (الساعة والتاريخ)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = currentTime,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = currentDate,
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // إشارة التمرير لمكتبة التطبيقات
        Surface(
            color = Color.White.copy(alpha = 0.3f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = "Swipe left for App Library ➔",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // الشريط السفلي (Dock)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White.copy(alpha = 0.25f), shape = RoundedCornerShape(32.dp))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val installedApps = remember { getInstalledApps(context) }
            val dockApps = installedApps.take(4) // أول 4 تطبيقات في الشريط السفلي

            dockApps.forEach { app ->
                val bitmap = remember(app.icon) { app.icon.toBitmap(100, 100).asImageBitmap() }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { launchApp(context, app.packageName) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = app.label,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// 3. شاشة مكتبة التطبيقات الحقيقية (App Library)
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
        AppLibrarySearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

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
fun AppLibrarySearchBar(
    query: String,
    onQueryChange: (String) -> Unit
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
            Text(
                text = if (query.isEmpty()) "App Library" else query,
                color = Color(0xFF4A5568),
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF4A5568), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF4A5568), modifier = Modifier.size(20.dp))
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
            Image(
                bitmap = bitmap,
                contentDescription = app.label,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Spacer(modifier = modifier.fillMaxSize())
    }
}

// دالة قراءة التطبيقات من نظام الأندرويد
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
