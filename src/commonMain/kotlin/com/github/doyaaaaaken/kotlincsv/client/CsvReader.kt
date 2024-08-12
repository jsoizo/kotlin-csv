package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext

/**
 * CSV Reader class
 *
 * @author doyaaaaaken
 */
expect class CsvReader(
    ctx: CsvReaderContext = CsvReaderContext()
) {
    /**
     * read csv data as String, and convert into List<List<String?>>
     */
    fun readAll(data: String): List<List<String?>>

    /**
     * read csv data with header, and convert into List<Map<String, String?>>
     */
    fun readAllWithHeader(data: String): List<Map<String, String?>>
}
