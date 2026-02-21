package com.dpi.changer.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dpi.changer.data.local.PresetDataStore
import com.dpi.changer.data.model.Preset
import com.dpi.changer.ui.components.LiquidGlassCard
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val presets by PresetDataStore.presets.collectAsStateWithLifecycle(initialValue = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPreset by remember { mutableStateOf<Preset?>(null) }
    var showImportExport by remember { mutableStateOf(false) }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { importPresets(context, it) }
    }

    // Create file for export
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { exportPresets(context, presets, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Presets") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showImportExport = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Import/Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Preset")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(presets, key = { it.id }) { preset ->
                PresetCard(
                    preset = preset,
                    onEdit = { editingPreset = preset },
                    onDelete = {
                        scope.launch {
                            PresetDataStore.deletePreset(preset.id)
                        }
                    }
                )
            }

            if (presets.isEmpty()) {
                item {
                    EmptyState()
                }
            }
        }

        // Add/Edit Dialog
        if (showAddDialog || editingPreset != null) {
            PresetDialog(
                preset = editingPreset,
                onDismiss = { 
                    showAddDialog = false
                    editingPreset = null
                },
                onSave = { name, dpi ->
                    scope.launch {
                        if (editingPreset != null) {
                            PresetDataStore.updatePreset(
                                editingPreset!!.copy(name = name, dpi = dpi)
                            )
                        } else {
                            PresetDataStore.addPreset(
                                Preset(name = name, dpi = dpi)
                            )
                        }
                    }
                    showAddDialog = false
                    editingPreset = null
                }
            )
        }

        // Import/Export Bottom Sheet
        if (showImportExport) {
            ModalBottomSheet(
                onDismissRequest = { showImportExport = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Import / Export",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    ListItem(
                        headlineContent = { Text("Import Presets") },
                        leadingContent = { Icon(Icons.Default.FileOpen, null) },
                        modifier = Modifier.clickable {
                            importLauncher.launch("application/json")
                            showImportExport = false
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Export Presets") },
                        leadingContent = { Icon(Icons.Default.Save, null) },
                        modifier = Modifier.clickable {
                            exportLauncher.launch("dpi_presets.json")
                            showImportExport = false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PresetCard(
    preset: Preset,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    LiquidGlassCard(blurRadius = 10.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    preset.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${preset.dpi} DPI",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = {
                            onEdit()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PresetDialog(
    preset: Preset?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(preset?.name ?: "") }
    var dpi by remember { mutableStateOf(preset?.dpi?.toString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (preset == null) "Add Preset" else "Edit Preset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dpi,
                    onValueChange = { 
                        dpi = it.filter { char -> char.isDigit() }
                        error = null
                    },
                    label = { Text("DPI Value") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val dpiValue = dpi.toIntOrNull()
                    when {
                        name.isBlank() -> error = "Name required"
                        dpiValue == null || dpiValue <= 0 -> error = "Invalid DPI"
                        else -> onSave(name, dpiValue)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No presets yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

private fun importPresets(context: android.content.Context, uri: Uri) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = reader.readText()
            val type = object : TypeToken<List<Preset>>() {}.type
            val imported = Gson().fromJson<List<Preset>>(json, type) ?: emptyList()
            
            kotlinx.coroutines.GlobalScope.launch {
                PresetDataStore.importPresets(imported)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun exportPresets(context: android.content.Context, presets: List<Preset>, uri: Uri) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val json = Gson().toJson(presets)
            outputStream.write(json.toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}