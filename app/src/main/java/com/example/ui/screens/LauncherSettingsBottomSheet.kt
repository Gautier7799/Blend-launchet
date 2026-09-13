package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.LauncherSettings
import com.example.ui.viewmodels.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherSettingsBottomSheet(
    viewModel: LauncherViewModel,
    settings: LauncherSettings,
    onDismissRequest: () -> Unit,
    onManageWidgetsClick: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isWallpaperPickerOpen by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Paramètres de Blend",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Personnalisation Material You",
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

            // Section 1: Style & Material You Icons
            SettingsSectionTitle(title = "Apparence & Style (Material You)")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Wallpaper Chooser Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isWallpaperPickerOpen = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fonds d'écran & Arrière-plans",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Galerie photo ou dégradés AMOLED / Émeraude",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Manage Widgets Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onManageWidgetsClick() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = "Widgets",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gérer les Widgets",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ajouter et supprimer des widgets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Themed Icons Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Palette,
                        title = "Icônes Thématiques (Material You)",
                        subtitle = "Harmoniser les icônes avec la couleur du fond d'écran",
                        checked = settings.themedIcons,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(themedIcons = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Show Labels Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Edit,
                        title = "Afficher les noms des applications",
                        subtitle = "Masquer pour un style minimaliste et épuré",
                        checked = settings.showLabels,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showLabels = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Icon Size Selector
                    Text(
                        text = "Taille des icônes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Petite", 52, "52dp"),
                            Triple("Normale", 60, "60dp"),
                            Triple("Grande", 68, "68dp")
                        ).forEach { (label, sizeDp, _) ->
                            val isSelected = settings.iconSizeDp == sizeDp
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateSettings(settings.copy(iconSizeDp = sizeDp)) },
                                label = { Text(label) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Grid & Dock
            SettingsSectionTitle(title = "Grille & Disposition")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nombre de colonnes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4, 5).forEach { cols ->
                            val isSelected = settings.gridColumns == cols
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateSettings(settings.copy(gridColumns = cols)) },
                                label = { Text("$cols Colonnes") },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "Applications dans le Dock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4, 5).forEach { count ->
                            val isSelected = settings.dockCount == count
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateSettings(settings.copy(dockCount = count)) },
                                label = { Text("$count Applications") },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 3: Widgets de l'écran d'accueil
            SettingsSectionTitle(title = "Widgets de l'écran d'accueil")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // AI Widget Switch
                    SettingsSwitchRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Assistant IA (Remplace la recherche)",
                        subtitle = "Affiche l'icône IA intelligente pour Gemini et recherche vocale",
                        checked = settings.showAiWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showAiWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Date Widget Switch
                    SettingsSwitchRow(
                        icon = Icons.Default.CalendarToday,
                        title = "Date & Calendrier",
                        subtitle = "Affiche le widget date avec accès rapide à l'agenda",
                        checked = settings.showDateWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showDateWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Weather Widget Switch
                    SettingsSwitchRow(
                        icon = Icons.Default.WbSunny,
                        title = "Météo en direct",
                        subtitle = "Affiche la température et les prévisions météo",
                        checked = settings.showWeatherWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showWeatherWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Battery Widget Switch
                    SettingsSwitchRow(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Niveau de Batterie",
                        subtitle = "Indicateur en temps réel du pourcentage et de la charge",
                        checked = settings.showBatteryWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showBatteryWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Clock Widget Switch
                    SettingsSwitchRow(
                        icon = Icons.Default.AccessTime,
                        title = "Horloge & Alarme",
                        subtitle = "Affiche l'heure précise avec accès aux alarmes",
                        checked = settings.showClockWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showClockWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Flashlight Widget Switch
                    SettingsSwitchRow(
                        icon = Icons.Default.FlashOn,
                        title = "Lampe Torche rapide",
                        subtitle = "Allumer et éteindre la torche directement depuis l'écran",
                        checked = settings.showTorchWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showTorchWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Settings Shortcut Switch
                    SettingsSwitchRow(
                        icon = Icons.Default.Settings,
                        title = "Raccourci Paramètres",
                        subtitle = "Bouton d'accès rapide aux réglages du lanceur",
                        checked = settings.showSettingsWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showSettingsWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // App Shortcuts Widget
                    SettingsSwitchRow(
                        icon = Icons.Default.Apps,
                        title = "Widget Raccourcis d'applications",
                        subtitle = "Affiche la barre d'accès rapide aux applications favorites",
                        checked = settings.showAppShortcutsWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showAppShortcutsWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Device Battery Card Widget
                    SettingsSwitchRow(
                        icon = Icons.Default.Smartphone,
                        title = "Widget Batterie & Appareil (Pixel style)",
                        subtitle = "Affiche la carte de l'état de la batterie et de la charge",
                        checked = settings.showDeviceCardWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showDeviceCardWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Music Player Widget
                    SettingsSwitchRow(
                        icon = Icons.Default.MusicNote,
                        title = "Widget Lecteur Musique (Pixel Music)",
                        subtitle = "Contrôle rapide de lecture et piste suivante",
                        checked = settings.showMusicWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showMusicWidget = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Tasks Widget
                    SettingsSwitchRow(
                        icon = Icons.Default.Checklist,
                        title = "Widget Mes Tâches & Notes",
                        subtitle = "Liste interactive de tâches avec cases à cocher",
                        checked = settings.showTasksWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showTasksWidget = it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 4: Style épuré & Suppression des ombres et lignes
            SettingsSectionTitle(title = "Apparence & Style épuré")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Text Shadows Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.BlurOn,
                        title = "Ombres sous le texte",
                        subtitle = "Désactivez pour un texte parfaitement net sans contour flou noir",
                        checked = settings.showTextShadows,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showTextShadows = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Dock Border Lines Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.BorderColor,
                        title = "Lignes de contour du Dock",
                        subtitle = "Désactivez pour un dock en verre pur sans lignes ni bordures",
                        checked = settings.showDockLines,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showDockLines = it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 5: Gestes & Fonctionnalités Avancées (iOS & Android)
            SettingsSectionTitle(title = "Gestes & Fonctionnalités Avancées (iOS & Android)")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Double-tap to Sleep Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Lock,
                        title = "Verrouillage par double pression",
                        subtitle = "Double-tapez sur l'écran d'accueil pour éteindre l'écran (Conserve l'empreinte)",
                        checked = settings.doubleTapToSleep,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(doubleTapToSleep = it))
                            if (it && !viewModel.isAccessibilityServiceEnabled()) {
                                viewModel.performSleep(context)
                            }
                        }
                    )

                    if (settings.doubleTapToSleep && !viewModel.isAccessibilityServiceEnabled()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.performSleep(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Activer le service d'accessibilité",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Haptic Feedback Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.TouchApp,
                        title = "Retour Haptique",
                        subtitle = "Vibrations subtiles lors des appuis longs et des gestes",
                        checked = settings.hapticFeedback,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(hapticFeedback = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Fullscreen Immersive Mode Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Fullscreen,
                        title = "Mode Plein Écran Immersif",
                        subtitle = "Masquer la barre d'état (en haut) et la barre de navigation (en bas)",
                        checked = settings.fullscreenMode,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(fullscreenMode = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Minimal Drawer Header Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.VerticalSplit,
                        title = "Épurer le tiroir d'applications",
                        subtitle = "Masquer le grand titre supérieur pour afficher directement les icônes",
                        checked = settings.hideDrawerHeader,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(hideDrawerHeader = it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 4: System & Actions
            SettingsSectionTitle(title = "Système & Lanceur par défaut")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Default Home Setting Button
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Définir comme écran d'accueil par défaut",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text("Ouvrir les paramètres Android pour choisir Blend Launcher")
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.openDefaultLauncherSettings(context) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Reload Apps Button
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Actualiser les applications",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text("Recharger la liste des applications installées")
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.loadApps() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
