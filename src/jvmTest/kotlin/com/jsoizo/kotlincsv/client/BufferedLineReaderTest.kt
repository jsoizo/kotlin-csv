package com.jsoizo.kotlincsv.client

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BufferedLineReaderTest : StringSpec({
    "regard \\n as line terminator" {
        val str = "a,b,c\nd,e,f"
        val br = str.byteInputStream().bufferedReader()
        val blr = BufferedLineReader(br)
        assertSoftly {
            blr.readLineWithTerminator() shouldBe "a,b,c\n"
            blr.readLineWithTerminator() shouldBe "d,e,f"
            blr.readLineWithTerminator() shouldBe null
        }
    }

    "regard \\r\\n as line terminator" {
        val str = "a,b,c\r\nd,e,f"
        val br = str.byteInputStream().bufferedReader()
        val blr = BufferedLineReader(br)
        assertSoftly {
            blr.readLineWithTerminator() shouldBe "a,b,c\r\n"
            blr.readLineWithTerminator() shouldBe "d,e,f"
            blr.readLineWithTerminator() shouldBe null
        }
    }

    "regard \\r as line terminator" {
        val str = "a,b,c\rd,e,f"
        val br = str.byteInputStream().bufferedReader()
        val blr = BufferedLineReader(br)
        assertSoftly {
            blr.readLineWithTerminator() shouldBe "a,b,c\r"
            blr.readLineWithTerminator() shouldBe "d,e,f"
            blr.readLineWithTerminator() shouldBe null
        }
    }

    "regard \\u2028 as line terminator" {
        val str = "a,b,c\u2028d,e,f"
        val br = str.byteInputStream().bufferedReader()
        val blr = BufferedLineReader(br)
        assertSoftly {
            blr.readLineWithTerminator() shouldBe "a,b,c\u2028"
            blr.readLineWithTerminator() shouldBe "d,e,f"
            blr.readLineWithTerminator() shouldBe null
        }
    }

    "regard \\u2029 as line terminator" {
        val str = "a,b,c\u2029d,e,f"
        val br = str.byteInputStream().bufferedReader()
        val blr = BufferedLineReader(br)
        assertSoftly {
            blr.readLineWithTerminator() shouldBe "a,b,c\u2029"
            blr.readLineWithTerminator() shouldBe "d,e,f"
            blr.readLineWithTerminator() shouldBe null
        }
    }

    "regard \\u0085 as line terminator" {
        val str = "a,b,c\u0085d,e,f"
        val br = str.byteInputStream().bufferedReader()
        val blr = BufferedLineReader(br)
        assertSoftly {
            blr.readLineWithTerminator() shouldBe "a,b,c\u0085"
            blr.readLineWithTerminator() shouldBe "d,e,f"
            blr.readLineWithTerminator() shouldBe null
        }
    }

    "deal with \\r at the end of file" {
        val str = "a,b,c\r"
        val br = str.byteInputStream().bufferedReader()
        val blr = BufferedLineReader(br)
        assertSoftly {
            blr.readLineWithTerminator() shouldBe "a,b,c\r"
            blr.readLineWithTerminator() shouldBe null
        }
    }
})
