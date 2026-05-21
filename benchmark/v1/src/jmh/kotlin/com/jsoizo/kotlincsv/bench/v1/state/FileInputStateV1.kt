package com.jsoizo.kotlincsv.bench.v1.state

import com.jsoizo.kotlincsv.bench.shared.CsvDataGen
import com.jsoizo.kotlincsv.bench.shared.DatasetSpec
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import java.io.File

@State(Scope.Benchmark)
open class FileInputStateV1 {
    @Param("SMALL", "MEDIUM", "HARD")
    lateinit var dataset: String

    lateinit var file: File

    @Setup(Level.Trial)
    fun setup() {
        val spec = DatasetSpec.valueOf(dataset)
        val gen = CsvDataGen.generate(spec)
        file = File.createTempFile("bench-v1-${dataset}-", ".csv").apply {
            writeBytes(gen.csvBytes)
        }
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        file.delete()
    }
}

@State(Scope.Benchmark)
open class FileInputStateLargeV1 {
    @Param("SMALL", "MEDIUM", "LARGE", "HARD")
    lateinit var dataset: String

    lateinit var file: File

    @Setup(Level.Trial)
    fun setup() {
        val spec = DatasetSpec.valueOf(dataset)
        val gen = CsvDataGen.generate(spec)
        file = File.createTempFile("bench-v1-large-${dataset}-", ".csv").apply {
            writeBytes(gen.csvBytes)
        }
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        file.delete()
    }
}
