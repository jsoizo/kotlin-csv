package com.jsoizo.kotlincsv.bench.v2.state

import com.jsoizo.kotlincsv.bench.shared.CsvDataGen
import com.jsoizo.kotlincsv.bench.shared.DatasetSpec
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
open class GeneratedDataStateV2 {
    @Param("SMALL", "MEDIUM", "HARD")
    lateinit var dataset: String

    lateinit var rows: List<List<String>>
    lateinit var csvText: String
    lateinit var csvBytes: ByteArray

    @Setup(Level.Trial)
    fun setup() {
        val spec = DatasetSpec.valueOf(dataset)
        val gen = CsvDataGen.generate(spec)
        rows = gen.rows
        csvText = gen.csvText
        csvBytes = gen.csvBytes
    }
}

@State(Scope.Benchmark)
open class GeneratedDataStateLargeV2 {
    @Param("SMALL", "MEDIUM", "LARGE", "HARD")
    lateinit var dataset: String

    lateinit var rows: List<List<String>>
    lateinit var csvBytes: ByteArray

    @Setup(Level.Trial)
    fun setup() {
        val spec = DatasetSpec.valueOf(dataset)
        val gen = CsvDataGen.generate(spec)
        rows = gen.rows
        csvBytes = gen.csvBytes
    }
}
