package com.dpi.changer.ui.screens

import android.content.Intent
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
import com.dpi.changer.service.DPIService
import com.dpi.changer.ui.components.GlassButton
import com.dpi.changer.ui.components.GlassTextField
import com.dpi.changer.ui.components.LiquidGlassCard
import com.dpi.changer.util.RootUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToPresets: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dpiInput by remember { mutableStateOf("") }
    var showWarning by remember { mutableStateOf(false) }
    var warningDpi by remember { mutableIntStateOf(0) }
    var currentDPI by remember { mutableIntStateOf(RootUtil.getCurrentDPI() ?: 400) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "DPI Changer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                ),
                actions = {
                    IconButton(onClick = onNavigateToPresets) {
                        Icon(Icons.Default.List, contentDescription = "Presets")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current DPI Display
            LiquidGlassCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Current DPI",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        currentDPI.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick DPI Input
            LiquidGlassCard {
                Column {
                    GlassTextField(
                        value = dpiInput,
                        onValueChange = { 
                            dpiInput = it.filter { char -> char.isDigit() }
                        },
                        label = "Enter DPI Value"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "Apply",
                            onClick = {
                                val dpi = dpiInput.toIntOrNull() ?: return@GlassButton
                                if (dpi > 1000) {
                                    warningDpi = dpi
                                    showWarning = true
                                } else {
                                    RootUtil.setDPI(dpi)
                                    currentDPI = dpi
                                    dpiInput = ""
                                }
                            },
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.primary
                        )

                        GlassButton(
                            text = "Reset",
                            onClick = {
                                RootUtil.resetDPI()
                                currentDPI = RootUtil.getCurrentDPI() ?: 400
                            },
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Presets
            Text(
                "Quick Presets",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val presets by PresetDataStore.presets.collectAsStateWithLifecycle(initialValue = emptyList())
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets.take(5)) { preset ->
                    PresetItem(
                        preset = preset,
                        onClick = {
                            if (preset.dpi > 1000) {
                                warningDpi = preset.dpi
                                showWarning = true
                            } else {
                                RootUtil.setDPI(preset.dpi)
                                currentDPI = preset.dpi
                            }
                        }
                    )
                }
            }
        }

        // Warning Dialog
        if (showWarning) {
            AlertDialog(
                onDismissRequest = { showWarning = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                title = { Text("High DPI Warning") },
                text = { 
                    Text("You are about to set DPI to $warningDpi. This value is very high and might make the UI extremely small. Continue?") 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            RootUtil.setDPI(warningDpi)
                            currentDPI = warningDpi
                            showWarning = false
                            dpiInput = ""
                        }
                    ) {
                        Text("Continue", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWarning = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        }
    }
}

@Composable
fun PresetItem(
    preset: Preset,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${preset.dpi} DPI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}