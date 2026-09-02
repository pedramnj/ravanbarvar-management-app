package com.ravanbarvar.patientmanager.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NumberStepper(value: String, onIncrease: () -> Unit, onDecrease: () -> Unit, width: androidx.compose.ui.unit.Dp = 44.dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onDecrease) { Icon(Icons.Filled.Remove, contentDescription = null) }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.width(width),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onIncrease) { Icon(Icons.Filled.Add, contentDescription = null) }
    }
}
