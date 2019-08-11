package org.doyaaaaaken.kotlincsv.parser

class CsvParser {

    fun parseLine(line: String): List<String>? {
        return line.split(",")
    }
}
