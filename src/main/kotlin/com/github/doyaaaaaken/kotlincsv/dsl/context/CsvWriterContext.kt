package com.github.doyaaaaaken.kotlincsv.dsl.context

import com.github.doyaaaaaken.kotlincsv.util.Const
import java.nio.charset.Charset

interface ICsvWriterContext {
    val charset: Charset
    val delimiter: Char
//    val quoteChar: Char
    val lineTerminator: String
}

class CsvWriterContext : ICsvWriterContext {
    override var charset = Const.defaultCharset
    override var delimiter: Char = ','
//    override var quoteChar: Char = '"'
    override var lineTerminator: String = "\r\n"
}
