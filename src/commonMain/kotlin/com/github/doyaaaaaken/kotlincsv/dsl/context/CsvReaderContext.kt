package com.github.doyaaaaaken.kotlincsv.dsl.context

import com.github.doyaaaaaken.kotlincsv.util.Const
import com.github.doyaaaaaken.kotlincsv.util.CsvDslMarker

/**
 * Interface for CSV Reader settings
 *
 * @author doyaaaaaken
 */
@CsvDslMarker
interface ICsvReaderContext {
    val charset: String
    val quoteChar: Char
    val delimiter: Char
    val escapeChar: Char
    val skipEmptyLine: Boolean
    val skipMissMatchedRow: Boolean
    val numberOfColumns: Int?
    val enableLogging: Boolean
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
    override var skipEmptyLine: Boolean = false
    override var skipMissMatchedRow: Boolean = false
    override var numberOfColumns: Int? = null
    override var enableLogging: Boolean = false
}
