package com.codetutor.varientdemo.diagnostics

class DiagnosticsImpl : Diagnostics {
    override fun info(): String = "Diagnostics disabled in release."
}