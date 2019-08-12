<h1 align="center">Welcome to kotlin-csv 👋</h1>
<p>
  <img alt="Version" src="https://img.shields.io/badge/version-0.1.0-blue.svg?cacheSeconds=2592000" />
  <a href="https://github.com/doyaaaaaken/kotlin-csv/blob/master/LICENSE">
    <img alt="License: Apache License 2.0" src="https://img.shields.io/badge/License-Apache License 2.0-yellow.svg" target="_blank" />
  </a>
  <img alt="CircleCI" src="https://circleci.com/gh/doyaaaaaken/kotlin-csv/tree/master.svg?style=svg" />
</p>

> Kotlin DSL CSV Reader/Writer

# Usage

## Download

gradle kotlin DSL:
```
compile("com.github.doyaaaaaken:kotlin-csv:0.1.0")
```

gradle groovy DSL:
```
implementation 'com.github.doyaaaaaken:kotlin-csv:0.1.0'
```

maven:
```
<dependency>
  <groupId>com.github.doyaaaaaken</groupId>
  <artifactId>kotlin-csv</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Example

### Reading example

You can read csv file by both String and java.io.File object.
```kotlin
val data: String = "a,b,c"
val rows: List<List<String>> = csvReader().read(data)

val file: File = File("test.csv")
val rows: List<List<String>> = csvReader().read(file)
```
If you want to improve performance, you can use `readAsSequence` method instead of `read` method. It gets `Sequence<List<String>>` as return value type.

You can also read tsv file by changing setting.
```kotlin
val file = File("test.csv")
val rows: csvReader{
  delimiter = '\t'
}.read(file)
```

### Writing example

TODO: implement code

# Miscellaneousness

## 🤝 Contributing

Contributions, issues and feature requests are welcome!<br />Feel free to check [issues page](https://github.com/doyaaaaaken/kotlin-csv/issues).

## 💻 Development

```
$ git clone git@github.com:doyaaaaaken/kotlin-csv.git
$ cd kotlin-csv
$ ./gradlew test
```

## Show your support

Give a ⭐️ if this project helped you!

## 📝 License

Copyright © 2019 [doyaaaaaken](https://github.com/doyaaaaaken).<br />
This project is [Apache License 2.0](https://github.com/doyaaaaaken/kotlin-csv/blob/master/LICENSE) licensed.

***
_This project is inspired ❤️ by [scala-csv](https://github.com/tototoshi/scala-csv)_

_This README was generated with ❤️ by [readme-md-generator](https://github.com/kefranabg/readme-md-generator)_
