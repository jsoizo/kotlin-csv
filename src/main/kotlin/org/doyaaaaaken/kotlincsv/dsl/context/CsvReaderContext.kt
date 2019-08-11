package org.doyaaaaaken.kotlincsv.dsl.context

import org.doyaaaaaken.kotlincsv.util.Const
import java.nio.charset.Charset

interface ICsvReaderContext {
    val charset: Charset
    val quoteChar: Char
    val delimiter: Char
    val escapeChar: Char
    val withHeader: Boolean
}

class CsvReaderContext : ICsvReaderContext {
    override var charset = Const.defaultCharset
    override var quoteChar: Char = '"'
    override var delimiter: Char = ','
    override var escapeChar: Char = '\\'
    override var withHeader: Boolean = true
}
