package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.parser.CsvParser
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException
import mu.KotlinLogging

/**
 * CSV Reader class, which controls file I/O flow.
 *
 * @author doyaaaaaken
 */
class CsvFileReader internal constructor(
        private val ctx: CsvReaderContext,
        reader: Reader
) {

    private val logger = KotlinLogging.logger {  }
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
    fun readNext(): List<String>? {
        return readUntilNextCsvRow("")
    }

    /**
     * read all csv rows as Sequence
     */
    fun readAllAsSequence(fieldsNum: Int? = null): Sequence<List<String>> {
        var fieldsNumInRow: Int? = fieldsNum
        return generateSequence {
            readNext()
        }.mapIndexedNotNull { idx, row ->
            if (fieldsNumInRow == null) fieldsNumInRow = row.size
            if (fieldsNumInRow != row.size) {
                if (ctx.skipMissMatchedRow) {
                    logger.info{"skip miss matched row. [csv row num = ${idx + 1}, fields num = ${row.size}, fields num of first row = $fieldsNumInRow]"}
                    null
                } else {
                    throw CSVFieldNumDifferentException(requireNotNull(fieldsNumInRow), row.size, idx + 1)
                }
            } else {
                row
            }
        }
    }

    /**
     * read all csv rows as Sequence with header information
     */
    fun readAllWithHeaderAsSequence(): Sequence<Map<String, String>> {
        var headers = readNext() ?: return emptySequence()
        if (ctx.autoRenameDuplicateHeaders) {
            headers = deduplicateHeaders(headers)
        } else {
            val duplicated = findDuplicate(headers)
            if (duplicated != null) throw MalformedCSVException("header '$duplicated' is duplicated")
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
     * Ex: [a,b,b,c,a] => [a,b,b_2,c,a_2]
     *
     * @return return headers as List<String>.
     *
     */
    private fun deduplicateHeaders(headers: List<String>): List<String> {
        val occurrences = headers.toSet().associateWith { 1 }.toMutableMap()
        return headers.map { header ->
            occurrences[header]?.let { occurred ->
                when {
                    occurred > 1 -> "${header}_$occurred"
                    else -> header
                }.also {
                    occurrences[header] = occurred + 1
                }
            } ?: throw RuntimeException("a")
        }
    }
}
