package com.jsoizo.kotlincsv.bench.v2

import com.jsoizo.kotlincsv.bench.v2.state.FileInputStateLargeV2
import com.jsoizo.kotlincsv.bench.v2.state.FileInputStateV2
import com.jsoizo.kotlincsv.bench.v2.state.FileOutputSinkStateV2
import com.jsoizo.kotlincsv.bench.v2.state.GeneratedDataStateLargeV2
import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.csvWriter
import com.jsoizo.kotlincsv.reader.readAllFromFile
import com.jsoizo.kotlincsv.reader.readFromFile
import com.jsoizo.kotlincsv.writer.writeToFile
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput, Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class V2BackendBenchmarks {

    @Benchmark
    fun readAllFile_javaIo(state: FileInputStateV2): List<List<String>> =
        csvReader().readAllFromFile(state.file, "UTF-8")

    @Benchmark
    fun readAllFile_kotlinxIo(state: FileInputStateV2): List<List<String>> =
        csvReader().readAllFromFile(state.path)

    @Benchmark
    fun sequenceIterativeFile_javaIo(state: FileInputStateLargeV2): Int =
        csvReader().readFromFile(state.file, "UTF-8") { it.count() }

    @Benchmark
    fun sequenceIterativeFile_kotlinxIo(state: FileInputStateLargeV2): Int =
        csvReader().readFromFile(state.path) { it.count() }

    @Benchmark
    fun writeAllFile_javaIo(data: GeneratedDataStateLargeV2, sink: FileOutputSinkStateV2) {
        csvWriter().writeToFile(data.rows.asSequence(), sink.file, "UTF-8")
    }

    @Benchmark
    fun writeAllFile_kotlinxIo(data: GeneratedDataStateLargeV2, sink: FileOutputSinkStateV2) {
        csvWriter().writeToFile(data.rows.asSequence(), sink.path)
    }
}
