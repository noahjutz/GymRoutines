package com.noahjutz.gymroutines.ui.settings.data

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.noahjutz.gymroutines.ui.components.TopBar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterialApi
@Composable
fun DataSettings(
    popBackStack: () -> Unit,
    viewModel: DataSettingsViewModel = getViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopBar(
                title = "Data",
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) {
        val exportDatabaseLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument()
        ) { uri ->
            if (uri != null) {
                viewModel.exportDatabase(uri)
                viewModel.restartApp()
            }
        }

        val importDatabaseLauncher = rememberLauncherForActivityResult(
            object : ActivityResultContracts.OpenDocument() {
                override fun createIntent(context: Context, input: Array<String>): Intent {
                    super.createIntent(context, input)
                    return Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("*/*")
                }
            }
        ) { uri ->
            if (uri != null) {
                viewModel.importDatabase(uri)
                viewModel.restartApp()
            }
        }

        Column(Modifier.verticalScroll(rememberScrollState())) {
            val isWorkoutInProgress by viewModel.isWorkoutInProgress.collectAsState(initial = true)
            val scope = rememberCoroutineScope()
            ListItem(
                modifier = Modifier.clickable {
                    if (isWorkoutInProgress) {
                        scope.launch {
                            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                            scaffoldState.snackbarHostState.showSnackbar("Please finish your workout first.")
                        }
                    } else {
                        exportDatabaseLauncher.launch("gymroutines_${viewModel.getCurrentTimeIso()}.db")
                    }
                },
                text = { Text("Backup") },
                secondaryText = { Text("Save routines, exercises and workouts in a file") },
                icon = { Icon(Icons.Default.SaveAlt, null) },
            )
            ListItem(
                modifier = Modifier.clickable {
                    if (isWorkoutInProgress) {
                        scope.launch {
                            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                            scaffoldState.snackbarHostState.showSnackbar("Please finish your workout first.")
                        }
                    } else {
                        importDatabaseLauncher.launch(emptyArray())
                    }
                },
                text = { Text("Restore") },
                secondaryText = { Text("Import a database file, overriding all data.") },
                icon = { Icon(Icons.Default.SettingsBackupRestore, null) },
            )
        }
    }
}
