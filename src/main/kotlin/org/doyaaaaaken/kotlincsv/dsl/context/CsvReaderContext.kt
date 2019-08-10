package org.doyaaaaaken.kotlincsv.dsl.context

import java.nio.charset.Charset

interface ICsvReaderContext {
    val charset: Charset
}

class CsvReaderContext: ICsvReaderContext {
    override var charset = Charsets.UTF_8
}
