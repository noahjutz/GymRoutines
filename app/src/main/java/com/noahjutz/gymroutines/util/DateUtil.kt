package com.noahjutz.gymroutines.util

import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import org.ocpsoft.prettytime.PrettyTime

private val prettyTime = PrettyTime()

fun Date.pretty(): String = prettyTime.format(this)

@OptIn(ExperimentalTime::class)
fun Duration.pretty(): String = this.toComponents { h, m, _, _ -> "${h}h ${m}min" }
