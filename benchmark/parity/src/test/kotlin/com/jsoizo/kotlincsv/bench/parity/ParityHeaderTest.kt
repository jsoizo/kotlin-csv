package com.jsoizo.kotlincsv.bench.parity

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader as v1csvReader
import com.jsoizo.kotlincsv.csvReader as v2csvReader
import com.jsoizo.kotlincsv.reader.withHeader
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParityHeaderTest {

    @Test
    fun readAllWithHeader_string_v1_v2_parity() {
        val headers = (0 until ParityFixtures.hard.rows[0].size)
            .joinToString(",") { "col$it" }
        val body = ParityFixtures.hard.csvText
        val text = headers + "\r\n" + body

        val v1: List<Map<String, String>> = v1csvReader().readAllWithHeader(text)
        val v2Rows: List<Map<String, String>> =
            v2csvReader().readAll(text).asSequence().withHeader().toList()

        assertEquals(v1.size, v2Rows.size, "row count mismatch")
        for (i in v1.indices) {
            val e = v1[i]
            val a = v2Rows[i]
            assertEquals(e.keys.toList(), a.keys.toList(), "row $i header keys mismatch")
            for (k in e.keys) {
                assertEquals(e[k], a[k], "row $i key '$k' mismatch")
            }
        }
    }
}
