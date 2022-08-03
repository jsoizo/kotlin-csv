package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ExcessFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.parser.CsvParser
import com.github.doyaaaaaken.kotlincsv.util.CSVAutoRenameFailedException
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException
import com.github.doyaaaaaken.kotlincsv.util.logger.Logger

/**
 * CSV Reader class, which controls file I/O flow.
 *
 * @author doyaaaaaken
 */
class CsvFileReader internal constructor(
    private val ctx: CsvReaderContext,
    reader: Reader,
    private val logger: Logger,
) {

    private val reader = BufferedLineReader(reader)
    private var rowNum = 0L

    private val parser = CsvParser(ctx.quoteChar, ctx.delimiter, ctx.escapeChar)

    /**
     * read next csv row
     * (which may contain multiple lines, because csv fields may contain line feed)
     *
     * @return return fields in row as List<String>.
     *         or return null, if all line are already read.
     */
    @Deprecated("We are considering making it a private method. If you have feedback, please comment on Issue #100.")
    fun readNext(): List<String>? {
        return readUntilNextCsvRow("")
    }

    /**
     * read all csv rows as Sequence
     */
    fun readAllAsSequence(fieldsNum: Int? = null): Sequence<List<String>> {
        var expectedNumFieldsInRow: Int? = fieldsNum
        return generateSequence {
            @Suppress("DEPRECATION") readNext()
        }.mapIndexedNotNull { idx, row ->
            val numberOfFields = expectedNumFieldsInRow ?: row.size
            // If no expected number of fields was passed in, then set it based on the first row.
            if (expectedNumFieldsInRow == null) expectedNumFieldsInRow = numberOfFields
            readRow(idx, row, numberOfFields)
        }
    }

    /**
     * read the specified number of csv rows as Sequence
     *
     * if the requested number of rows is negative, read all rows
     * if the requested number of rows is zero, return an empty sequence
     */
    fun readAsSequence(numberOfRows: Int, fieldsNum: Int? = null): Sequence<List<String>> {
        if (numberOfRows < 0) return readAllAsSequence(fieldsNum)
        if (numberOfRows == 0) return emptySequence()

        var expectedNumFieldsInRow: Int? = fieldsNum
        return generateSequence {
            @Suppress("DEPRECATION") readNext()
        }.take(numberOfRows).mapIndexedNotNull { idx, row ->
            val numberOfFields = expectedNumFieldsInRow ?: row.size
            // If no expected number of fields was passed in, then set it based on the first row.
            if (expectedNumFieldsInRow == null) expectedNumFieldsInRow = numberOfFields
            readRow(idx, row, numberOfFields)
        }
    }

    private fun readRow(idx: Int, row: List<String>, numFieldsInRow: Int): List<String>? {
        @Suppress("DEPRECATION")
        return if (row.size > numFieldsInRow) {
            if (ctx.excessFieldsRowBehaviour == ExcessFieldsRowBehaviour.TRIM) {
                logger.info("trimming excess rows. [csv row num = ${idx + 1}, fields num = ${row.size}, fields num of row = $numFieldsInRow]")
                row.subList(0, numFieldsInRow)
            } else if (ctx.skipMissMatchedRow || ctx.excessFieldsRowBehaviour == ExcessFieldsRowBehaviour.IGNORE) {
                skipMismatchedRow(idx, row, numFieldsInRow)
            } else {
                throw CSVFieldNumDifferentException(numFieldsInRow, row.size, idx + 1)
            }
        } else if (numFieldsInRow != row.size) {
            if (ctx.skipMissMatchedRow || ctx.insufficientFieldsRowBehaviour == InsufficientFieldsRowBehaviour.IGNORE) {
                skipMismatchedRow(idx, row, numFieldsInRow)
            } else {
                throw CSVFieldNumDifferentException(numFieldsInRow, row.size, idx + 1)
            }
        } else {
            row
        }
    }

    private fun skipMismatchedRow(
        idx: Int,
        row: List<String>,
        numFieldsInRow: Int
    ): Nothing? {
        logger.info("skip miss matched row. [csv row num = ${idx + 1}, fields num = ${row.size}, fields num of first row = $numFieldsInRow]")
        return null
    }

    /**
     * read all csv rows as Sequence with header information
     */
    fun readAllWithHeaderAsSequence(): Sequence<Map<String, String>> {
        val headers = readHeader()
        return readAllAsSequence(headers.size).map { fields -> headers.zip(fields).toMap() }
    }

    /**
     * read the specified number of csv rows as Sequence with header information
     */
    fun readWithHeaderAsSequence(numberOfRows: Int): Sequence<Map<String, String>> {
        val headers = readHeader()
        return readAsSequence(numberOfRows, headers.size).map { fields -> headers.zip(fields).toMap() }
    }

    private fun readHeader(): List<String> {
        @Suppress("DEPRECATION")
        var headers = readNext() ?: return emptyList()
        if (ctx.autoRenameDuplicateHeaders) {
            headers = deduplicateHeaders(headers)
        } else {
            val duplicated = findDuplicate(headers)
            if (duplicated != null) throw MalformedCSVException("header '$duplicated' is duplicated. please consider to use 'autoRenameDuplicateHeaders' option.")
        }
        return headers
    }

    fun close() {
        reader.close()
    }

    /**
     * read next csv row (which may contain multiple lines)
     *
     * @return return fields in row as List<String>.
     *         or return null, if all line are already read.
     */
    private tailrec fun readUntilNextCsvRow(leftOver: String = ""): List<String>? {
        val nextLine = reader.readLineWithTerminator()
        rowNum++
        return if (nextLine == null) {
            if (leftOver.isNotEmpty()) {
                throw MalformedCSVException("\"$leftOver\" on the tail of file is left on the way of parsing row")
            } else {
                null
            }
        } else if (ctx.skipEmptyLine && nextLine.isBlank() && leftOver.isBlank()) {
            readUntilNextCsvRow(leftOver)
        } else {
            val value = if (leftOver.isEmpty()) {
                "$nextLine"
            } else {
                "$leftOver$nextLine"
            }
            parser.parseRow(value, rowNum) ?: readUntilNextCsvRow("$leftOver$nextLine")
        }
    }

    private fun findDuplicate(headers: List<String>): String? {
        val set = mutableSetOf<String>()
        headers.forEach { h ->
            if (set.contains(h)) {
                return h
            } else {
                set.add(h)
            }
        }
        return null
    }

    /**
     * deduplicate headers based on occurrence by appending "_<NUM>"
     * Ex: [a,b,b,b,c,a] => [a,b,b_2,b_3,c,a_2]
     *
     * @return return headers as List<String>.
     */
    private fun deduplicateHeaders(headers: List<String>): List<String> {
        val occurrences = mutableMapOf<String, Int>()
        return headers.map { header ->
            val count = occurrences.getOrPut(header) { 0 } + 1
            occurrences[header] = count
            when {
                count > 1 -> "${header}_$count"
                else -> header
            }
        }.also { results ->
            if (results.size != results.distinct().size) throw CSVAutoRenameFailedException()
        }
    }
}
