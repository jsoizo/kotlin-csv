package com.github.doyaaaaaken.kotlincsv.util.logger

/**
 * Logger interface for logging debug statements at runtime.
 * Library consumers may provide implementations suiting their needs.
 * @see [com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvReaderContext.logger]
 */
interface Logger {
    fun info(message: String)
}
