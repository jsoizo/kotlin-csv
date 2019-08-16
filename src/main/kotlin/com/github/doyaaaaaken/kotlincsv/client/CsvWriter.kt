package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvWriterContext
import com.github.doyaaaaaken.kotlincsv.parser.CsvParser

class CsvWriter(ctx: CsvWriterContext = CsvWriterContext()) : ICsvWriterContext by ctx {

    private val parser = CsvParser()

    fun write(data: String): List<List<String>> {
        TODO()
    }
}
