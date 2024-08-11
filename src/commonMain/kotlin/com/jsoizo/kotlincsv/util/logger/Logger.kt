package com.jzoizo.kotlincsv.util.logger

/**
 * Logger interface for logging debug statements at runtime.
 * Library consumers may provide implementations suiting their needs.
 * @see [com.jzoizo.kotlincsv.dsl.context.ICsvReaderContext.logger]
 */
interface Logger {
    fun info(message: String)
}
