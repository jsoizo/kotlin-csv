package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.reader.withHeader
import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalKotest::class)
class HeaderAutoRenamePropertyTest {

    private data class HeaderCase(
        val header: List<String>,
        val row: List<String>,
    )

    private val headerCaseArb: Arb<HeaderCase> =
        Arb.int(1..8).flatMap { headerSize ->
            Arb.bind(
                Arb.list(fieldArb, headerSize..headerSize),
                Arb.int(0..10).flatMap { rowSize ->
                    Arb.list(fieldArb, rowSize..rowSize)
                },
                ::HeaderCase,
            )
        }

    @Test
    fun autoRenameProducesUniqueKeysAndZipsToShorterWidth_property() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            headerCaseArb,
        ) { case ->
            val result = sequenceOf(case.header, case.row)
                .withHeader(autoRenameDuplicateHeaders = true)
                .single()

            val keys = result.keys.toList()
            keys.size shouldBe minOf(case.header.size, case.row.size)
            keys.toSet().size shouldBe keys.size
            result.values.toList() shouldBe case.row.take(case.header.size)
        }
    }

    @Test
    fun autoRenameLeavesUniqueHeadersUnchanged_property() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            uniqueHeaderCaseArb,
        ) { case ->
            val result = sequenceOf(case.header, case.row)
                .withHeader(autoRenameDuplicateHeaders = true)
                .single()

            result.keys.toList() shouldBe case.header.take(case.row.size)
        }
    }

    private val uniqueHeaderCaseArb: Arb<HeaderCase> =
        Arb.int(1..8).flatMap { headerSize ->
            Arb.bind(
                Arb.int(0..0).map { (0 until headerSize).map { index -> "header_$index" } },
                Arb.int(0..10).flatMap { rowSize ->
                    Arb.list(fieldArb, rowSize..rowSize)
                },
                ::HeaderCase,
            )
        }
}
