package com.github.doyaaaaaken.kotlincsv.dsl.context

import com.github.doyaaaaaken.kotlincsv.util.Const
import com.github.doyaaaaaken.kotlincsv.util.CsvDslMarker
import java.nio.charset.Charset

/**
 * Interface for CSV Writer settings
 *
 * @author doyaaaaaken
 */
@CsvDslMarker
interface ICsvWriterContext {
    val charset: Charset
    val delimiter: Char
    val nullCode: String
    val lineTerminator: String
    val quote: CsvWriteQuoteContext
}

/**
 * CSV Writer settings used in `csvWriter` DSL method.
 *
 * @author doyaaaaaken
 */
@CsvDslMarker
class CsvWriterContext : ICsvWriterContext {
    override var charset = Const.defaultCharset
    override var delimiter: Char = ','
    override var nullCode: String = ""
    override var lineTerminator: String = "\r\n"
    override val quote: CsvWriteQuoteContext = CsvWriteQuoteContext()

    fun quote(init: CsvWriteQuoteContext.() -> Unit = {}) {
        quote.init()
    }
}
