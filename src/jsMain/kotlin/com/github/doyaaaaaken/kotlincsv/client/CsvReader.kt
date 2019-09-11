package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvReaderContext

/**
 * CSV Reader class
 *
 * @author doyaaaaaken
 */
actual class CsvReader actual constructor(
        private val ctx: CsvReaderContext
) : ICsvReaderContext by ctx {

    /**
     * read csv data as String, and convert into List<List<String>>
     */
    actual fun readAll(data: String): List<List<String>> {
        TODO("not implemented")
    }

    /**
     * read csv data with header, and convert into List<Map<String, String>>
     */
    actual fun readAllWithHeader(data: String): List<Map<String, String>> {
        TODO("not implemented")
    }
}
