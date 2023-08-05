package com.noahjutz.gymroutines.util

import java.text.DateFormat
import java.util.*
import kotlin.time.Duration

fun Date.formatSimple(): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(this)
}

fun Duration.pretty(): String = toComponents { h, m, _, _ -> "${h}h ${m}min" }
