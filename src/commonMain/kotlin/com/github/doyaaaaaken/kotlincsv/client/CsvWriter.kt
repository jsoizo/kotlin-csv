@file:Suppress("DEPRECATION")
package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.util.V2_MIGRATION_GUIDE_URL

/**
 * CSV Writer class
 *
 * @author doyaaaaaken
 */
@Deprecated(
    message = "Replaced by com.jsoizo.kotlincsv.writer.CsvWriter in v2.0. " +
            "Prefer the top-level com.jsoizo.kotlincsv.csvWriter DSL. " +
            "File I/O and nullable row handling change, so rewrite call sites manually. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
expect class CsvWriter(ctx: CsvWriterContext = CsvWriterContext()) {

    fun open(targetFileName: String, append: Boolean = false, write: ICsvFileWriter.() -> Unit)

    fun writeAll(rows: List<List<Any?>>, targetFileName: String, append: Boolean = false)

    suspend fun writeAllAsync(rows: List<List<Any?>>, targetFileName: String, append: Boolean = false)

    suspend fun openAsync(targetFileName: String, append: Boolean = false, write: suspend ICsvFileWriter.() -> Unit)
}
