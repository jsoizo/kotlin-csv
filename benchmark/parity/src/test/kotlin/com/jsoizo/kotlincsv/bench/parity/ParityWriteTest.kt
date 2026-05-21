package com.jsoizo.kotlincsv.bench.parity

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader as v1csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter as v1csvWriter
import com.jsoizo.kotlincsv.csvReader as v2csvReader
import com.jsoizo.kotlincsv.csvWriter as v2csvWriter
import com.jsoizo.kotlincsv.writer.write as v2write
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class ParityWriteTest {

    @Test
    fun writeAll_outputStream_then_v1_reparse_matches_input() {
        val rows = ParityFixtures.hard.rows

        val v1Out = ByteArrayOutputStream()
        v1csvWriter().writeAll(rows, v1Out)
        val v1Reparsed = v1csvReader().readAll(v1Out.toString(Charsets.UTF_8))

        val v2Out = ByteArrayOutputStream()
        v2csvWriter().v2write(rows.asSequence(), v2Out, "UTF-8")
        val v2Reparsed = v2csvReader().readAll(v2Out.toString(Charsets.UTF_8))

        assertRowsEqual(rows, v1Reparsed)
        assertRowsEqual(rows, v2Reparsed)
        assertRowsEqual(v1Reparsed, v2Reparsed)
    }
}
