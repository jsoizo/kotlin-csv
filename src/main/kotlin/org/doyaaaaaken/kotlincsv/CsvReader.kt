package org.doyaaaaaken.kotlincsv

import org.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext

class CsvReader(ctx: CsvReaderContext = CsvReaderContext()) {

    fun read(data: String) {
        println(data)
    }
}
