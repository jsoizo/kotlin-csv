package com.github.doyaaaaaken.kotlincsv.util.logger

/**
 * Logger interface for logging debug statements at runtime.
 * Library consumers may provide implementations suiting their needs.
 * @see [com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvReaderContext.logger]
 */
@Deprecated(
    message = "v1 logger hook; v2.0 does not expose a built-in logger interface.",
    level = DeprecationLevel.WARNING
)
interface Logger {
    fun info(message: String)
}
