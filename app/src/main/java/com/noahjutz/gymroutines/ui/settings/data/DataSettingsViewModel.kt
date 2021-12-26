package com.noahjutz.gymroutines.ui.settings.data

import android.net.Uri
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class DataSettingsViewModel : ViewModel() {
    fun exportDatabase(uri: Uri) {
        // TODO
    }

    fun importDatabase(uri: Uri) {
        // TODO
    }

    fun getCurrentTimeIso(): String {
        val now = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return formatter.format(now)
    }
}