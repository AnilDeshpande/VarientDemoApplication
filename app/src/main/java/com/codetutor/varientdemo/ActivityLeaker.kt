package com.codetutor.varientdemo

import android.app.Activity

object ActivityLeaker {
    private var retained: Activity? = null

    fun leak(activity: MainActivity){
        retained = activity
    }
}