package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.AppItem
import com.example.ui.viewmodels.LauncherViewModel

enum class AddAppsTarget {
    HOME, DOCK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppsBottomSheet(
    viewModel: LauncherViewModel,
    apps: List<AppItem>,
    homeAppPackages: List<String>,
    dockAppPackages: List<String> = emptyList(),
    initialTarget: AddAppsTarget = AddAppsTarget.HOME,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedTarget by remember { mutableStateOf(initialTarget) }

    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) apps
        else apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

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
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (selectedTarget == AddAppsTarget.HOME) "Ajouter à l'écran d'accueil" else "Gérer les applications du Dock",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (selectedTarget == AddAppsTarget.HOME)
                            "Sélectionnez les applications pour l'écran principal"
                        else
                            "Sélectionnez les applications pour le Dock inférieur",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer la sélection d'applications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Target Selector: Home vs Dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTarget == AddAppsTarget.HOME,
                    onClick = { selectedTarget = AddAppsTarget.HOME },
                    label = { Text("Écran d'accueil (${homeAppPackages.size})") },
                    leadingIcon = if (selectedTarget == AddAppsTarget.HOME) {
                        { Icon(Icons.Default.Check, contentDescription = "Actif", modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedTarget == AddAppsTarget.DOCK,
                    onClick = { selectedTarget = AddAppsTarget.DOCK },
                    label = { Text("Dock (${dockAppPackages.size})") },
                    leadingIcon = if (selectedTarget == AddAppsTarget.DOCK) {
                        { Icon(Icons.Default.Check, contentDescription = "Actif", modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher une application...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Effacer la recherche",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // List of Apps
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredApps) { app ->
                    val isAdded = if (selectedTarget == AddAppsTarget.HOME) {
                        homeAppPackages.contains(app.packageName)
                    } else {
                        dockAppPackages.contains(app.packageName)
                    }
                    val targetName = if (selectedTarget == AddAppsTarget.HOME) "l'écran d'accueil" else "le dock"
                    val actionLabel = if (isAdded) "Retirer de $targetName" else "Ajouter à $targetName"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .semantics(mergeDescendants = true) {
                                role = Role.Checkbox
                                contentDescription = "${app.label}, ${if (isAdded) "ajouté à $targetName" else "non ajouté"}"
                            }
                            .clickable(
                                role = Role.Checkbox,
                                onClickLabel = actionLabel,
                                onClick = {
                                    if (selectedTarget == AddAppsTarget.HOME) {
                                        if (isAdded) viewModel.removeAppFromHome(app.packageName)
                                        else viewModel.addAppToHome(app.packageName)
                                    } else {
                                        if (isAdded) viewModel.removeAppFromDock(app.packageName)
                                        else viewModel.addAppToDock(app.packageName)
                                    }
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (app.iconBitmap != null) {
                            Image(
                                bitmap = app.iconBitmap,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = app.icon,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (selectedTarget == AddAppsTarget.HOME) {
                                    if (isAdded) viewModel.removeAppFromHome(app.packageName)
                                    else viewModel.addAppToHome(app.packageName)
                                } else {
                                    if (isAdded) viewModel.removeAppFromDock(app.packageName)
                                    else viewModel.addAppToDock(app.packageName)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isAdded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = if (isAdded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .size(36.dp)
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = actionLabel,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
