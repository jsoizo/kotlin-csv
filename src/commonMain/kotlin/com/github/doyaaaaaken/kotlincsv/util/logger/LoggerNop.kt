package com.github.doyaaaaaken.kotlincsv.util.logger

/**
 * Internal no-operation logger implementation, which does not log anything.
 */
internal object LoggerNop : Logger {
    override fun info(message: () -> Any?) = Unit
}
