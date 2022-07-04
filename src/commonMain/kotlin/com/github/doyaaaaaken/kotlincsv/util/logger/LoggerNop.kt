package com.github.doyaaaaaken.kotlincsv.util.logger

internal object LoggerNop : Logger {
    override fun info(message: () -> Any?) = Unit
}
