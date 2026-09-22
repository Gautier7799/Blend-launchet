package com.gautier7799.blend.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. نماذج البيانات للتطبيق والمجلد
data class DrawerAppItem(
    val id: String,
    val name: String,
    val iconColor: Color = Color(0xFF1E88E5)
)

data class DrawerCategory(
    val id: String,
    val name: String,
    val apps: List<DrawerAppItem>
)

// 2. الواجهة الرئيسية لـ AppDrawer
@Composable
fun AppDrawer(
    categories: List<DrawerCategory> = getSampleCategories(),
    onAppClick: (DrawerAppItem) -> Unit = {},
    onCategoryClick: (DrawerCategory) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC2D6E3)) // لون الخلفية الفاتح المطابق للواجهة
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // شريط البحث العلوي (App Library)
        AppLibrarySearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // شبكة المجلدات والتصنيفات (العنصر 1 و 2 و 3)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { category ->
                CategoryFolderCard(
                    category = category,
                    onAppClick = onAppClick,
                    onCategoryClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

// شريط البحث العلوي
@Composable
fun AppLibrarySearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        color = Color.White.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF4A5568),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (query.isEmpty()) "App Library" else query,
                color = Color(0xFF4A5568),
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Search",
                tint = Color(0xFF4A5568),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF4A5568),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// تصميم المجلد الداخلي (مثل Social, Productivity, Travel)
@Composable
fun CategoryFolderCard(
    category: DrawerCategory,
    onAppClick: (DrawerAppItem) -> Unit,
    onCategoryClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable { onCategoryClick() },
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppIconSlot(app = category.apps.getOrNull(0), onAppClick = onAppClick, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    AppIconSlot(app = category.apps.getOrNull(1), onAppClick = onAppClick, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppIconSlot(app = category.apps.getOrNull(2), onAppClick = onAppClick, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    AppIconSlot(app = category.apps.getOrNull(3), onAppClick = onAppClick, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3748),
            textAlign = TextAlign.Center
        )
    }
}

// أيقونة التطبيق داخل المجلد (تتعامل مع الأيقونات والمساحات الفارغة مثل مجلد Travel)
@Composable
fun AppIconSlot(
    app: DrawerAppItem?,
    onAppClick: (DrawerAppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (app != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(app.iconColor)
                .clickable { onAppClick(app) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.name.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    } else {
        Spacer(modifier = modifier.fillMaxSize())
    }
}

// بيانات المجلدات والتطبيقات التجريبية
fun getSampleCategories(): List<DrawerCategory> {
    return listOf(
        DrawerCategory(
            id = "1",
            name = "Social",
            apps = listOf(
                DrawerAppItem("1", "FB", Color(0xFF1877F2)),
                DrawerAppItem("2", "IG", Color(0xFFE4405F)),
                DrawerAppItem("3", "WA", Color(0xFF25D366)),
                DrawerAppItem("4", "PN", Color(0xFFBD081C))
            )
        ),
        DrawerCategory(
            id = "2",
            name = "Utilities",
            apps = listOf(
                DrawerAppItem("5", "Cam", Color(0xFF4A5568)),
                DrawerAppItem("6", "Calc", Color(0xFF38A169)),
                DrawerAppItem("7", "Clock", Color(0xFFDD6B20)),
                DrawerAppItem("8", "Weather", Color(0xFF3182CE))
            )
        ),
        DrawerCategory(
            id = "3",
            name = "Productivity",
            apps = listOf(
                DrawerAppItem("9", "Cal", Color(0xFF3182CE)),
                DrawerAppItem("10", "Docs", Color(0xFF4299E1)),
                DrawerAppItem("11", "Drive", Color(0xFF38A169)),
                DrawerAppItem("12", "Git", Color(0xFF2D3748))
            )
        ),
        DrawerCategory(
            id = "4",
            name = "Entertainment",
            apps = listOf(
                DrawerAppItem("13", "Music", Color(0xFFDD6B20)),
                DrawerAppItem("14", "SUNO", Color(0xFFE53E3E)),
                DrawerAppItem("15", "TikTok", Color(0xFF1A202C)),
                DrawerAppItem("16", "YT", Color(0xFFE53E3E))
            )
        ),
        DrawerCategory(
            id = "5",
            name = "Travel",
            // خانة واحدة متوفرة وخانات فارغة مطابقة للصورة الثانية (Travel)
            apps = listOf(
                DrawerAppItem("17", "Maps", Color(0xFF38A169))
            )
        ),
        DrawerCategory(
            id = "6",
            name = "Shopping",
            apps = listOf(
                DrawerAppItem("18", "Shop", Color(0xFF805AD5)),
                DrawerAppItem("19", "Store", Color(0xFFD69E2E))
            )
        )
    )
}
