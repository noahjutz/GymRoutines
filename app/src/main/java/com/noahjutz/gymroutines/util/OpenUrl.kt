package com.noahjutz.gymroutines.util

import android.content.Intent
import android.net.Uri
import com.noahjutz.gymroutines.ui.MainActivity

fun MainActivity.openUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) })
}
