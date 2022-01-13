package com.noahjutz.gymroutines.ui.settings.general

import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.noahjutz.gymroutines.R
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
                title = stringResource(R.string.screen_general_settings),
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_pop_back))
                    }
                }
            )
        }
    ) {
        val (isVisible, setIsVisible) = remember { mutableStateOf(false) }
        ListItem(
            modifier = Modifier.clickable { setIsVisible(true) },
            text = { Text(stringResource(R.string.pref_reset_settings)) },
            icon = { Icon(Icons.Default.RestartAlt, null) }
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
                Text(stringResource(R.string.dialog_title_reset_settings))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReset()
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm_reset_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}
