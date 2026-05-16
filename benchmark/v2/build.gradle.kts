plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jmh)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    jmh(project(":"))
    jmh(project(":benchmark:shared"))
    jmh(libs.kotlinx.io.core)
}

jmh {
    warmupIterations.set(5)
    iterations.set(5)
    fork.set(2)
    timeOnIteration.set("10s")
    warmup.set("10s")
    benchmarkMode.set(listOf("thrpt", "avgt"))
    resultFormat.set("JSON")
    jvmArgs.set(listOf("-Xms2g", "-Xmx2g"))
    duplicateClassesStrategy.set(DuplicatesStrategy.EXCLUDE)

    val include = (project.findProperty("jmh.include") as String?)?.let { listOf(it) }
    if (include != null) includes.set(include)
    (project.findProperty("jmh.warmupIterations") as String?)?.toInt()?.let { warmupIterations.set(it) }
    (project.findProperty("jmh.iterations") as String?)?.toInt()?.let { iterations.set(it) }
    (project.findProperty("jmh.fork") as String?)?.toInt()?.let { fork.set(it) }

    when (project.findProperty("bench.profile") as String?) {
        "quick" -> {
            warmupIterations.set(3)
            iterations.set(3)
            fork.set(1)
            timeOnIteration.set("5s")
            warmup.set("5s")
            benchmarkMode.set(listOf("thrpt", "avgt"))
            benchmarkParameters.put("dataset", listOf("SMALL", "HARD"))
        }
        "large" -> {
            warmupIterations.set(2)
            iterations.set(3)
            fork.set(1)
            benchmarkMode.set(listOf("thrpt"))
        }
        "gcprof" -> {
            warmupIterations.set(3)
            iterations.set(3)
            fork.set(2)
            benchmarkMode.set(listOf("thrpt"))
            profilers.set(listOf("gc"))
        }
        "stackprof" -> {
            warmupIterations.set(2)
            iterations.set(2)
            fork.set(1)
            benchmarkMode.set(listOf("thrpt"))
            profilers.set(listOf("stack"))
        }
    }
}
