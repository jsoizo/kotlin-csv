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
    /**
     * Charset encoding
     *
     * The name must be supported by [java.nio.charset.Charset].
     *
     * ex.)
     *     "UTF-8"
     *     "SJIS"
     */
    val charset: String

    /**
     * Character used as quote between each fields
     *
     * ex.)
     *     '"'
     *     '\''
     */
    val quoteChar: Char

    /**
     * Character used as delimiter between each fields
     *
     * ex.)
     *     ","
     *     "\t" (TSV file)
     */
    val delimiter: Char

    /**
     * Character to escape quote inside field string.
     * Normally, you don't have to change this option.
     *
     * According to [CSV specification](https://tools.ietf.org/html/rfc4180#section-2),
     * > If double-quotes are used to enclose fields, then a double-quote appearing inside a field must be escaped by preceding it with another double quote.
     * > For example:
     * > "aaa","b""bb","ccc"
     */
    val escapeChar: Char

    /**
     * If empty line is found, skip it or not (=throw an exception).
     */
    val skipEmptyLine: Boolean

    /**
     * If a invalid row which has different number of fields from other rows is found, skip it or not (=throw an exception).
     */
    val skipMissMatchedRow: Boolean

    /**
     * If a header occurs multiple times weather auto renaming should be applied when `readAllWithHeaderAsSequence()` (=throw an exception).
     */
    val autoRenameDuplicateHeaders: Boolean
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
    override var autoRenameDuplicateHeaders: Boolean = false
}
