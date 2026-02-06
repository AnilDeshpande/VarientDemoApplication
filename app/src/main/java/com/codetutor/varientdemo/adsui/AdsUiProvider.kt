package com.codetutor.varientdemo.adsui

import com.example.variantsdemo.adsui.AdsUiImpl

object AdsUiProvider {
    fun get(): AdsUi = AdsUiImpl()
}