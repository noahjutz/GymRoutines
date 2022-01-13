package com.noahjutz.gymroutines.util

import java.text.DateFormat
import java.util.*
import kotlin.time.Duration

fun Date.formatSimple(): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(this)
}

fun Duration.pretty(): String = toComponents { h, m, _, _ -> "${h}h ${m}min" }

fun Duration.iso8601(): String = toComponents { h, m, s, _ ->
    (if (h.toString().length < 2) "0" else "") +
        h.toString() +
        ":" +
        (if (m.toString().length < 2) "0" else "") +
        m.toString() +
        ":" +
        (if (s.toString().length < 2) "0" else "") +
        s.toString()
}
