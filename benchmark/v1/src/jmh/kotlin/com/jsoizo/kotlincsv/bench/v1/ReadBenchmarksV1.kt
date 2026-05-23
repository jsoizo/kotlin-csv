package com.jsoizo.kotlincsv.bench.v1

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.jsoizo.kotlincsv.bench.v1.state.FileInputStateLargeV1
import com.jsoizo.kotlincsv.bench.v1.state.FileInputStateV1
import com.jsoizo.kotlincsv.bench.v1.state.GeneratedDataStateV1
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput, Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class ReadBenchmarksV1 {

    @Benchmark
    fun readAllString(data: GeneratedDataStateV1): List<List<String>> =
        csvReader().readAll(data.csvText)

    @Benchmark
    fun readAllInputStream(data: GeneratedDataStateV1): List<List<String>> =
        ByteArrayInputStream(data.csvBytes).use { csvReader().readAll(it) }

    @Benchmark
    fun readAllFile(state: FileInputStateV1): List<List<String>> =
        csvReader().readAll(state.file)

    @Benchmark
    fun sequenceIterativeFile(state: FileInputStateLargeV1): Int =
        csvReader().open(state.file) { readAllAsSequence().count() }

    @Benchmark
    fun readAllWithHeader(data: GeneratedDataStateV1): List<Map<String, String>> {
        val headers = (0 until data.rows[0].size).joinToString(",") { "col$it" }
        val text = headers + "\r\n" + data.csvText
        return csvReader().readAllWithHeader(text)
    }
}
