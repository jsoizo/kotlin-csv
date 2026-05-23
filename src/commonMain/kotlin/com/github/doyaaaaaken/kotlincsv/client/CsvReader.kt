@file:Suppress("DEPRECATION")
package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.util.V2_MIGRATION_GUIDE_URL

/**
 * CSV Reader class
 *
 * @author doyaaaaaken
 */
@Deprecated(
    message = "Replaced by com.jsoizo.kotlincsv.reader.CsvReader in v2.0. " +
            "Prefer the top-level com.jsoizo.kotlincsv.csvReader DSL. " +
            "File I/O call shapes change, so rewrite call sites manually. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
expect class CsvReader(
    ctx: CsvReaderContext = CsvReaderContext()
) {
    /**
     * read csv data as String, and convert into List<List<String>>
     */
    fun readAll(data: String): List<List<String>>

    /**
     * read csv data with header, and convert into List<Map<String, String>>
     */
    fun readAllWithHeader(data: String): List<Map<String, String>>
}
