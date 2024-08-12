package com.jsoizo.kotlincsv.client

import com.jsoizo.kotlincsv.dsl.context.CsvWriterContext
import com.jsoizo.kotlincsv.dsl.context.WriteQuoteMode
import com.jsoizo.kotlincsv.dsl.csvWriter
import com.jsoizo.kotlincsv.util.Const
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.charset.Charset


class CsvWriterTest : WordSpec({

    val testFileName = "test.csv"

    afterTest { File(testFileName).delete() }

    fun readTestFile(charset: Charset = Charsets.UTF_8): String {
        return File(testFileName).readText(charset)
    }

    "CsvWriter class constructor" should {
        "be created with no argument" {
            val writer = CsvWriter()
            writer.charset shouldBe Const.defaultCharset
        }
        "be created with CsvWriterContext argument" {
            val context = CsvWriterContext().apply {
                charset = Charsets.ISO_8859_1.name()
                delimiter = '\t'
                nullCode = "NULL"
                lineTerminator = "\n"
                outputLastLineTerminator = false
                prependBOM = true
                quote {
                    char = '\''
                    mode = WriteQuoteMode.ALL
                }
            }
            val writer = CsvWriter(context)
            assertSoftly {
                writer.charset shouldBe Charsets.ISO_8859_1.name()
                writer.delimiter shouldBe '\t'
                writer.nullCode shouldBe "NULL"
                writer.lineTerminator shouldBe "\n"
                writer.outputLastLineTerminator shouldBe false
                writer.prependBOM shouldBe true
                writer.quote.char = '\''
                writer.quote.mode = WriteQuoteMode.ALL
            }
        }
    }

    "open method" should {
        val row1 = listOf("a", "b", null)
        val row2 = listOf("d", "2", "1.0")
        val expected = "a,b,\r\nd,2,1.0\r\n"

        "write simple csv data into file with writing each rows" {
            csvWriter().open(testFileName) {
                writeRow(row1)
                writeRow(row2)
            }
            val actual = readTestFile()
            actual shouldBe expected
        }

        "write simple csv data into file with writing all at one time" {
            csvWriter().open(testFileName) { writeRows(listOf(row1, row2)) }
            val actual = readTestFile()
            actual shouldBe expected
        }

        "write simple csv data to the tail of existing file with append = true" {
            val writer = csvWriter()
            writer.open(File(testFileName), true) {
                writeRows(listOf(row1, row2))
            }
            writer.open(File(testFileName), true) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected + expected
        }

        "overwrite simple csv data with append = false" {
            val writer = csvWriter()
            writer.open(File(testFileName), false) {
                writeRows(listOf(row2, row2, row2))
            }
            writer.open(File(testFileName), false) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
    }

    "writeAsString method" should {
        val row1 = listOf("a", "b", null)
        val row2 = listOf("d", "2", "1.0")
        val expected = "a,b,\r\nd,2,1.0\r\n"

        "write simple csv data to String" {
            val actual = csvWriter().writeAsString {
                writeRow(row1)
                writeRow(row2)
            }
            actual shouldBe expected
        }
    }

    "writeAll method without calling `open` method" should {
        val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
        val expected = "a,b,c\r\nd,e,f\r\n"

        "write data with target file name" {
            csvWriter().writeAll(rows, testFileName)
            val actual = readTestFile()
            actual shouldBe expected
        }

        "write data with target file (java.io.File)" {
            csvWriter().writeAll(rows, File(testFileName))
            val actual = readTestFile()
            actual shouldBe expected
        }

        "write data with target output stream (java.io.OutputStream)" {
            csvWriter().writeAll(rows, File(testFileName).outputStream())
            val actual = readTestFile()
            actual shouldBe expected
        }

        "write data to String" {
            val actual = csvWriter().writeAllAsString(rows)
            actual shouldBe expected
        }
    }

    "Customized CsvWriter" should {
        "write csv with SJIS charset" {
            csvWriter {
                charset = "SJIS"
            }.open(File(testFileName)) {
                writeRows(listOf(listOf("あ", "い")))
            }
            val actual = readTestFile(Charset.forName("SJIS"))
            actual shouldBe "あ,い\r\n"
        }
        "write csv with '|' delimiter" {
            val row1 = listOf("a", "b")
            val row2 = listOf("c", "d")
            val expected = "a|b\r\nc|d\r\n"
            csvWriter {
                delimiter = '|'
            }.open(File(testFileName)) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write null with customized null code" {
            val row = listOf(null, null)
            csvWriter {
                nullCode = "NULL"
            }.open(testFileName) {
                writeRow(row)
            }
            val actual = readTestFile()
            actual shouldBe "NULL,NULL\r\n"
        }
        "write csv with \n line terminator" {
            val row1 = listOf("a", "b")
            val row2 = listOf("c", "d")
            val expected = "a,b\nc,d\n"
            csvWriter {
                lineTerminator = "\n"
            }.open(File(testFileName)) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write csv with WriteQuoteMode.ALL mode" {
            val row1 = listOf("a", "b")
            val row2 = listOf("c", "d")
            val expected = "\"a\",\"b\"\r\n\"c\",\"d\"\r\n"
            csvWriter {
                quote {
                    mode = WriteQuoteMode.ALL
                }
            }.open(File(testFileName)) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write csv with WriteQuoteMode.NON_NUMERIC mode" {
            val row1 = listOf("a", "b", 1)
            val row2 = listOf(2.0, "03.0", "4.0.0")
            val expected = "\"a\",\"b\",1\r\n2.0,03.0,\"4.0.0\"\r\n"
            csvWriter {
                quote {
                    mode = WriteQuoteMode.NON_NUMERIC
                }
            }.open(File(testFileName)) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write csv with custom quote character" {
            val row1 = listOf("a'", "b")
            val row2 = listOf("'c", "d")
            val expected = "'a''',b\r\n'''c',d\r\n"
            csvWriter {
                quote {
                    char = '\''
                }
            }.open(File(testFileName)) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write csv with custom quote character on WriteQuoteMode.ALL mode" {
            val rows = listOf(listOf("a1", "b1"), listOf("a2", "b2"))
            val expected = "_a1_,_b1_\r\n_a2_,_b2_\r\n"
            csvWriter {
                quote {
                    mode = WriteQuoteMode.ALL
                    char = '_'
                }
            }.writeAll(rows, testFileName)
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write simple csv with disabled last line terminator with custom terminator" {
            val row1 = listOf("a", "b")
            val row2 = listOf("c", "d")
            val expected = "a,b\nc,d"
            csvWriter {
                lineTerminator = "\n"
                outputLastLineTerminator = false
            }.open(File(testFileName)) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write simple csv with enabled last line and custom terminator" {
            val row1 = listOf("a", "b")
            val row2 = listOf("c", "d")
            val expected = "a,b\nc,d\n"
            csvWriter {
                lineTerminator = "\n"
                outputLastLineTerminator = true
            }.open(File(testFileName)) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write simple csv with disabled last line terminator" {
            val row1 = listOf("a", "b")
            val row2 = listOf("c", "d")
            val expected = "a,b\r\nc,d"
            csvWriter {
                outputLastLineTerminator = false
            }.open(File(testFileName)) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write simple csv with prepending BOM" {
            val row1 = listOf("a", "b")
            val row2 = listOf("c", "d")
            val expected = "\uFEFFa,b\r\nc,d\r\n"
            csvWriter {
                prependBOM = true
            }.open(File(testFileName)) {
                writeRows(listOf(row1, row2))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write simple csv with disabled last line terminator multiple writes" {
            val row1 = listOf("a", "b")
            val row2 = listOf("c", "d")
            val row3 = listOf("e", "f")
            val row4 = listOf("g", "h")
            val row5 = listOf("1", "2")
            val row6 = listOf("3", "4")
            val expected = "a,b\r\nc,d\r\ne,f\r\ng,h\r\n1,2\r\n3,4"
            csvWriter {
                outputLastLineTerminator = false
            }.open(File(testFileName)) {
                writeRow(row1)
                writeRows(listOf(row2, row3))
                writeRow(row4)
                writeRows(listOf(row5, row6))
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
    }

    "openAndGetRawWriter method" should {
        val row1 = listOf("a", "b", null)
        val row2 = listOf("d", "2", "1.0")
        val expected = "a,b,\r\nd,2,1.0\r\n"

        "get raw writer from fileName string and can use it" {
            @OptIn(KotlinCsvExperimental::class)
            val writer = csvWriter().openAndGetRawWriter(testFileName)
            writer.writeRow(row1)
            writer.writeRow(row2)
            writer.close()

            val actual = readTestFile()
            actual shouldBe expected
        }

        "get raw writer from java.io.File and can use it" {
            @OptIn(KotlinCsvExperimental::class)
            val writer = csvWriter().openAndGetRawWriter(File(testFileName))
            writer.writeRow(row1)
            writer.writeRow(row2)
            writer.close()

            val actual = readTestFile()
            actual shouldBe expected
        }

        "get raw writer from OutputStream and can use it" {
            val ops = File(testFileName).outputStream()

            @OptIn(KotlinCsvExperimental::class)
            val writer = csvWriter().openAndGetRawWriter(ops)
            writer.writeRow(row1)
            writer.writeRow(row2)
            writer.close()

            val actual = readTestFile()
            actual shouldBe expected
        }
    }
})
