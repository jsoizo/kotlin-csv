package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException
import com.jsoizo.kotlincsv.reader.internal.parseRowsWithMetadata
import com.jsoizo.kotlincsv.reader.internal.parseRowsWithMetadataFromChunks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LeadingWhitespaceTest {
    @Test
    fun leadingWhitespaceIsOptInAndOnlyRemovedBeforeOpeningQuotes() {
        val input = "\" \"\"T\"\" \", \"t\""
        assertFailsWith<CsvParseFormatException> { CsvReader().readAll(input) }
        val reader = csvReader { ignoreLeadingWhitespace = true }
        assertEquals(listOf(listOf(" \"T\" ", "t")), reader.readAll(input))
        assertEquals(listOf(listOf(" a \t", "b")), reader.readAll(" a \t, \t\"b\""))
        assertEquals(listOf(listOf(" \t", "")), reader.readAll(" \t,"))
        assertEquals(listOf(listOf(" \t")), reader.readAll(" \t"))
        assertEquals(emptyList(), reader.readAll(""))
        assertEquals(listOf(listOf("\u00a0a", "\uFEFFb")), reader.readAll("\u00a0a,\uFEFFb"))
        assertFailsWith<CsvParseFormatException> { reader.readAll("\u00a0\"a\"") }
        assertFailsWith<CsvParseFormatException> { reader.readAll("\uFEFF\"a\"") }
        assertFailsWith<CsvParseFormatException> { reader.readAll(" \"a\" ") }
    }

    @Test
    fun iteratorAndChunkPathsAgreeAcrossPrefixesQuotesAndTerminators() {
        val cases = listOf(
            " \t\"a\"\"b,c\",x\r\n\t\"\uD83D\uDE00\n z\", y" to
                listOf(listOf("a\"b,c", "x"), listOf("\uD83D\uDE00\n z", " y")),
            " \t".repeat(5000) + "\"a\", \t\"b\"" to listOf(listOf("a", "b")),
            " \t\r\n \"a\"\u2028\t\"b\"\u0085\"c\"\u2029" to
                listOf(listOf(" \t"), listOf("a"), listOf("b"), listOf("c")),
        )
        for ((input, expected) in cases) {
            val iterator = parseRowsWithMetadata(input.asSequence(), CsvDialect.RFC4180, true).toList()
            assertEquals(expected, iterator.map { row -> row.map { it.value } })
            for (size in listOf(2, 3, 8192)) {
                var offset = 0
                val chunks = parseRowsWithMetadataFromChunks({ buffer ->
                    val count = minOf(buffer.size, input.length - offset)
                    input.toCharArray(buffer, 0, offset, offset + count)
                    offset += count
                    count
                }, CsvDialect.RFC4180, false, size, true).toList()
                assertEquals(iterator, chunks, "chunk size $size")
            }
        }
    }

    @Test
    fun nullableQuotedMetadataAndImmutableConfigurationArePreserved() {
        val config = CsvReaderConfig().withIgnoreLeadingWhitespace(true)
        assertTrue(config.copy(skipEmptyLine = true).ignoreLeadingWhitespace)
        assertFalse(config.withIgnoreLeadingWhitespace(false).ignoreLeadingWhitespace)
        assertFalse(CsvReaderConfig().ignoreLeadingWhitespace)
        assertTrue(config != CsvReaderConfig())
        for (indicator in CsvNullFieldIndicator.entries) {
            val reader = CsvReader(config.copy(nullFieldIndicator = indicator))
            val quoted = if (indicator == CsvNullFieldIndicator.EMPTY_QUOTES || indicator == CsvNullFieldIndicator.BOTH) null else ""
            val unquoted = if (indicator == CsvNullFieldIndicator.EMPTY_SEPARATORS || indicator == CsvNullFieldIndicator.BOTH) null else ""
            assertEquals(listOf(listOf(quoted, unquoted, " \t")), reader.readAllNullable(" \t\"\",, \t"))
        }
    }

    @Test
    fun configurationOverloadsRemainSourceCompatible() {
        val legacy = CsvReaderConfig(CsvDialect.TSV, true, InsufficientFieldsRowBehaviour.ERROR,
            ExcessFieldsRowBehaviour.ERROR, CsvNullFieldIndicator.BOTH)
        assertFalse(legacy.ignoreLeadingWhitespace)
        assertEquals(legacy, CsvReaderConfig(dialect = CsvDialect.TSV, skipEmptyLine = true,
            nullFieldIndicator = CsvNullFieldIndicator.BOTH))
        val enabled = legacy.withIgnoreLeadingWhitespace(true)
        assertTrue(enabled.ignoreLeadingWhitespace)
        assertEquals(enabled, enabled.copy())
        assertTrue(enabled.copy(skipEmptyLine = false).ignoreLeadingWhitespace)
        assertTrue(enabled.copy(CsvDialect.RFC4180, false, InsufficientFieldsRowBehaviour.ERROR,
            ExcessFieldsRowBehaviour.ERROR, CsvNullFieldIndicator.NEITHER).ignoreLeadingWhitespace)
        assertFalse(enabled.withIgnoreLeadingWhitespace(false).ignoreLeadingWhitespace)
        assertFalse(enabled.copy(CsvDialect.TSV, true, InsufficientFieldsRowBehaviour.ERROR,
            ExcessFieldsRowBehaviour.ERROR, CsvNullFieldIndicator.BOTH)
            .withIgnoreLeadingWhitespace(false).ignoreLeadingWhitespace)
    }

    @Test
    fun untypedConfigurationReferencesPreserveFiveArgumentCalls() {
        val make = ::CsvReaderConfig
        val config = make(CsvDialect.TSV, true, InsufficientFieldsRowBehaviour.ERROR,
            ExcessFieldsRowBehaviour.ERROR, CsvNullFieldIndicator.BOTH)
        val duplicate = config::copy
        assertEquals(config, duplicate(CsvDialect.TSV, true, InsufficientFieldsRowBehaviour.ERROR,
            ExcessFieldsRowBehaviour.ERROR, CsvNullFieldIndicator.BOTH))
        val enabled = config.withIgnoreLeadingWhitespace(true)
        val duplicateEnabled = enabled::copy
        assertTrue(duplicateEnabled(CsvDialect.RFC4180, false, InsufficientFieldsRowBehaviour.ERROR,
            ExcessFieldsRowBehaviour.ERROR, CsvNullFieldIndicator.NEITHER).ignoreLeadingWhitespace)
    }

    @Test
    fun dialectCharactersAndEscapeRulesRetainTheirMeaning() {
        val tsv = CsvReader(CsvReaderConfig(dialect = CsvDialect.TSV).withIgnoreLeadingWhitespace(true))
        assertEquals(listOf(listOf("", "a")), tsv.readAll("\t \"a\""))
        val spaceDelimiter = CsvReader(CsvReaderConfig(
            dialect = CsvDialect(delimiter = ' ')).withIgnoreLeadingWhitespace(true))
        assertEquals(listOf(listOf("", "a")), spaceDelimiter.readAll(" \"a\""))
        val spaceQuote = CsvReader(CsvReaderConfig(
            dialect = CsvDialect(quoteChar = ' ', escapeChar = ' ')).withIgnoreLeadingWhitespace(true))
        assertEquals(listOf(listOf("a")), spaceQuote.readAll(" a "))
        val tabEscape = CsvReader(CsvReaderConfig(
            dialect = CsvDialect(escapeChar = '\t')).withIgnoreLeadingWhitespace(true))
        assertEquals(listOf(listOf(" \"a")), tabEscape.readAll(" \t\"a"))
        val backslash = CsvReader(CsvReaderConfig(
            dialect = CsvDialect(escapeChar = '\\')).withIgnoreLeadingWhitespace(true))
        assertEquals(listOf(listOf("a\"b\\c")), backslash.readAll(" \t\"a\\\"b\\\\c\""))
        assertEquals(listOf(listOf(" \"a")), backslash.readAll(" \\\"a"))
        assertFailsWith<CsvParseFormatException> { backslash.readAll(" \"a\\q\"") }
    }

    @Test
    fun errorsKeepOriginalLogicalRowAndColumnAcrossChunkBoundaries() {
        val cases = listOf(
            "a\r\n \t\"b\" x" to Triple(2L, 5L, ' '),
            " \t\"a" to Triple(1L, 4L, '"'),
            " \t\"a\", \"b\"x" to Triple(1L, 10L, 'x'),
        )
        for ((input, expected) in cases) {
            fun verify(error: CsvParseFormatException) {
                assertEquals(expected, Triple(error.rowNum, error.colIndex, error.char))
            }
            verify(assertFailsWith { csvReader { ignoreLeadingWhitespace = true }.readAll(input) })
            for (size in listOf(2, 3, 8192)) {
                var offset = 0
                verify(assertFailsWith {
                    parseRowsWithMetadataFromChunks({ buffer ->
                        val count = minOf(buffer.size, input.length - offset)
                        input.toCharArray(buffer, 0, offset, offset + count)
                        offset += count
                        count
                    }, CsvDialect.RFC4180, false, size, true).toList()
                })
            }
        }
    }
}
