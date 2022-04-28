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
    @Deprecated("Use [differentLengthFieldRow] specifier with behaviour set to IGNORE")
    val skipMissMatchedRow: Boolean

    /**
     * Sets the behaviour for dealing with rows with lengths that differ to the column count established by the first row.
     * See [DifferentNumberOfColumnsBehaviour]
     */
    val differentNumberOfColumnsBehaviour: DifferentNumberOfColumnsBehaviour

    /**
     * If a header occurs multiple times whether auto renaming should be applied when `readAllWithHeaderAsSequence()` (=throw an exception).
     *
     * Renaming is done based on occurrence and only applied from the first detected duplicate onwards.
     * ex:
     * [a,b,b,b,c,a] => [a,b,b_2,b_3,c,a_2]
     */
    val autoRenameDuplicateHeaders: Boolean
}

/**
 * Behaviour to use when subsequent rows have different numbers of columns from the original.
 *
 * [ERROR] - Throw an exception and stop processing (default).
 * [IGNORE] - Skip the row and continue processing.
 * [TRIM_EXCESS] - Trim the excess columns from the end of the row and use the data in the first n columns, where n is the
 * expected number of columns.
 */
enum class DifferentNumberOfColumnsBehaviour {
    ERROR,
    IGNORE,
    TRIM_EXCESS
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
    override var differentNumberOfColumnsBehaviour: DifferentNumberOfColumnsBehaviour = DifferentNumberOfColumnsBehaviour.ERROR
}
