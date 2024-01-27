package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvReaderContext

/**
 * Base implementation for a [CsvReader] that reads from Strings.
 *
 * @author doyaaaaaken
 * @author gsteckman
 */
open class CsvStringReader(
    private val ctx: CsvReaderContext
) : ICsvReaderContext by ctx {

    /**
     * read csv data as String, and convert into List<List<String>>
     */
    fun readAll(data: String): List<List<String>> {
        return CsvFileReader(ctx, StringReaderImpl(data), logger).readAllAsSequence().toList()
    }

    /**
     * read csv data with header, and convert into List<Map<String, String>>
     */
    fun readAllWithHeader(data: String): List<Map<String, String>> {
        return CsvFileReader(ctx, StringReaderImpl(data), logger).readAllWithHeaderAsSequence().toList()
    }
}
