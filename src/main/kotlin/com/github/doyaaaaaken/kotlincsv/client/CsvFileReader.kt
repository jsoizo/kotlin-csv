package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.parser.CsvParser
import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException
import java.io.BufferedReader
import java.io.Closeable

/**
 * CSV Reader class, which controls file I/O flow.
 *
 * @author doyaaaaaken
 */
class CsvFileReader internal constructor(
        private val ctx: CsvReaderContext,
        reader: BufferedReader
) : Closeable {

    private val reader = BufferedLineReader(reader)

    private val parser = CsvParser(ctx.quoteChar, ctx.delimiter, ctx.escapeChar)

    fun readAll(): List<List<String>> {
        return readAllAsSequence().toList()
    }

    fun readAllWithHeader(): List<Map<String, String>> {
        val headers = readNext()
        val duplicated = headers?.let(::findDuplicate)
        if (duplicated != null) throw MalformedCSVException("header '$duplicated' is duplicated")

        return generateSequence { readNext() }.map { fields ->
            if (requireNotNull(headers).size != fields.size) {
                throw MalformedCSVException("fields num  ${fields.size} is not matched with header num ${headers.size}")
            }
            headers.zip(fields).toMap()
        }.toList()
    }

    fun readAllAsSequence(): Sequence<List<String>> {
        return generateSequence { readNext() }
    }

    override fun close() {
        reader.close()
    }

    private tailrec fun readNext(leftOver: String = ""): List<String>? {
        val nextLine = reader.readLineWithTerminator()
        return if (nextLine == null) {
            if (leftOver.isNotEmpty()) {
                throw MalformedCSVException("Malformed format: leftOver \"$leftOver\" on the tail of file")
            } else {
                null
            }
        } else {
            val value = if (leftOver.isEmpty()) {
                "$nextLine"
            } else {
                "$leftOver$nextLine"
            }
            val parsedLine = parser.parseRow(value)
            if (parsedLine == null) {
                readNext("$leftOver$nextLine")
            } else {
                parsedLine
            }
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
}
