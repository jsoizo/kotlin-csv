package com.jsoizo.kotlincsv.util.logger

/**
 * Logger interface for logging debug statements at runtime.
 * Library consumers may provide implementations suiting their needs.
 * @see [com.jsoizo.kotlincsv.dsl.context.ICsvReaderContext.logger]
 */
interface Logger {
    fun info(message: String)
}
