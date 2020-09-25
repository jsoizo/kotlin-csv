<h1 align="center">Welcome to kotlin-csv 👋</h1>
<p>
  <img alt="Version" src="https://img.shields.io/badge/version-0.11.1-blue.svg?cacheSeconds=2592000" />
  <a href="https://github.com/doyaaaaaken/kotlin-csv/blob/master/LICENSE">
    <img alt="License: Apache License 2.0" src="https://img.shields.io/badge/License-Apache License 2.0-yellow.svg" target="_blank" />
  </a>
  <img alt="CircleCI" src="https://circleci.com/gh/doyaaaaaken/kotlin-csv/tree/master.svg?style=svg" />
  <a href="https://codecov.io/gh/doyaaaaaken/kotlin-csv">
    <img src="https://codecov.io/gh/doyaaaaaken/kotlin-csv/branch/master/graph/badge.svg" alt="codecov" />
  </a>
  <a href="https://www.codefactor.io/repository/github/doyaaaaaken/kotlin-csv">
    <img src="https://www.codefactor.io/repository/github/doyaaaaaken/kotlin-csv/badge" alt="CodeFactor" />
  </a>
</p>

> Pure Kotlin CSV Reader/Writer

# Principals

### 1. Simple interface
  * easy to setup
  * use DSL so easy to read

### 2. No need to be aware of file close
  * on Java, we always need to close file. but it's boilerplate code and not friendly for non-JVM user.
  * provide interfaces which automatically close file without being aware.

### 3. Multiplatform (Planned in #15)
  * kotlin multiplatform project

# Usage

## Download

gradle DSL:
```
//gradle kotlin DSL
implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.11.1")

//gradle groovy DSL
implementation 'com.github.doyaaaaaken:kotlin-csv-jvm:0.11.1'
```

maven:
```
<dependency>
  <groupId>com.github.doyaaaaaken</groupId>
  <artifactId>kotlin-csv-jvm</artifactId>
  <version>0.11.1</version>
</dependency>
```

## Examples

### CSV Read examples

#### Simple case

You can read csv file from `String`, `java.io.File` or `java.io.InputStream` object.
```kotlin
// read from `String`
val csvData: String = "a,b,c\nd,e,f"
val rows: List<List<String>> = csvReader().readAll(csvData)

// read from `java.io.File`
val file: File = File("test.csv")
val rows: List<List<String>> = csvReader().readAll(file)
```

#### Read with header

```kotlin
val csvData: String = "a,b,c\nd,e,f"
val rows: List<Map<String, String>> = csvReader().readAllWithHeader(csvData)
println(rows) //[{a=d, b=e, c=f}]
```

#### Read as `Sequence`

`Sequence` type allows to execute lazily.<br />
It starts to process each rows before reading all row data.

See detail about `Sequence` type on [Kotlin official document](https://kotlinlang.org/docs/reference/sequences.html).
 
```kotlin
csvReader().open("test1.csv") {
    readAllAsSequence().forEach { row: List<String> ->
        //Do something
        println(row) //[a, b, c]
    }
}

csvReader().open("test2.csv") {
    readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
        //Do something
        println(row) //{id=1, name=doyaaaaaken}
    }
}
```

NOTE:`readAllAsSequence` and `readAllWithHeaderAsSequence` methods can be only called inside `open` method lambda block.
Because, input stream is closed outside `open` method lambda block.

#### Read line by line

If you want to handle line-by-line, you can do it by using `open` method.<br />
Use `open` method and then use `readNext` method inside nested block to read row.

```kotlin
csvReader().open("test.csv") {
    readNext()
}
```

#### Customize

When you create CsvReader, you can choose read options.
```kotlin
// this is tsv reader's option
val tsvReader = csvReader {
    charset = "ISO_8859_1"
    quoteChar = '"'
    delimiter = '\t'
    escapeChar = '\\'
}
```

| Opton | default value | description                         |
|------------|---------------|-------------------------------------|
| charset |`UTF-8`| Charset encoding. The value must be supported by [java.nio.charset.Charset](https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html). |
| quoteChar | `"` | Character used as quote between each fields. |
| delimiter | `,` | Character used as delimiter between each fields.<br />Use `"\t"` if reading TSV file. |
| escapeChar | `"` | Character to escape quote inside field string.<br />Normally, you don't have to change this option.<br />See detail comment on `ICsvReaderContext`. |
| skipEmptyLine | `false` | If empty line is found, skip it or not (=throw an exception). |
| skipMissMatchedRow | `false` | If a invalid row which has different number of fields from other rows is found, skip it or not (=throw an exception). |

### CSV Write examples

#### Simple case

You can write csv simply, only one line.
No need to call other methods. <br />
Also, **You don't have to call `use`, `close` and `flush` method.**
```kotlin
val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
csvWriter().writeAll(rows, "test.csv")

// if you'd append data on the tail of the file, assign `append = true`.
csvWriter().writeAll(rows, "test.csv", append = true)
```

You can also write csv file per each line.<br />
Also, **You don't have to call `use`, `close` and `flush` method.**
```kotlin
val row1 = listOf("a", "b", "c")
val row2 = listOf("d", "e", "f")
csvWriter().open("test.csv") { 
    writeRow(row1)
    writeRow(row2)
    writeRow("g", "h", "i")
    writeRows(listOf(row1, row2))
}
```

#### long-running write (manual control for file close)

If you want to close file writer manually for performance reason (i.e. streaming scenario), you can use `openAndGetRawWriter` and get raw `CsvFileWriter`.  
**DO NOT forget to call `close` method manually.**

```kotlin
val row1 = listOf("a", "b", "c")
@OptIn(KotlinCsvExperimental::class)
val writer = csvWriter().openAndGetRawWriter("test.csv") 
writer.writeRow(row1)
writer.close()
```

#### Customize

When you create CsvWriter, you can choose write options.
```kotlin
val writer = csvWriter {
    charset = "ISO_8859_1"
    delimiter = '\t'
    nullCode = "NULL"
    lineTerminator = "\n"
    outputLastLineTerminator = true
    quote {
        mode = WriteQuoteMode.ALL
        char = '\''
    }
}
```

| Option | default value | description                         |
|------------|---------------|-------------------------------------|
| charset |`UTF-8`| Charset encoding. The value must be supported by [java.nio.charset.Charset](https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html). |
| delimiter | `,` | Character used as delimiter between each fields.<br />Use `"\t"` if reading TSV file. |
| nullCode | `(empty string)` | Character used when a written field is null value. |
| lineTerminator | `\r\n` | Character used as line terminator. |
| outputLastLineTerminator | `true` | Output line break at the end of file or not. |
| quote.char  | `"` | Character to quote each fields. |
| quote.mode  | `CANONICAL` | Quote mode. <br />- `CANONICAL`: Not quote normally, but quote special characters (quoteChar, delimiter, line feed). This is [the specification of CSV](https://tools.ietf.org/html/rfc4180#section-2).<br />- `ALL`: Quote all fields. |

# Links

**Libraries**
* [kotlin-grass](https://github.com/blackmo18/kotlin-grass): Csv File to Kotlin Data Class Parser.

# Miscellaneous

## 🤝 Contributing

Contributions, issues and feature requests are welcome!<br />Feel free to check [issues page](https://github.com/doyaaaaaken/kotlin-csv/issues).<br />
If you have question, feel free to ask in [Kotlin slack's](https://kotlinlang.slack.com/) `kotlin-csv` room.

## 💻 Development

```
$ git clone git@github.com:doyaaaaaken/kotlin-csv.git
$ cd kotlin-csv
$ ./gradlew check
```

## Show your support

Give a ⭐️ if this project helped you!

## 📝 License

Copyright © 2019 [doyaaaaaken](https://github.com/doyaaaaaken).<br />
This project is [Apache License 2.0](https://github.com/doyaaaaaken/kotlin-csv/blob/master/LICENSE) licensed.

***
_This project is inspired ❤️ by [scala-csv](https://github.com/tototoshi/scala-csv)_

_This README was generated with ❤️ by [readme-md-generator](https://github.com/kefranabg/readme-md-generator)_
