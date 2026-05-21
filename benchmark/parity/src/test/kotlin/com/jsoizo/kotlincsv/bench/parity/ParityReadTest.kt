package com.jsoizo.kotlincsv.bench.parity

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader as v1csvReader
import com.jsoizo.kotlincsv.csvReader as v2csvReader
import com.jsoizo.kotlincsv.reader.readAll as v2readAll
import com.jsoizo.kotlincsv.reader.readAllFromFile as v2readAllFromFile
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayInputStream
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParityReadTest {

    private lateinit var tempFile: File

    @BeforeAll
    fun setUp() {
        tempFile = File.createTempFile("parity-hard-", ".csv").apply {
            writeBytes(ParityFixtures.hard.csvBytes)
        }
    }

    @AfterAll
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun readAll_string_v1_v2_parity() {
        val v1 = v1csvReader().readAll(ParityFixtures.hard.csvText)
        val v2 = v2csvReader().readAll(ParityFixtures.hard.csvText)
        assertRowsEqual(v1, v2)
        assertRowsEqual(ParityFixtures.hard.rows, v2)
    }

    @Test
    fun readAll_inputStream_v1_v2_parity() {
        val v1 = ByteArrayInputStream(ParityFixtures.hard.csvBytes).use {
            v1csvReader().readAll(it)
        }
        val v2 = ByteArrayInputStream(ParityFixtures.hard.csvBytes).use {
            v2csvReader().v2readAll(it)
        }
        assertRowsEqual(v1, v2)
        assertRowsEqual(ParityFixtures.hard.rows, v2)
    }

    @Test
    fun readAll_file_v1_v2_parity() {
        val v1 = v1csvReader().readAll(tempFile)
        val v2 = v2csvReader().v2readAllFromFile(tempFile)
        assertRowsEqual(v1, v2)
        assertRowsEqual(ParityFixtures.hard.rows, v2)
    }
}
