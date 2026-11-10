package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.CsvDialect
import io.kotest.matchers.shouldBe
import kotlin.test.Test

// Binaries compiled against 2.0.0 call these signatures; Kotlin source cannot, so reflection stands in for them
class CsvReaderConfigBinaryCompatTest {

    private val v200Params = arrayOf(
        CsvDialect::class.java,
        Boolean::class.javaPrimitiveType,
        InsufficientFieldsRowBehaviour::class.java,
        ExcessFieldsRowBehaviour::class.java,
    )
    private val allDefaultsMask = 0b1111

    @Test
    fun constructor_v200Signature_forwardsNeitherIndicator() {
        val config = CsvReaderConfig::class.java.getConstructor(*v200Params)
            .newInstance(CsvDialect.TSV, true, InsufficientFieldsRowBehaviour.EMPTY_STRING, ExcessFieldsRowBehaviour.TRIM)

        config shouldBe CsvReaderConfig(
            dialect = CsvDialect.TSV,
            skipEmptyLine = true,
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING,
            excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM,
            nullFieldIndicator = CsvNullFieldIndicator.NEITHER,
        )
    }

    @Test
    fun constructor_v200DefaultsSignature_matchesNoArgConfig() {
        val marker = Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
        val config = CsvReaderConfig::class.java
            .getConstructor(*v200Params, Int::class.javaPrimitiveType, marker)
            .newInstance(null, false, null, null, allDefaultsMask, null)

        config shouldBe CsvReaderConfig()
    }

    @Test
    fun copy_v200Signature_keepsNullFieldIndicator() {
        val base = CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.BOTH)
        val copied = CsvReaderConfig::class.java.getMethod("copy", *v200Params)
            .invoke(base, CsvDialect.TSV, true, InsufficientFieldsRowBehaviour.IGNORE, ExcessFieldsRowBehaviour.IGNORE)

        copied shouldBe CsvReaderConfig(
            dialect = CsvDialect.TSV,
            skipEmptyLine = true,
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE,
            excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.IGNORE,
            nullFieldIndicator = CsvNullFieldIndicator.BOTH,
        )
    }

    @Test
    fun copy_v200DefaultsSignature_keepsEveryField() {
        val base = CsvReaderConfig(
            dialect = CsvDialect.TSV,
            skipEmptyLine = true,
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING,
            excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM,
            nullFieldIndicator = CsvNullFieldIndicator.BOTH,
        )
        val copied = CsvReaderConfig::class.java
            .getMethod("copy\$default", CsvReaderConfig::class.java, *v200Params, Int::class.javaPrimitiveType, Any::class.java)
            .invoke(null, base, null, false, null, null, allDefaultsMask, null)

        copied shouldBe base
    }
}
