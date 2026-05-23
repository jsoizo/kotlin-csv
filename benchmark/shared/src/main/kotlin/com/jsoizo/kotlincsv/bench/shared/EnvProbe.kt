package com.jsoizo.kotlincsv.bench.shared

object EnvProbe {
    data class Snapshot(
        val jdkVersion: String,
        val jdkVendor: String,
        val osName: String,
        val osVersion: String,
        val osArch: String,
        val availableProcessors: Int,
        val totalMemoryMB: Long,
        val kotlinStdlibVersion: String,
        val benchSide: String,
        val timestampEpochMs: Long,
    ) {
        fun toJson(): String = buildString {
            append("{")
            append("\"jdkVersion\":\"").append(escape(jdkVersion)).append("\",")
            append("\"jdkVendor\":\"").append(escape(jdkVendor)).append("\",")
            append("\"osName\":\"").append(escape(osName)).append("\",")
            append("\"osVersion\":\"").append(escape(osVersion)).append("\",")
            append("\"osArch\":\"").append(escape(osArch)).append("\",")
            append("\"availableProcessors\":").append(availableProcessors).append(",")
            append("\"totalMemoryMB\":").append(totalMemoryMB).append(",")
            append("\"kotlinStdlibVersion\":\"").append(escape(kotlinStdlibVersion)).append("\",")
            append("\"benchSide\":\"").append(escape(benchSide)).append("\",")
            append("\"timestampEpochMs\":").append(timestampEpochMs)
            append("}")
        }

        private fun escape(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")
    }

    fun snapshot(benchSide: String): Snapshot {
        val runtime = Runtime.getRuntime()
        return Snapshot(
            jdkVersion = System.getProperty("java.version") ?: "",
            jdkVendor = System.getProperty("java.vendor") ?: "",
            osName = System.getProperty("os.name") ?: "",
            osVersion = System.getProperty("os.version") ?: "",
            osArch = System.getProperty("os.arch") ?: "",
            availableProcessors = runtime.availableProcessors(),
            totalMemoryMB = runtime.totalMemory() / (1024L * 1024L),
            kotlinStdlibVersion = KotlinVersion.CURRENT.toString(),
            benchSide = benchSide,
            timestampEpochMs = System.currentTimeMillis(),
        )
    }
}
