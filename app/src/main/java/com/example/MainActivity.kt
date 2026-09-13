package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.AppItem
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.LauncherViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BlendLauncherScreen(
                    viewModel = viewModel,
                    onSearchClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                        startActivity(intent)
                    }
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back button from closing launcher
    }
}

@Composable
fun BlendLauncherScreen(viewModel: LauncherViewModel, onSearchClick: () -> Unit) {
    val apps by viewModel.installedApps.collectAsState()
    var isDrawerOpen by remember { mutableStateOf(false) }

    val dockApps = apps.take(4)
    val homeApps = if (apps.size > 4) apps.drop(4).take(12) else emptyList() // Show limited apps on home

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Detect Swipe Up to open drawer
                    if (dragAmount.y < -50) {
                        isDrawerOpen = true
                    }
                    // Detect Swipe Down to close drawer
                    else if (dragAmount.y > 50 && isDrawerOpen) {
                        isDrawerOpen = false
                    }
                }
            }
    ) {
        // --- Home Screen Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
        ) {
            // Pixel: At A Glance & Search Bar
            AtAGlanceWidget(onSearchClick)

            Spacer(modifier = Modifier.height(32.dp))

            // Home Screen Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 120.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(homeApps) { app ->
                    AppIconItem(app = app, onClick = { viewModel.launchApp(app.packageName) })
                }
            }
        }

        // --- iOS: Ultra-Premium Glassmorphism Dock ---
        if (!isDrawerOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(94.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.18f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockApps.forEach { app ->
                        AppIconItem(
                            app = app,
                            showLabel = false,
                            iconSize = 56.dp,
                            onClick = { viewModel.launchApp(app.packageName) }
                        )
                    }
                }
            }
        }

        // --- Android: App Drawer ---
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AppDrawer(apps = apps, onAppClick = { viewModel.launchApp(it) })
        }
    }
}

@Composable
fun AtAGlanceWidget(onSearchClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Date Text with High-Contrast Shadow
        Text(
            text = currentDate,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp, bottom = 14.dp),
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.65f),
                    blurRadius = 8f
                )
            )
        )

        // Glassmorphic Capsule Search Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(27.dp),
            color = Color.White.copy(alpha = 0.88f),
            shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.4f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF3C4043),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search...",
                    color = Color(0xFF5F6368),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AppDrawer(apps: List<AppItem>, onAppClick: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        // Material You Dynamic Color for Drawer Background
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "All Apps",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                items(apps) { app ->
                    AppIconItem(
                        app = app,
                        textColor = MaterialTheme.colorScheme.onBackground,
                        shadow = false,
                        onClick = { onAppClick(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppIconItem(
    app: AppItem,
    showLabel: Boolean = true,
    textColor: Color = Color.White,
    shadow: Boolean = true,
    iconSize: Dp = 60.dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "icon_press_scale"
    )

    Column(
        modifier = Modifier
            .padding(6.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = app.icon,
            contentDescription = app.label,
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop
        )
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            val textStyle = if (shadow) {
                androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        blurRadius = 6f
                    )
                )
            } else {
                androidx.compose.ui.text.TextStyle()
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
