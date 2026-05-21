package com.jsoizo.kotlincsv.bench.v2

import com.jsoizo.kotlincsv.bench.v2.state.FileInputStateLargeV2
import com.jsoizo.kotlincsv.bench.v2.state.FileInputStateV2
import com.jsoizo.kotlincsv.bench.v2.state.GeneratedDataStateV2
import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.reader.readAll
import com.jsoizo.kotlincsv.reader.readAllFromFile
import com.jsoizo.kotlincsv.reader.readFromFile
import com.jsoizo.kotlincsv.reader.withHeader
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
open class ReadBenchmarksV2 {

    @Benchmark
    fun readAllString(data: GeneratedDataStateV2): List<List<String>> =
        csvReader().readAll(data.csvText)

    @Benchmark
    fun readAllInputStream(data: GeneratedDataStateV2): List<List<String>> =
        ByteArrayInputStream(data.csvBytes).use { csvReader().readAll(it, "UTF-8") }

    @Benchmark
    fun readAllFile(state: FileInputStateV2): List<List<String>> =
        csvReader().readAllFromFile(state.file, "UTF-8")

    @Benchmark
    fun sequenceIterativeFile(state: FileInputStateLargeV2): Int =
        csvReader().readFromFile(state.file, "UTF-8") { it.count() }

    @Benchmark
    fun readAllWithHeader(data: GeneratedDataStateV2): List<Map<String, String>> {
        val headers = (0 until data.rows[0].size).joinToString(",") { "col$it" }
        val text = headers + "\r\n" + data.csvText
        return csvReader().readAll(text).asSequence().withHeader().toList()
    }
}
