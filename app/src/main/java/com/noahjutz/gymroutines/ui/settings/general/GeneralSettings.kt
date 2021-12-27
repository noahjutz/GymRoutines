package com.noahjutz.gymroutines.ui.settings.general

import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.noahjutz.gymroutines.ui.components.TopBar
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterialApi
@Composable
fun GeneralSettings(
    popBackStack: () -> Unit,
    viewModel: GeneralSettingsViewModel = getViewModel()
) {
    Scaffold(
        topBar = {
            TopBar(
                title = "General",
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) {
        val (isVisible, setIsVisible) = remember { mutableStateOf(false) }
        ListItem(
            modifier = Modifier.clickable { setIsVisible(true) },
            text = {
                Text("Reset all settings")
            },
            icon = {
                Icon(Icons.Default.RestartAlt, null)
            }
        )
        ResetDialog(
            isVisible = isVisible,
            onDismiss = { setIsVisible(false) },
            onReset = { viewModel.resetSettings() }
        )
    }
}

@Composable
private fun ResetDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("Reset all settings?")
            },
            text = {
                Text("This will set all settings to their defaults.")
            },
            confirmButton = {
                Button(onClick = {
                    onReset()
                    onDismiss()
                }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}