package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
                AppLibraryRealScreen()
            }
        }
    }
}

// نموذج البيانات للتطبيق الحقيقي
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
    
    // جلب التطبيقات المثبتة الحقيقية من النظام
    val installedApps = remember { getInstalledApps(context) }
    val categories = remember(installedApps) { groupAppsIntoCategories(installedApps) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC7DAE5)) // خلفية زرقاء فاتحة ناعمة طبق الأصل
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // شريط البحث العلوي الزجاجي
        AppLibrarySearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // شبكة المجلدات الرئيسية
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
            color = Color.White.copy(alpha = 0.45f) // خلفية زجاجية شفافة للمجلد
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
        val bitmap = remember(app.icon) {
            app.icon.toBitmap(120, 120).asImageBitmap()
        }
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

// دالة جلب التطبيقات المثبتة من النظام
fun getInstalledApps(context: Context): List<RealAppModel> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
    
    val list = mutableListOf<RealAppModel>()
    for (info in resolveInfos) {
        val packageName = info.activityInfo.packageName
        if (packageName == context.packageName) continue // تجنب إظهار المشغل نفسه
        
        val label = info.loadLabel(pm).toString()
        val icon = info.loadIcon(pm)
        val category = determineCategory(packageName, label)
        
        list.add(RealAppModel(packageName, label, icon, category))
    }
    return list
}

// تصنيف التطبيقات الحقيقية إلى مجلدات
fun determineCategory(packageName: String, label: String): String {
    val pkg = packageName.lowercase()
    val lbl = label.lowercase()
    
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
    val grouped = apps.groupBy { it.category }
    return grouped.map { (cat, appList) ->
        RealCategoryFolder(cat, appList)
    }
}

fun launchApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    }
}
