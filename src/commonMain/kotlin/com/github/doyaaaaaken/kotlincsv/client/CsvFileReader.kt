package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ExcessFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.parser.CsvParser
import com.github.doyaaaaaken.kotlincsv.util.CSVAutoRenameFailedException
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import com.github.doyaaaaaken.kotlincsv.util.logger.Logger
import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException

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
            // If no expected number of fields was passed in, then set it based on the first row.
            if (expectedNumFieldsInRow == null) expectedNumFieldsInRow = row.size
            // Assign this number to a non-nullable type to avoid need for thread-safety null checks.
            val numFieldsInRow: Int = expectedNumFieldsInRow ?: row.size
            @Suppress("DEPRECATION")
            if (row.size > numFieldsInRow) {
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
        @Suppress("DEPRECATION")
        var headers = readNext() ?: return emptySequence()
        if (ctx.autoRenameDuplicateHeaders) {
            headers = deduplicateHeaders(headers)
        } else {
            val duplicated = findDuplicate(headers)
            if (duplicated != null) throw MalformedCSVException("header '$duplicated' is duplicated. please consider to use 'autoRenameDuplicateHeaders' option.")
        }
        return readAllAsSequence(headers.size).map { fields -> headers.zip(fields).toMap() }
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
