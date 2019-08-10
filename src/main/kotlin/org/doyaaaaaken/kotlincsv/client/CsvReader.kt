package org.doyaaaaaken.kotlincsv.client

import org.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import org.doyaaaaaken.kotlincsv.dsl.context.ICsvReaderContext

class CsvReader(ctx: CsvReaderContext = CsvReaderContext()) : ICsvReaderContext by ctx {

    fun read(data: String): Sequence<List<String>> {
        println(data)
        return listOf(
                listOf("a1", "a2", "a3"),
                listOf("b1", "b2", "b3")
        ).asSequence()
    }
}
