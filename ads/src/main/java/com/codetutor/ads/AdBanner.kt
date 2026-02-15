package com.codetutor.ads

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdBanner (modifier: Modifier = Modifier) {
    Surface (
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = modifier
    ) {
        Text(
            text = "AdBanner() from :ads module",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}