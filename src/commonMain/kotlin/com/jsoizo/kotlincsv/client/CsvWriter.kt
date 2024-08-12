package com.jsoizo.kotlincsv.client

import com.jsoizo.kotlincsv.dsl.context.CsvWriterContext

/**
 * CSV Writer class
 *
 * @author doyaaaaaken
 */
expect class CsvWriter(ctx: CsvWriterContext = CsvWriterContext()) {

    fun open(targetFileName: String, append: Boolean = false, write: ICsvFileWriter.() -> Unit)

    fun writeAll(rows: List<List<Any?>>, targetFileName: String, append: Boolean = false)

    suspend fun writeAllAsync(rows: List<List<Any?>>, targetFileName: String, append: Boolean = false)

    suspend fun openAsync(targetFileName: String, append: Boolean = false, write: suspend ICsvFileWriter.() -> Unit)
}
