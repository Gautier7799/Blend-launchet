package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer les paramètres du lanceur",
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
                            .clickable { 
                                isWallpaperPickerOpen = true
                            }
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
                                text = "Fonds d'écran & Flou calibré",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Fond Système, effet verre dépoli (Blur) & photos personnalisées",
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

                    // iOS 18 Icon Tinting Mode Selector
                    Text(
                        text = "Style des icônes (iOS 18)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Standard, Sombre, ou Teinté monochrome avec votre couleur préférée",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "default" to "Standard",
                            "dark" to "Sombre",
                            "tinted" to "Teinté"
                        ).forEach { (mode, label) ->
                            val isSelected = settings.iconColorMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateSettings(settings.copy(iconColorMode = mode)) },
                                label = { Text(label, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (settings.iconColorMode == "tinted") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Palette de teinte iOS 18 :",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                "#007AFF" to Color(0xFF007AFF), // Blue
                                "#AF52DE" to Color(0xFFAF52DE), // Purple
                                "#34C759" to Color(0xFF34C759), // Green
                                "#FF9500" to Color(0xFFFF9500), // Orange
                                "#FF2D55" to Color(0xFFFF2D55), // Red
                                "#FFCC00" to Color(0xFFFFCC00), // Yellow
                                "#8E8E93" to Color(0xFF8E8E93)  // Slate
                            ).forEach { (hex, color) ->
                                val isSelected = settings.iconTintColorHex == hex
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.updateSettings(settings.copy(iconTintColorHex = hex))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Control Center Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Tune,
                        title = "Centre de Contrôle iOS",
                        subtitle = "Glissement depuis le haut pour les curseurs luminosité & volume",
                        checked = settings.controlCenterEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(controlCenterEnabled = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Glassmorphism Icons Toggle ("كل الايقونات زجاج مع blur")
                    SettingsSwitchRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "أيقونات زجاجية (Glass Icons)",
                        subtitle = "تأثير زجاجي ثلاثي الأبعاد مع انعكاسات ضوئية على كل الأيقونات",
                        checked = settings.glassIcons,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(glassIcons = it)) }
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

            // Section 2.5: Widgets iOS 17 (Style Apple)
            SettingsSectionTitle(title = "Widgets iOS 17 (Style Apple)")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSwitchRow(
                        icon = Icons.Default.Widgets,
                        title = "Widgets iOS sur l'écran d'accueil",
                        subtitle = "Affiche les widgets iOS en haut de l'écran d'accueil",
                        checked = settings.showIosHomeWidgets,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showIosHomeWidgets = it)) }
                    )

                    if (settings.showIosHomeWidgets) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "Style de widget iOS",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = settings.iosWidgetStyle == "pair",
                                onClick = { viewModel.updateSettings(settings.copy(iosWidgetStyle = "pair")) },
                                label = { Text("Météo + Batterie (2x2)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = settings.iosWidgetStyle == "quad_battery",
                                onClick = { viewModel.updateSettings(settings.copy(iosWidgetStyle = "quad_battery")) },
                                label = { Text("Batterie 4 Appareils") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.BatteryChargingFull,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "موضع الـ Widget (Widget Placement)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple("secondary", "الواجهة الثانوية", Icons.Default.Dashboard),
                                Triple("home", "الرئيسية", Icons.Default.Home),
                                Triple("both", "كلاهما", Icons.Default.Layers)
                            ).forEach { (mode, label, icon) ->
                                val isSelected = settings.widgetPlacement == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateSettings(settings.copy(widgetPlacement = mode)) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        SettingsSwitchRow(
                            icon = Icons.Default.Label,
                            title = "Afficher les étiquettes",
                            subtitle = "Affiche 'Weather' et 'Battery' sous les widgets",
                            checked = settings.showWidgetLabels,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(showWidgetLabels = it)) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        SettingsSwitchRow(
                            icon = Icons.Default.ViewAgenda,
                            title = "Barre d'outils classique en haut",
                            subtitle = "Affiche aussi la barre classique au-dessus des widgets",
                            checked = settings.showTopBarWidgets,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(showTopBarWidgets = it)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 3: Widgets de l'écran d'accueil
            SettingsSectionTitle(title = "Widgets du tableau de bord (-1)")

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

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Notifications Center Widget
                    SettingsSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = "Widget Centre de Notifications (iOS)",
                        subtitle = "Affiche les notifications actives avec actions directes et suppression",
                        checked = settings.showNotificationsWidget,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showNotificationsWidget = it)) }
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

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Control Center iOS 18 Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Tune,
                        title = "Centre de Contrôle iOS 18",
                        subtitle = "Bouton d'accès rapide dans la barre d'état et glissement depuis le coin supérieur droit",
                        checked = settings.controlCenterEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(controlCenterEnabled = it)) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Quick access to Wallpaper & Calibrated Blur
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
                                imageVector = Icons.Default.BlurOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fond Système libre & Flou calibré",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Flou: ${if (settings.wallpaperBlurDp == 0) "Net (0 dp)" else "${settings.wallpaperBlurDp} dp"} • Mode: ${if (settings.wallpaperType == "system") "Système" else settings.wallpaperType}",
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
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 4: Centre des Autorisations & Stabilité du Système
            SettingsSectionTitle(title = "Autorisations & Stabilité du Système")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    val isDefault = remember { viewModel.isDefaultLauncher(context) }
                    val isOverlay = remember { viewModel.isOverlayPermissionGranted(context) }
                    val isWriteSettings = remember { viewModel.isWriteSettingsGranted(context) }
                    val isNotifAccess = remember { viewModel.isNotificationAccessGranted(context) }
                    val isBatteryIgnored = remember { viewModel.isBatteryOptimizationIgnored(context) }
                    val isAccessibility = remember { viewModel.isAccessibilityServiceEnabled() }
                    val isNotifPolicy = remember { viewModel.isNotificationPolicyAccessGranted(context) }

                    // 1. Default Launcher
                    PermissionStatusItem(
                        icon = Icons.Default.Home,
                        title = "Lanceur d'accueil par défaut",
                        subtitle = if (isDefault) "Blend Launcher est défini comme écran d'accueil" else "Définir Blend Launcher comme lanceur principal",
                        isGranted = isDefault,
                        statusText = if (isDefault) "Par défaut" else "Définir",
                        onClick = { viewModel.openDefaultLauncherSettings(context) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 2. Overlay / Draw over other apps (Control Center & Floating gestures)
                    PermissionStatusItem(
                        icon = Icons.Default.Layers,
                        title = "Superposition sur d'autres applications",
                        subtitle = if (isOverlay) "Autorisation accordée pour le Centre de Contrôle" else "Requis pour ouvrir le Centre de Contrôle par-dessus d'autres applications",
                        isGranted = isOverlay,
                        statusText = if (isOverlay) "Accordée" else "Configurer",
                        onClick = { viewModel.openOverlaySettings(context) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 3. Write Settings (Brightness & Display controls)
                    PermissionStatusItem(
                        icon = Icons.Default.Tune,
                        title = "Modifier les paramètres système",
                        subtitle = if (isWriteSettings) "Luminosité et affichage configurables" else "Requis pour régler la luminosité de l'écran depuis le Centre de Contrôle",
                        isGranted = isWriteSettings,
                        statusText = if (isWriteSettings) "Accordée" else "Configurer",
                        onClick = { viewModel.openWriteSettings(context) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 2. Notification Badges & Listener
                    PermissionStatusItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Accès aux Notifications & Badges",
                        subtitle = if (isNotifAccess) "Écoute active pour pastilles sur les icônes" else "Autoriser l'affichage du nombre de notifications sur les applications",
                        isGranted = isNotifAccess,
                        statusText = if (isNotifAccess) "Actif" else "Activer",
                        onClick = { viewModel.openNotificationAccessSettings(context) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 3. Battery Optimization (Keep alive in background)
                    PermissionStatusItem(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Exemption de batterie (Arrière-plan)",
                        subtitle = if (isBatteryIgnored) "Optimisation ignorée pour une réactivité maximale" else "Empêcher le système de suspendre le lanceur pour éviter tout ralentissement",
                        isGranted = isBatteryIgnored,
                        statusText = if (isBatteryIgnored) "Illimité" else "Optimiser",
                        onClick = { viewModel.requestIgnoreBatteryOptimization(context) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 4. Accessibility Service (Double tap sleep)
                    PermissionStatusItem(
                        icon = Icons.Default.Lock,
                        title = "Service d'Accessibilité (Verrouillage)",
                        subtitle = if (isAccessibility) "Verrouillage de l'écran activé sans mot de passe" else "Permet d'éteindre l'écran par double-tap tout en gardant l'empreinte",
                        isGranted = isAccessibility,
                        statusText = if (isAccessibility) "Actif" else "Activer",
                        onClick = { viewModel.performSleep(context) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 5. Sound & DND Policy Access
                    PermissionStatusItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Gestion Son & Ne Pas Déranger",
                        subtitle = if (isNotifPolicy) "Contrôle des modes sonores autorisé" else "Permet de basculer silencieux/sonnerie depuis les raccourcis",
                        isGranted = isNotifPolicy,
                        statusText = if (isNotifPolicy) "Autorisé" else "Configurer",
                        onClick = { viewModel.openNotificationPolicySettings(context) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 6. Full App Permissions
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Toutes les autorisations système",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text("Ouvrir la page des informations de l'application dans Android")
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Security,
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
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.openAppDetailsSettings(context) }
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
                                contentDescription = "Icône actualiser",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .semantics(mergeDescendants = true) {
                                role = Role.Button
                                contentDescription = "Actualiser les applications"
                            }
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Actualiser la liste des applications",
                                onClick = { viewModel.loadApps() }
                            )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Version Info Footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Blend Launcher v3.0 (Pro iOS)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Édition Pro • iOS 18 Stacks, Large Folders & Control Center",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (isWallpaperPickerOpen) {
        WallpaperPickerBottomSheet(
            settings = settings,
            onUpdateSettings = { viewModel.updateSettings(it) },
            onDismissRequest = { isWallpaperPickerOpen = false }
        )
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
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .semantics(mergeDescendants = true) {
                role = Role.Switch
            }
            .clickable(
                role = Role.Switch,
                onClickLabel = if (checked) "Désactiver $title" else "Activer $title",
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
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

@Composable
private fun PermissionStatusItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    statusText: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = title, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isGranted) androidx.compose.ui.graphics.Color(0xFF34C759).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isGranted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFF34C759),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = statusText,
                        color = if (isGranted) androidx.compose.ui.graphics.Color(0xFF34C759) else MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    )
}

