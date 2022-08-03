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
     * read csv data as String, and convert into List<List<String>>
     */
    fun readAll(data: String): List<List<String>>

    /**
     * read csv data with header, and convert into List<Map<String, String>>
     */
    fun readAllWithHeader(data: String): List<Map<String, String>>

    /**
     * read the specified number of rows, and convert into List<List<String>>
     */
    fun read(data: String, numberOfRows: Int = -1): List<List<String>>

    /**
     * read the specified number of rows with header, and convert into List<Map<String, String>>
     */
    fun readWithHeader(data: String, numberOfRows: Int = -1): List<Map<String, String>>
}
