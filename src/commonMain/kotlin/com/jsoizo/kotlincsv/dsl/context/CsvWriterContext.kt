package com.jsoizo.kotlincsv.dsl.context

import com.jsoizo.kotlincsv.util.Const
import com.jsoizo.kotlincsv.util.CsvDslMarker

/**
 * Interface for CSV Writer settings
 *
 * @author doyaaaaaken
 */
@CsvDslMarker
interface ICsvWriterContext {
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
     * Character used as delimiter between each fields
     *
     * ex.)
     *     ","
     *     "\t" (TSV file)
     */
    val delimiter: Char

    /**
     * Character used when a written field is null value
     *
     * ex.)
     *     "" (empty field)
     *     "NULL"
     *     "-"
     */
    val nullCode: String

    /**
     * Character used as line terminator
     *
     * ex.)
     *     "\r\n"
     *     "\n"
     */
    val lineTerminator: String

    /**
     * Output line break at the end of file or not.
     *
     * According to [CSV specification](https://tools.ietf.org/html/rfc4180#section-2),
     * > The last record in the file may or may not have an ending line break.
     */
    val outputLastLineTerminator: Boolean

    /**
     * Output BOM (Byte Order Mark) at the beginning of file or not.
     * See https://github.com/doyaaaaaken/kotlin-csv/issues/84
     */
    val prependBOM: Boolean

    /**
     * Options about quotes of each fields
     */
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
    override var outputLastLineTerminator = true
    override var prependBOM = false
    override val quote: CsvWriteQuoteContext = CsvWriteQuoteContext()

    fun quote(init: CsvWriteQuoteContext.() -> Unit) {
        quote.init()
    }
}
