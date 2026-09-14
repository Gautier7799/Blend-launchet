package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DocCloudApplication
import com.example.domain.AppItem
import com.example.model.Document
import com.example.ui.viewmodels.LauncherSettings
import com.example.ui.viewmodels.LauncherTask
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MinusOneScreen(
    settings: LauncherSettings,
    tasks: List<LauncherTask> = emptyList(),
    apps: List<AppItem> = emptyList(),
    onAddTask: (String) -> Unit = {},
    onToggleTask: (String) -> Unit = {},
    onDeleteTask: (String) -> Unit = {},
    onAppClick: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onAiClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = if (settings.fullscreenMode) 24.dp else 52.dp,
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Google Web Search Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        contentDescription = "Rechercher sur Google"
                    }
                    .clickable { 
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Rechercher sur le Web & Google...",
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                }
            }
        }

        // 2. Firestore Document Smart Search Widget
        item {
            FirestoreDocumentSearchWidget()
        }

        // 3. Compact Top Widgets Bar (Date, Weather, Clock, Battery)
        if (settings.showWeatherWidget || settings.showDateWidget || settings.showClockWidget || settings.showBatteryWidget) {
            item {
                TopWidgetsBar(
                    settings = settings,
                    onAiClick = onAiClick,
                    onSettingsClick = onSettingsClick
                )
            }
        }

        // 4. Automated Dashboard Widgets (Battery, Music, Tasks, App Shortcuts)
        // Automatically added to secondary dashboard as requested by user
        if (settings.showDeviceCardWidget) {
            item {
                DeviceBatteryCard(
                    settings = settings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (settings.showMusicWidget) {
            item {
                MusicPlayerCard(
                    settings = settings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (settings.showTasksWidget) {
            item {
                TasksCard(
                    tasks = tasks,
                    onAddTask = onAddTask,
                    onToggleTask = onToggleTask,
                    onDeleteTask = onDeleteTask,
                    settings = settings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (settings.showAppShortcutsWidget) {
            item {
                AppShortcutsCard(
                    apps = apps,
                    settings = settings,
                    onAppClick = onAppClick,
                    onOpenDrawer = onOpenDrawer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 5. Google News Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Actualités",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://news.google.com")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                ) {
                    Text(
                        text = "Voir tout",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 6. Google News Feed Items
        items(5) { index ->
            NewsCard(index = index)
        }
    }
}

/**
 * Smart Search Widget querying documents indexed in Firestore
 */
@Composable
fun FirestoreDocumentSearchWidget(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var allDocuments by remember { mutableStateOf<List<Document>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Tous") }
    var selectedDocument by remember { mutableStateOf<Document?>(null) }

    // Query documents from Firestore repository
    fun loadDocuments() {
        coroutineScope.launch {
            try {
                isLoading = true
                val app = context.applicationContext as? DocCloudApplication
                val docs = app?.container?.documentRepository?.getDocuments() ?: emptyList()
                allDocuments = docs
            } catch (_: Exception) {
                allDocuments = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadDocuments()
    }

    val filteredDocs = remember(searchQuery, selectedFilter, allDocuments) {
        allDocuments.filter { doc ->
            val matchesQuery = searchQuery.isBlank() ||
                    doc.title.contains(searchQuery, ignoreCase = true) ||
                    doc.content.contains(searchQuery, ignoreCase = true) ||
                    doc.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesFilter = when (selectedFilter) {
                "PDF" -> doc.type.equals("pdf", ignoreCase = true)
                "Images" -> doc.type.equals("image", ignoreCase = true) || doc.type.equals("jpg", ignoreCase = true) || doc.type.equals("png", ignoreCase = true)
                "Texte" -> doc.type.equals("txt", ignoreCase = true) || doc.type.equals("text", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recherche Cloud & Fichiers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Fichiers indexés dans Firestore (${allDocuments.size})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = { loadDocuments() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualiser les fichiers",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search input field
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Rechercher un fichier, document ou mot-clé...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Effacer",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Tous", "PDF", "Images", "Texte").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Results List
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            } else if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Aucun document trouvé pour \"$searchQuery\"" else "Aucun document indexé pour le moment",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredDocs.take(4).forEach { doc ->
                        DocumentResultItem(
                            document = doc,
                            onClick = { selectedDocument = doc }
                        )
                    }
                }
            }
        }
    }

    // Document detail dialog
    selectedDocument?.let { doc ->
        AlertDialog(
            onDismissRequest = { selectedDocument = null },
            icon = {
                Icon(
                    imageVector = when (doc.type.lowercase()) {
                        "pdf" -> Icons.Default.PictureAsPdf
                        "image" -> Icons.Default.Image
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = doc.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Type : ${doc.type.uppercase()}", fontSize = 13.sp)
                    if (doc.uploadDate > 0) {
                        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(doc.uploadDate))
                        Text("Date d'ajout : $dateStr", fontSize = 12.sp)
                    }
                    if (doc.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = doc.content,
                            fontSize = 13.sp,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (doc.storageUrl.isNotBlank() && doc.storageUrl.startsWith("http")) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.storageUrl)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                            selectedDocument = null
                        }
                    ) {
                        Text("Ouvrir le fichier")
                    }
                } else {
                    TextButton(onClick = { selectedDocument = null }) {
                        Text("Fermer")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDocument = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun DocumentResultItem(
    document: Document,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconVector = when (document.type.lowercase()) {
                "pdf" -> Icons.Default.PictureAsPdf
                "image", "jpg", "png" -> Icons.Default.Image
                else -> Icons.Default.Description
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${document.type.uppercase()} • ${if (document.size > 0) "${document.size / 1024} KB" else "Cloud"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun NewsCard(index: Int) {
    val context = LocalContext.current
    val titles = listOf(
        "Découvrez les dernières nouveautés d'Android 15 pour votre téléphone",
        "L'Intelligence Artificielle transforme notre façon d'interagir avec les applications",
        "Les 10 astuces indispensables pour optimiser la batterie de votre smartphone",
        "Nouveaux modèles de téléphones : ce qu'il faut attendre cette année",
        "Comment la réalité augmentée s'intègre dans la vie quotidienne"
    )
    val sources = listOf("TechRadar", "Le Monde Tech", "FrAndroid", "01net", "Numerama")
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://news.google.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titles.getOrElse(index) { "Actualité..." },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${sources.getOrElse(index) { "Google News" }} • Il y a 2h",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

