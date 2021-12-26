package com.noahjutz.gymroutines.ui.settings.data

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.jakewharton.processphoenix.ProcessPhoenix
import com.noahjutz.gymroutines.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

class DataSettingsViewModel(
    private val database: AppDatabase,
    private val application: Application
) : ViewModel() {
    fun exportDatabase(uri: Uri) {
        database.close()
        val inStream = application.applicationContext
            .getDatabasePath("workout_routines_database")
            .inputStream()

        val outStream = application.applicationContext
            .contentResolver
            .openOutputStream(uri)

        inStream.use { input ->
            outStream?.use { output ->
                input.copyTo(output)
            }
        }
    }

    fun importDatabase(uri: Uri) {
        database.close()
        val inStream = application.applicationContext
            .contentResolver
            .openInputStream(uri)

        val databasePath = application.applicationContext
            .getDatabasePath("workout_routines_database")

        val outStream = databasePath.outputStream()

        inStream.use { input ->
            outStream.use { output ->
                input?.copyTo(output)
            }
        }
    }

    fun restartApp() = ProcessPhoenix.triggerRebirth(application.applicationContext)

    fun getCurrentTimeIso(): String {
        val now = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return formatter.format(now)
    }
}