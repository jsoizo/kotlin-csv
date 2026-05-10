package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.exceptions.MalformedCsvException
import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalKotest::class)
class ParseStateMachineCrashFreedomTest {

    private val textArb: Arb<String> =
        Arb.list(anyChar, 0..64).map { it.joinToString("") }

    @Test
    fun crashFreedom_property() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            dialectArb,
            textArb,
        ) { dialect, text ->
            try {
                csvReader { this.dialect = dialect }.readAll(text)
            } catch (_: MalformedCsvException) {
                // declared parse failure; any other Throwable bubbles up
                // and lets kotest-property shrink to a minimal reproduction.
            }
        }
    }
}
