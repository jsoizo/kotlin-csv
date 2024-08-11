package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext

/**
 * CSV Writer class
 *
 * @author doyaaaaaken
 */
actual class CsvWriter actual constructor(ctx: CsvWriterContext) {

    actual fun open(targetFileName: String, append: Boolean, write: ICsvFileWriter.() -> Unit) {
        TODO("Not Implemented")
    }

    actual fun writeAll(rows: List<List<Any?>>, targetFileName: String, append: Boolean) {
        TODO("Not Implemented")
    }

    actual suspend fun writeAllAsync(rows: List<List<Any?>>, targetFileName: String, append: Boolean) {
        TODO("Not Implemented")
    }

    actual suspend fun openAsync(targetFileName: String, append: Boolean, write: suspend ICsvFileWriter.() -> Unit) {
        TODO("Not Implemented")
    }
}
