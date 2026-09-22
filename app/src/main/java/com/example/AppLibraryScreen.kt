package com.example.yourapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. نماذج البيانات (Data Models)
data class AppItem(
    val id: String,
    val name: String,
    val iconColor: Color = Color.White
)

data class FolderCategory(
    val id: String,
    val title: String,
    val apps: List<AppItem>
)

// 2. الواجهة الرئيسية (App Library Screen)
@Composable
fun AppLibraryScreen(
    categories: List<FolderCategory>,
    onFolderClick: (FolderCategory) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC7DAE5)) // خلفية زرقاء فاتحة مطابقة للصورة الثانية
            .padding(16.dp)
    ) {
        // شريط البحث العلوي
        SearchBarView()

        Spacer(modifier = Modifier.height(16.dp))

        // شبكة المجلدات الرئيسية (العنصر 1)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { category ->
                FolderCard(
                    category = category,
                    onClick = { onFolderClick(category) }
                )
            }
        }
    }
}

// شريط البحث العلوي
@Composable
fun SearchBarView() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.DarkGray)
            Spacer(modifier = Modifier.width(8.dp))
            Text("App Library", color = Color.DarkGray, fontSize = 15.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.DarkGray)
        }
    }
}

// 3. كرت المجلد الداخلي (العناصر 2 و 3)
@Composable
fun FolderCard(
    category: FolderCategory,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.4f)
        ) {
            // شبكة 2x2 داخل المجلد لعرض أيقونات التطبيقات
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(4) { index ->
                    if (index < category.apps.size) {
                        // مربع الأيقونة الممتلئ (مثل الخرائط أو الأيقونات الأخرى)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(category.apps[index].iconColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.apps[index].name.take(1),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // خانة فارغة للمجلدات غير المكتملة (مثل مجلد Travel في الصورة الثانية)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Transparent)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}
