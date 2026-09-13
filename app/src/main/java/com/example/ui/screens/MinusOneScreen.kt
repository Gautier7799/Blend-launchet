package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.LauncherSettings

@Composable
fun MinusOneScreen(
    settings: LauncherSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = if (settings.fullscreenMode) 24.dp else 52.dp, bottom = 100.dp)
    ) {
        item {
            // Google Search Bar Fake
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Rechercher sur Google...", color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f), fontSize = 16.sp)
                }
            }
        }
        
        item {
            // Top Widgets (automatically added)
            if (settings.showWeatherWidget || settings.showDateWidget || settings.showClockWidget) {
                TopWidgetsBar(
                    settings = settings,
                    onAiClick = {},
                    onSettingsClick = {},
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        
        item {
            Text(
                text = "Google News",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        // Simulated News feed items
        items(5) { index ->
            NewsCard(index = index)
            Spacer(modifier = Modifier.height(12.dp))
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
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://news.google.com"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = titles.getOrElse(index) { "Actualité..." },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = sources.getOrElse(index) { "Google News" } + " • Il y a 2h",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
