package org.doyaaaaaken.kotlincsv.dsl.context

import org.doyaaaaaken.kotlincsv.util.Const
import java.nio.charset.Charset

interface ICsvReaderContext {
    val charset: Charset
}

class CsvReaderContext : ICsvReaderContext {
    override var charset = Const.defaultCharset
}
