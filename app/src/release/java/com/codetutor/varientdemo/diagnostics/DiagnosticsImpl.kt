package com.codetutor.varientdemo.diagnostics

class DiagnosticsImpl: Diagnostics {
    override fun info(): String {
        return "Diagnostics disabled in release."
    }
}