package org.doyaaaaaken.kotlincsv

import org.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import org.doyaaaaaken.kotlincsv.dsl.context.ICsvReaderContext

class CsvReader(ctx: CsvReaderContext = CsvReaderContext()) : ICsvReaderContext by ctx {

    fun read(data: String) {
        println(data)
    }
}
