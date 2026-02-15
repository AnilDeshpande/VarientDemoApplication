package com.codetutor.varientdemo.adsui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface AdsUi {
    @Composable fun Banner(modifier: Modifier = Modifier)
}