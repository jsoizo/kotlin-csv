package com.jsoizo.kotlincsv.bench.v1.state

import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import java.io.File
import java.io.OutputStream

@State(Scope.Benchmark)
open class NullOutputSinkStateV1 {
    var sink: OutputStream = OutputStream.nullOutputStream()
        private set

    @Setup(Level.Invocation)
    fun setup() {
        sink = OutputStream.nullOutputStream()
    }
}

@State(Scope.Benchmark)
open class FileOutputSinkStateV1 {
    @Param("SMALL", "MEDIUM", "LARGE", "HARD")
    lateinit var dataset: String

    lateinit var file: File

    @Setup(Level.Trial)
    fun setupTrial() {
        file = File.createTempFile("bench-v1-out-${dataset}-", ".csv")
    }

    @Setup(Level.Invocation)
    fun setupInvocation() {
        if (file.exists()) file.delete()
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        file.delete()
    }
}
