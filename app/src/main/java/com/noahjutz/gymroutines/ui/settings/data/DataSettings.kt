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
import androidx.compose.ui.res.stringResource
import com.noahjutz.gymroutines.R
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
                title = stringResource(R.string.screen_data_settings),
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_pop_back))
                    }
                }
            )
        }
    ) {
        val exportDatabaseLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/vnd.sqlite3")
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
            val alertFinishWorkout = stringResource(R.string.alert_must_finish_workout)
            ListItem(
                modifier = Modifier.clickable {
                    if (isWorkoutInProgress) {
                        scope.launch {
                            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                            scaffoldState.snackbarHostState.showSnackbar(alertFinishWorkout)
                        }
                    } else {
                        exportDatabaseLauncher.launch(
                            "gymroutines_${viewModel.getCurrentTimeIso()}.db"
                        )
                    }
                },
                text = { Text(stringResource(R.string.pref_back_up_data)) },
                secondaryText = { Text(stringResource(R.string.pref_detail_back_up_data)) },
                icon = { Icon(Icons.Default.SaveAlt, null) }
            )
            ListItem(
                modifier = Modifier.clickable {
                    if (isWorkoutInProgress) {
                        scope.launch {
                            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                            scaffoldState.snackbarHostState.showSnackbar(alertFinishWorkout)
                        }
                    } else {
                        importDatabaseLauncher.launch(emptyArray())
                    }
                },
                text = { Text(stringResource(R.string.pref_restore_data)) },
                secondaryText = { Text(stringResource(R.string.pref_detail_restore_data)) },
                icon = { Icon(Icons.Default.SettingsBackupRestore, null) }
            )
        }
    }
}
