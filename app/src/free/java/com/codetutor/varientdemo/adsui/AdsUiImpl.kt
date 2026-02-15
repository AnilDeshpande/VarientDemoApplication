package com.codetutor.varientdemo.adsui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codetutor.ads.AdBanner

class AdsUiImpl: AdsUi {
    @Composable
    override fun Banner(modifier: Modifier) {
        AdBanner(modifier)
    }
}