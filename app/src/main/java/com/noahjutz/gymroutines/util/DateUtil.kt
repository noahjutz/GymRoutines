package com.noahjutz.gymroutines.util

import java.text.DateFormat
import java.util.Date
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun Date.formatSimple(): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(this)
}

@OptIn(ExperimentalTime::class)
fun Duration.pretty(): String = this.toComponents { h, m, _, _ -> "${h}h ${m}min" }
