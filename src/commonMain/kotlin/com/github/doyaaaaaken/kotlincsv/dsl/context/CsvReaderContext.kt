package com.github.doyaaaaaken.kotlincsv.dsl.context

import com.github.doyaaaaaken.kotlincsv.util.Const
import com.github.doyaaaaaken.kotlincsv.util.CsvDslMarker
import com.github.doyaaaaaken.kotlincsv.util.logger.Logger
import com.github.doyaaaaaken.kotlincsv.util.logger.LoggerNop

/**
 * Interface for CSV Reader settings
 *
 * @author doyaaaaaken
 */
@CsvDslMarker
interface ICsvReaderContext {

    /**
     * Logger instance for logging debug statements.
     * Default instance does not log anything.
     */
    val logger: Logger

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
    @Deprecated("Use insufficientFieldsRowBehaviour and excessRowsBehaviour to specify 'ignore'")
    val skipMissMatchedRow: Boolean

    /**
     * If a header occurs multiple times whether auto renaming should be applied when `readAllWithHeaderAsSequence()` (=throw an exception).
     *
     * Renaming is done based on occurrence and only applied from the first detected duplicate onwards.
     * ex:
     * [a,b,b,b,c,a] => [a,b,b_2,b_3,c,a_2]
     */
    val autoRenameDuplicateHeaders: Boolean

    /**
     * If a row does not have the expected number of fields (columns), how, and if, the reader should proceed
     */
    val insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour

    /**
     * If a row exceeds have the expected number of fields (columns), how, and if, the reader should proceed
     */
    val excessFieldsRowBehaviour: ExcessFieldsRowBehaviour

    /**
     * Configures which field values should be handled as null value by the reader.
     */
    val withFieldAsNull: CSVReaderNullFieldIndicator
}

enum class InsufficientFieldsRowBehaviour {

    /**
     * Throw an exception (default)
     */
    ERROR,

    /**
     * Ignore the row and skip to the next row
     */
    IGNORE,
    /**
     * Treat missing fields as an empty string
     */
    EMPTY_STRING
}

enum class ExcessFieldsRowBehaviour {

    /**
     * Throw an exception (default)
     */
    ERROR,

    /**
     * Ignore the row and skip to the next row
     */
    IGNORE,

    /**
     * Trim the excess fields from the row (e.g. if 8 fields are present and 7 are expected, return the first 7 fields)
     */
    TRIM
}

enum class CSVReaderNullFieldIndicator {

    /**
     * Two sequential separators are null.
     */
    EMPTY_SEPARATORS,

    /**
     * Two sequential quotes are null.
     */
    EMPTY_QUOTES,

    /**
     * Two sequential separators and two sequential quotes are null.
     */
    BOTH,

    /**
     * Default. Both are considered empty string.
     */
    NEITHER
}

/**
 * CSV Reader settings used in `csvReader` DSL method.
 *
 * @author doyaaaaaken
 */
@CsvDslMarker
class CsvReaderContext : ICsvReaderContext {
    override var logger: Logger = LoggerNop
    override var charset = Const.defaultCharset
    override var quoteChar: Char = '"'
    override var delimiter: Char = ','
    override var escapeChar: Char = '"'
    override var skipEmptyLine: Boolean = false
    override var skipMissMatchedRow: Boolean = false
    override var autoRenameDuplicateHeaders: Boolean = false
    override var insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.ERROR
    override var excessFieldsRowBehaviour: ExcessFieldsRowBehaviour = ExcessFieldsRowBehaviour.ERROR
    override var withFieldAsNull: CSVReaderNullFieldIndicator = CSVReaderNullFieldIndicator.NEITHER
}
