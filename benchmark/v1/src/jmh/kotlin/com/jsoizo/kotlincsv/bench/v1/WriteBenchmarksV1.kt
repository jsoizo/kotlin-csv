package com.jsoizo.kotlincsv.bench.v1

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.jsoizo.kotlincsv.bench.v1.state.FileOutputSinkStateV1
import com.jsoizo.kotlincsv.bench.v1.state.GeneratedDataStateLargeV1
import com.jsoizo.kotlincsv.bench.v1.state.GeneratedDataStateV1
import com.jsoizo.kotlincsv.bench.v1.state.NullOutputSinkStateV1
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
open class WriteBenchmarksV1 {

    @Benchmark
    fun writeAllOutputStream(data: GeneratedDataStateV1, sink: NullOutputSinkStateV1) {
        csvWriter().writeAll(data.rows, sink.sink)
    }

    @Benchmark
    fun writeAllFile(data: GeneratedDataStateLargeV1, sink: FileOutputSinkStateV1) {
        csvWriter().writeAll(data.rows, sink.file)
    }
}
