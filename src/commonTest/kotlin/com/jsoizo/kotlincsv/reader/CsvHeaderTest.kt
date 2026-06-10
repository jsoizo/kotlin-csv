package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.exceptions.MalformedCsvException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CsvHeaderTest {

    private fun seq(vararg rows: List<String>): Sequence<List<String>> = rows.asSequence()
    private fun nullableSeq(vararg rows: List<String?>): Sequence<List<String?>> = rows.asSequence()

    @Test
    fun basic_zipsHeaderWithRows() {
        val result = seq(
            listOf("a", "b", "c"),
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
        ).withHeader().toList()

        result shouldBe listOf(
            linkedMapOf("a" to "1", "b" to "2", "c" to "3"),
            linkedMapOf("a" to "4", "b" to "5", "c" to "6"),
        )
    }

    @Test
    fun rowShorterThanHeader_zipsToShorter() {
        val result = seq(
            listOf("a", "b", "c"),
            listOf("1", "2"),
        ).withHeader().toList()

        result.single() shouldBe linkedMapOf("a" to "1", "b" to "2")
    }

    @Test
    fun rowLongerThanHeader_zipsToShorter() {
        val result = seq(
            listOf("a", "b"),
            listOf("1", "2", "extra"),
        ).withHeader().toList()

        result.single() shouldBe linkedMapOf("a" to "1", "b" to "2")
    }

    @Test
    fun emptyInput_yieldsEmpty() {
        seq().withHeader().toList() shouldBe emptyList()
    }

    @Test
    fun headerOnly_yieldsEmpty() {
        seq(listOf("a", "b")).withHeader().toList() shouldBe emptyList()
    }

    @Test
    fun duplicateHeaders_default_throwsMalformedException() {
        val s = seq(listOf("a", "a"), listOf("1", "2"))
        shouldThrow<MalformedCsvException> {
            s.withHeader().toList()
        }
    }

    @Test
    fun duplicateHeaders_lazy_doesNotThrowBeforeTerminal() {
        val s = seq(listOf("a", "a"), listOf("1", "2"))
        // Building the pipeline does not throw.
        val result = s.withHeader()
        // toList triggers terminal operation.
        shouldThrow<MalformedCsvException> { result.toList() }
    }

    @Test
    fun autoRename_simpleDuplicate() {
        val result = seq(
            listOf("a", "a"),
            listOf("1", "2"),
        ).withHeader(autoRenameDuplicateHeaders = true).toList()

        result.single() shouldBe linkedMapOf("a" to "1", "a_2" to "2")
    }

    @Test
    fun autoRename_collidesWithExistingSuffix() {
        val result = seq(
            listOf("a", "a", "a_2"),
            listOf("1", "2", "3"),
        ).withHeader(autoRenameDuplicateHeaders = true).toList()

        result.single() shouldBe linkedMapOf("a" to "1", "a_3" to "2", "a_2" to "3")
    }

    @Test
    fun autoRename_emptyHeaderDuplicate() {
        val result = seq(
            listOf("", ""),
            listOf("1", "2"),
        ).withHeader(autoRenameDuplicateHeaders = true).toList()

        result.single() shouldBe linkedMapOf("" to "1", "_2" to "2")
    }

    @Test
    fun autoRename_threeWayDuplicateIsDeterministic() {
        val result = seq(
            listOf("a", "a", "a"),
            listOf("1", "2", "3"),
        ).withHeader(autoRenameDuplicateHeaders = true).toList()

        result.single() shouldBe linkedMapOf("a" to "1", "a_2" to "2", "a_3" to "3")
    }

    @Test
    fun resultPreservesHeaderInsertionOrder() {
        val result = seq(
            listOf("z", "y", "x"),
            listOf("1", "2", "3"),
        ).withHeader().toList()

        result.single().keys.toList() shouldBe listOf("z", "y", "x")
    }

    @Test
    fun nullableHeader_zipsHeaderWithNullableRows() {
        val result = nullableSeq(
            listOf("a", "b", "c"),
            listOf("1", null, ""),
        ).withNullableHeader().toList()

        result.single() shouldBe linkedMapOf("a" to "1", "b" to null, "c" to "")
    }

    @Test
    fun nullableHeader_nullHeaderIsEmptyHeaderForDuplicateCheck() {
        val s = nullableSeq(
            listOf(null, ""),
            listOf("1", "2"),
        )

        shouldThrow<MalformedCsvException> {
            s.withNullableHeader().toList()
        }
    }

    @Test
    fun nullableHeader_autoRenameEmptyHeaderDuplicates() {
        val result = nullableSeq(
            listOf(null, null, "x"),
            listOf("1", null, "3"),
        ).withNullableHeader(autoRenameDuplicateHeaders = true).toList()

        result.single() shouldBe linkedMapOf("" to "1", "_2" to null, "x" to "3")
    }

    @Test
    fun nullableHeader_readNullableNullHeadersBecomeEmptyHeaders() {
        val rows = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
        ).readNullable(",\n1,".asSequence())

        rows.withNullableHeader(autoRenameDuplicateHeaders = true).single() shouldBe
            linkedMapOf("" to "1", "_2" to null)
    }
}
