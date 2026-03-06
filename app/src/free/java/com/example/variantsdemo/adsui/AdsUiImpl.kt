package com.example.variantsdemo.adsui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.codetutor.ads.AdBanner
import com.codetutor.varientdemo.adsui.AdsUi

class AdsUiImpl : AdsUi{
    @Composable
    override fun Banner(modifier: Modifier) {
        AdBanner(modifier)
    }
}
