package com.codetutor.varientdemo
import android.app.Activity

/** Intentionally leaks an Activity by storing a static strong reference. */
object ActivityLeaker {
    private var retained: Activity? = null

    fun leak(activity: Activity) {
        retained = activity
    }
}
