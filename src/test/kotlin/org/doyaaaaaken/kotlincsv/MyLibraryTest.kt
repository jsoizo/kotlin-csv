package org.doyaaaaaken.kotlincsv

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class MyLibraryTest : WordSpec({
    "testSomeLibraryMethod" should {
        "return true" {
            val classUnderTest = MyLibrary()
            classUnderTest.someLibraryMethod() shouldBe true
        }
    }
})
