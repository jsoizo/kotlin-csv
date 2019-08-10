package org.doyaaaaaken.kotlincsv.dsl.context

import org.doyaaaaaken.kotlincsv.util.Const
import java.nio.charset.Charset

interface ICsvReaderContext {
    val charset: Charset
    val delimiter: Char
    val withHeader: Boolean
}

class CsvReaderContext : ICsvReaderContext {
    override var charset = Const.defaultCharset
    override var delimiter: Char = ','
    override var withHeader: Boolean = true
}
