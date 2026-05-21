package com.jsoizo.kotlincsv.bench.parity

import kotlin.test.assertEquals

fun assertRowsEqual(expected: List<List<String>>, actual: List<List<String>>) {
    assertEquals(expected.size, actual.size, "row count mismatch")
    for (i in expected.indices) {
        val e = expected[i]
        val a = actual[i]
        if (e.size != a.size) {
            throw AssertionError("row $i col count mismatch: expected=${e.size} actual=${a.size}\n  expected=$e\n  actual=$a")
        }
        for (j in e.indices) {
            if (e[j] != a[j]) {
                throw AssertionError("row $i col $j mismatch: expected=${e[j].quoted()} actual=${a[j].quoted()}")
            }
        }
    }
}

private fun String.quoted(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
