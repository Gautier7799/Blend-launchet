package com.example.launcher.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.launcher.R
import com.example.launcher.data.model.AppItem
import com.example.launcher.data.model.FolderItem
import com.example.launcher.ui.components.SmartFolders

@Composable
fun AppLibraryScreen() {
    var searchQuery by remember { mutableStateOf("") }

    val sampleApps = remember {
        listOf(
            AppItem("1", "Camera", R.drawable.ic_launcher_foreground),
            AppItem("2", "Settings", R.drawable.ic_launcher_foreground),
            AppItem("3", "Photos", R.drawable.ic_launcher_foreground),
            AppItem("4", "Music", R.drawable.ic_launcher_foreground)
        )
    }

    val sampleFolders = remember {
        listOf(
            FolderItem("Social", sampleApps),
            FolderItem("Utilities", sampleApps.take(2))
        )
    }

    val filteredApps = remember(searchQuery) {
        if (searchQuery.isBlank()) sampleApps
        else sampleApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // شريط البحث العلوي
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("App Library") },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        if (searchQuery.isEmpty()) {
            // عرض المجلدات عند عدم وجود بحث
            SmartFolders(folders = sampleFolders)
        } else {
            // الجزء المكتمل من القائمة (LazyColumn)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = app.iconRes),
                            contentDescription = app.name,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
