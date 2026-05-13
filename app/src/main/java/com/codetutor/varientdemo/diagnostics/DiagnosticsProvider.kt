package com.codetutor.varientdemo.diagnostics

object DiagnosticsProvider {
    fun get(): Diagnostics = DiagnosticsImpl()
}
