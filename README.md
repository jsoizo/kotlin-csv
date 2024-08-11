<h1 align="center">kotlin-csv</h1>

<p>
  <img alt="Version" src="https://img.shields.io/badge/version-1.10.0-blue.svg?cacheSeconds=2592000" />
  <a href="https://github.com/jsoizo/kotlin-csv/blob/master/LICENSE">
    <img alt="License: Apache License 2.0" src="https://img.shields.io/badge/License-Apache License 2.0-yellow.svg" target="_blank" />
  </a>
  <a href="https://codecov.io/gh/jsoizo/kotlin-csv">
    <img src="https://codecov.io/gh/jsoizo/kotlin-csv/branch/master/graph/badge.svg" alt="codecov" />
  </a>
  <a href="https://www.codefactor.io/repository/github/jsoizo/kotlin-csv">
    <img src="https://www.codefactor.io/repository/github/jsoizo/kotlin-csv/badge" alt="CodeFactor" />
  </a>
</p>

Pure Kotlin CSV Reader/Writer.

# Design goals

### 1. Simple interface

* easy to setup
* use DSL so easy to read

### 2. Automatic handling of I/O

* in Java, we always need to close file. but it's boilerplate code and not friendly for non-JVM user.
* provide interfaces which automatically close file without being aware.

### 3. Multiplatform

* Kotlin Multiplatform projects support.

# Usage

## Download

### Gradle

for Kotlin DSL

```kotlin
implementation("com.jsoizo:kotlin-csv-jvm:1.10.0") // for JVM platform
implementation("com.jsoizo:kotlin-csv-js:1.10.0") // for Kotlin JS platform
```

for Gradle DSL

```groovy
implementation 'com.jsoizo:kotlin-csv-jvm:1.10.0' // for JVM platform
implementation 'com.jsoizo:kotlin-csv-js:1.10.0' // for Kotlin JS platform
```

### Maven

```maven
<dependency>
  <groupId>com.jsoizo</groupId>
  <artifactId>kotlin-csv-jvm</artifactId>
  <version>1.10.0</version>
</dependency>
<dependency>
  <groupId>com.jsoizo</groupId>
  <artifactId>kotlin-csv-js</artifactId>
  <version>1.10.0</version>
</dependency>
```

### [kscript](https://github.com/holgerbrandl/kscript)

```kotlin
@file:DependsOn("com.jsoizo:kotlin-csv-jvm:1.10.0") // for JVM platform
@file:DependsOn("com.jsoizo:kotlin-csv-js:1.10.0") // for Kotlin JS platform
```

## Examples

### CSV Read examples

#### Simple case

You can read csv file from `String`, `java.io.File` or `java.io.InputStream` object.  
No need to do any I/O handling. (No need to call `use`, `close` and `flush` method.)

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

Learn more about the `Sequence` type on [Kotlin's official documentation](https://kotlinlang.org/docs/reference/sequences.html).

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
        println(row) //{id=1, name=jsoizo}
    }
}
```

NOTE: `readAllAsSequence` and `readAllWithHeaderAsSequence` methods can only be called within the `open` lambda block.
The input stream is closed after the `open` lambda block.

#### Read line by line

If you want to handle line-by-line, you can do it by using `open` method. Use `open` method and then use `readNext`
method inside nested block to read row.

```kotlin
csvReader().open("test.csv") {
    readNext()
}
```

#### Read in a `Suspending Function`

```kotlin
csvReader().openAsync("test.csv") {
    val container = mutalbeListOf<List<String>>()
    delay(100) //other suspending task
    readAllAsSequence().asFlow().collect { row ->
        delay(100) // other suspending task
        container.add(row)
    }
}
```

Note: `openAsync` can be and only be accessed through a `coroutine` or another `suspending` function

#### Customize

When you create CsvReader, you can choose read options:

```kotlin
// this is tsv reader's option
val tsvReader = csvReader {
    charset = "ISO_8859_1"
    quoteChar = '"'
    delimiter = '\t'
    escapeChar = '\\'
}
```

| Option                         | default value | description                                                                                                                                                                                                                                                                            |
|--------------------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| logger                         | _no-op_       | Logger instance for logging debug information at runtime.                                                                                                                                                                                                                              |
| charset                        | `UTF-8`       | Charset encoding. The value must be supported by [java.nio.charset.Charset](https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html).                                                                                                                                  |
| quoteChar                      | `"`           | Character used to quote fields.                                                                                                                                                                                                                                                        |
| delimiter                      | `,`           | Character used as delimiter between each field.<br />Use `"\t"` if reading TSV file.                                                                                                                                                                                                   |
| escapeChar                     | `"`           | Character to escape quote inside field string.<br />Normally, you don't have to change this option.<br />See detail comment on [ICsvReaderContext](src/commonMain/kotlin/com/github/doyaaaaaken/kotlincsv/dsl/context/CsvReaderContext.kt).                                            |
| skipEmptyLine                  | `false`       | Whether to skip or error out on empty lines.                                                                                                                                                                                                                                           |
| autoRenameDuplicateHeaders     | `false`       | Whether to auto rename duplicate headers or throw an exception.                                                                                                                                                                                                                        |
| ~~skipMissMatchedRow~~         | `false`       | Deprecated. Replace with appropriate values in `excessFieldsRowBehaviour` and `insufficientFieldsRowBehaviour`, e.g. both set to `IGNORE`. ~~Whether to skip an invalid row. If `ignoreExcessCols` is true, only rows with less than the expected number of columns will be skipped.~~ |
| excessFieldsRowBehaviour       | `ERROR`       | Behaviour to use when a row has more fields (columns) than expected. `ERROR` (default), `IGNORE` (skip the row) or `TRIM` (remove the excess fields at the end of the row to match the expected number of fields).                                                                     |
| insufficientFieldsRowBehaviour | `ERROR`       | Behaviour to use when a row has fewer fields (columns) than expected. `ERROR` (default), `IGNORE` (skip the row) or `EMPTY_STRING` (replace missing fields with an empty string).                                                                                                      |

### CSV Write examples

#### Simple case

You can start writing csv in one line, no need to do any I/O handling (No need to call `use`, `close` and `flush`
method.):

```kotlin
val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
csvWriter().writeAll(rows, "test.csv")

// if you'd append data on the tail of the file, assign `append = true`.
csvWriter().writeAll(rows, "test.csv", append = true)

// You can also write into OutpusStream.
csvWriter().writeAll(rows, File("test.csv").outputStream())
```

You can also write a csv file line by line by `open` method:

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

#### Write in a `Suspending Function`

```kotlin
val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f")).asSequence()
csvWriter().openAsync(testFileName) {
    delay(100) //other suspending task
    rows.asFlow().collect {
        delay(100) // other suspending task
        writeRow(it)
    }
}
```

#### Write as String

```kotlin
val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
val csvString: String = csvWriter().writeAllAsString(rows) //a,b,c\r\nd,e,f\r\n
```

#### long-running write (manual control for file close)

If you want to close a file writer manually for performance reasons (e.g. streaming scenario), you can
use `openAndGetRawWriter` and get a raw `CsvFileWriter`.  
**DO NOT forget to `close` the writer!**

```kotlin
val row1 = listOf("a", "b", "c")

@OptIn(KotlinCsvExperimental::class)
val writer = csvWriter().openAndGetRawWriter("test.csv")
writer.writeRow(row1)
writer.close()
```

#### Customize

When you create a CsvWriter, you can choose write options.

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
| prependBOM | `false` | Output BOM (Byte Order Mark) at the beginning of file or not. |
| quote.char  | `"` | Character to quote each fields. |
| quote.mode  | `CANONICAL` | Quote mode. <br />- `CANONICAL`: Not quote normally, but quote special characters (quoteChar, delimiter, line feed). This is [the specification of CSV](https://tools.ietf.org/html/rfc4180#section-2).<br />- `ALL`: Quote all fields.<br />- `NON_NUMERIC`: Quote non-numeric fields. (ex. 1,"a",2.3) |

# Links

**Documents**

* [Change Logs](https://github.com/jsoizo/kotlin-csv/releases)

**Libraries which use kotlin-csv**

* [kotlin-grass](https://github.com/blackmo18/kotlin-grass): Csv File to Kotlin Data Class Parser.

# Miscellaneous

## 🤝 Contributing

Contributions, [issues](https://github.com/jsoizo/kotlin-csv/issues) and feature requests are welcome!
If you have questions, ask away in [Kotlin Slack's](https://kotlinlang.slack.com) `kotlin-csv` room.

## 💻 Development

```sh
git clone git@github.com:jsoizo/kotlin-csv.git
cd kotlin-csv
./gradlew check
```

## Show your support

Give a ⭐️ if this project helped you!

## 📝 License

Copyright © 2024 [jsoizo](https://github.com/jsoizo).
This project is licensed under [Apache 2.0](LICENSE).

***
_This project is inspired ❤️ by [scala-csv](https://github.com/tototoshi/scala-csv)_

_This README was generated with ❤️ by [readme-md-generator](https://github.com/kefranabg/readme-md-generator)_

## Acknowledgments

This project was originally created by [@doyaaaaaken](https://github.com/doyaaaaaken). The initial work and contributions are greatly appreciated.
