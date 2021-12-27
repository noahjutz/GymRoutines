package com.noahjutz.gymroutines.ui.settings.general

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import com.noahjutz.gymroutines.ui.components.TopBar

@Composable
fun GeneralSettings(
    popBackStack: () -> Unit,
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

    }
}