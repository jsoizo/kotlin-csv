package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext

/**
 * CSV Writer class
 *
 * @author doyaaaaaken
 */
expect class CsvWriter(ctx: CsvWriterContext = CsvWriterContext()) {

    fun open(targetFileName: String, append: Boolean = false, write: ICsvFileWriter.() -> Unit)

    fun writeAll(rows: List<List<Any?>>, targetFileName: String, append: Boolean = false)
}
