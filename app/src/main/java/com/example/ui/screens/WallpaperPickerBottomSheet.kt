package com.example.ui.screens

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import com.example.ui.viewmodels.LauncherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WallpaperPreset(
    val id: String,
    val name: String,
    val colors: List<Color>,
    val isSystem: Boolean = false
)

val WALLPAPER_PRESETS = listOf(
    WallpaperPreset(
        id = "system",
        name = "Fond Système",
        colors = listOf(Color(0xFF2563EB), Color(0xFF1E3A8A)),
        isSystem = true
    ),
    WallpaperPreset(
        id = "emerald",
        name = "Émeraude Sombre",
        colors = listOf(Color(0xFF143423), Color(0xFF1E4633), Color(0xFF0C2016))
    ),
    WallpaperPreset(
        id = "dark_amoled",
        name = "Noir AMOLED",
        colors = listOf(Color(0xFF0F1117), Color(0xFF181C26), Color(0xFF090A0E))
    ),
    WallpaperPreset(
        id = "twilight",
        name = "Crépuscule Violet",
        colors = listOf(Color(0xFF2C1645), Color(0xFF45215A), Color(0xFF160B24))
    ),
    WallpaperPreset(
        id = "ocean",
        name = "Océan Pacifique",
        colors = listOf(Color(0xFF0E2A44), Color(0xFF19446C), Color(0xFF071828))
    ),
    WallpaperPreset(
        id = "glass",
        name = "Graphite Givré",
        colors = listOf(Color(0xFF1E232A), Color(0xFF2E3640), Color(0xFF14171C))
    )
)

@Composable
fun WallpaperBackground(
    settings: LauncherSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var systemBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(settings.wallpaperType) {
        if (settings.wallpaperType == "system") {
            withContext(Dispatchers.IO) {
                try {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    val drawable = wallpaperManager.drawable
                    if (drawable != null) {
                        systemBitmap = drawable.toBitmap()
                    }
                } catch (_: Exception) {
                    systemBitmap = null
                }
            }
        }
    }

    val blurModifier = if (settings.wallpaperBlurDp > 0) {
        Modifier.blur(settings.wallpaperBlurDp.dp)
    } else {
        Modifier
    }

    Box(modifier = modifier) {
        when {
            settings.wallpaperType == "custom" && !settings.customWallpaperUri.isNullOrBlank() -> {
                AsyncImage(
                    model = Uri.parse(settings.customWallpaperUri),
                    contentDescription = "Fond d'écran personnalisé",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                )
            }
            settings.wallpaperType == "system" -> {
                if (systemBitmap != null) {
                    Image(
                        bitmap = systemBitmap!!.asImageBitmap(),
                        contentDescription = "Fond d'écran système",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(blurModifier)
                    )
                } else {
                    // Fallback to elegant clean deep blue gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(blurModifier)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1E293B),
                                        Color(0xFF0F172A),
                                        Color(0xFF020617)
                                    )
                                )
                            )
                    )
                }
            }
            settings.wallpaperType == "emerald" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF133623),
                                    Color(0xFF1A4731),
                                    Color(0xFF0D2418)
                                )
                            )
                        )
                )
            }
            settings.wallpaperType == "dark_amoled" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF07090E),
                                    Color(0xFF0F1117),
                                    Color(0xFF050608)
                                )
                            )
                        )
                )
            }
            settings.wallpaperType == "twilight" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2D1644),
                                    Color(0xFF45215C),
                                    Color(0xFF150A22)
                                )
                            )
                        )
                )
            }
            settings.wallpaperType == "ocean" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0E2943),
                                    Color(0xFF19456B),
                                    Color(0xFF071727)
                                )
                            )
                        )
                )
            }
            settings.wallpaperType == "glass" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E2329),
                                    Color(0xFF2D353F),
                                    Color(0xFF13171C)
                                )
                            )
                        )
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A),
                                    Color(0xFF020617)
                                )
                            )
                        )
                )
            }
        }

        // Optional Dimming overlay for readability
        if (settings.wallpaperDim > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = settings.wallpaperDim))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperPickerBottomSheet(
    settings: LauncherSettings,
    onUpdateSettings: (LauncherSettings) -> Unit,
    onDismissRequest: () -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateSettings(
                settings.copy(
                    wallpaperType = "custom",
                    customWallpaperUri = uri.toString()
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = "Fond d'écran",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Fonds d'écran & Arrière-plans",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Personnalisez l'ambiance visuelle du lanceur",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Photo Gallery Picker Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Galerie",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choisir une photo de la galerie",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (settings.wallpaperType == "custom") "Photo sélectionnée active" else "Utilisez vos propres photos et images",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (settings.wallpaperType == "custom") {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Actif",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Palettes & Dégradés exclusifs",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of Curated Wallpapers
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(WALLPAPER_PRESETS) { preset ->
                    val isSelected = settings.wallpaperType == preset.id

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clickable {
                                onUpdateSettings(
                                    settings.copy(wallpaperType = preset.id)
                                )
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(preset.colors))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sélectionné",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (preset.isSystem) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = "Système",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(
                                    text = preset.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calibrated Blur Slider (fond d ecran libre de systeme qualibre blur)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BlurOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Calibrage du flou (Blur)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Effet verre dépoli iOS sur le fond d'écran",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (settings.wallpaperBlurDp == 0) "Net (0 dp)" else "${settings.wallpaperBlurDp} dp",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.wallpaperBlurDp.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(wallpaperBlurDp = it.toInt())) },
                        valueRange = 0f..40f,
                        steps = 7,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0 (Net)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Flou équilibré (15 dp)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Flou profond (40 dp)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dimming slider
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BrightnessMedium,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Assombrissement du fond",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Améliore la lisibilité des icônes et widgets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${(settings.wallpaperDim * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.wallpaperDim,
                        onValueChange = { onUpdateSettings(settings.copy(wallpaperDim = it)) },
                        valueRange = 0f..0.6f,
                        steps = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
