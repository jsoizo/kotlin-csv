package com.jsoizo.kotlincsv.bench.v2

import com.jsoizo.kotlincsv.bench.v2.state.FileOutputSinkStateV2
import com.jsoizo.kotlincsv.bench.v2.state.GeneratedDataStateLargeV2
import com.jsoizo.kotlincsv.bench.v2.state.GeneratedDataStateV2
import com.jsoizo.kotlincsv.bench.v2.state.NullOutputSinkStateV2
import com.jsoizo.kotlincsv.csvWriter
import com.jsoizo.kotlincsv.writer.write
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
open class WriteBenchmarksV2 {

    @Benchmark
    fun writeAllOutputStream(data: GeneratedDataStateV2, sink: NullOutputSinkStateV2) {
        csvWriter().write(data.rows.asSequence(), sink.sink, "UTF-8")
    }

    @Benchmark
    fun writeAllFile(data: GeneratedDataStateLargeV2, sink: FileOutputSinkStateV2) {
        csvWriter().writeToFile(data.rows.asSequence(), sink.file, "UTF-8")
    }
}
