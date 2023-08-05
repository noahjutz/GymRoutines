package com.noahjutz.gymroutines.ui.routines.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.material.placeholder

@Composable
fun RoutineEditorPlaceholder() {
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .padding(top = 16.dp, start = 30.dp, end = 30.dp)
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(percent = 100))
                .placeholder(visible = true)
        )
        Box(
            Modifier
                .padding(top = 30.dp)
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(30.dp))
                .placeholder(visible = true)
        )
    }
}
