<h1 align="center">Welcome to kotlin-csv 👋</h1>
<p>
  <img alt="Version" src="https://img.shields.io/badge/version-0.8.0-blue.svg?cacheSeconds=2592000" />
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
implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.8.0")

//gradle groovy DSL
implementation 'com.github.doyaaaaaken:kotlin-csv-jvm:0.8.0'
```

maven:
```
<dependency>
  <groupId>com.github.doyaaaaaken</groupId>
  <artifactId>kotlin-csv-jvm</artifactId>
  <version>0.8.0</version>
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
    writeRows(listOf(row1, row2))
}
```

#### Customize

When you create CsvWriter, you can choose write options.
```kotlin
val writer = csvWriter {
    charset = "ISO_8859_1"
    delimiter = '\t'
    nullCode = "NULL"
    lineTerminator = "\n"
    quote {
        mode = WriteQuoteMode.ALL
        char = '\''
    }
}
```


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
