package com.github.doyaaaaaken.kotlincsv.dsl.context

import com.github.doyaaaaaken.kotlincsv.util.Const
import com.github.doyaaaaaken.kotlincsv.util.CsvDslMarker
import java.nio.charset.Charset

/**
 * Interface for CSV Reader settings
 *
 * @author doyaaaaaken
 */
@CsvDslMarker
interface ICsvReaderContext {
    val charset: Charset
    val quoteChar: Char
    val delimiter: Char
    val escapeChar: Char
}

/**
 * CSV Reader settings used in `csvReader` DSL method.
 *
 * @author doyaaaaaken
 */
@CsvDslMarker
class CsvReaderContext : ICsvReaderContext {
    override var charset = Const.defaultCharset
    override var quoteChar: Char = '"'
    override var delimiter: Char = ','
    override var escapeChar: Char = '"'
}
