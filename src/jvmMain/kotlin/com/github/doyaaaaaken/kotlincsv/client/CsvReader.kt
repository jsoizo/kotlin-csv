package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvReaderContext
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

/**
 * CSV Reader class
 *
 * @author doyaaaaaken
 */
actual class CsvReader actual constructor(
    private val ctx: CsvReaderContext
) : ICsvReaderContext by ctx {

    private val charsetCode = Charset.forName(charset)

    /**
     * read csv data as String, and convert into List<List<String?>>
     *
     * No need to close InputStream when calling this method.
     */
    actual fun readAll(data: String): List<List<String?>> {
        val br = data.byteInputStream(charsetCode).bufferedReader(charsetCode)
        return open(br) { readAllAsSequence().toList() }
    }

    /**
     * read csv data as File, and convert into List<List<String?>>
     *
     * No need to close InputStream when calling this method.
     */
    fun readAll(file: File): List<List<String?>> {
        val br = file.inputStream().bufferedReader(charsetCode)
        return open(br) { readAllAsSequence().toList() }
    }

    /**
     * read csv data as InputStream, and convert into List<List<String?>>
     *
     * No need to close InputStream when calling this method.
     */
    fun readAll(ips: InputStream): List<List<String?>> {
        val br = ips.bufferedReader(charsetCode)
        return open(br) { readAllAsSequence().toList() }
    }

    /**
     * read csv data with header, and convert into List<Map<String, String?>>
     *
     * No need to close InputStream when calling this method.
     */
    actual fun readAllWithHeader(data: String): List<Map<String, String?>> {
        val br = data.byteInputStream(charsetCode).bufferedReader(charsetCode)
        return open(br) { readAllWithHeaderAsSequence().toList() }
    }

    /**
     * read csv data with header, and convert into List<Map<String, String?>>
     *
     * No need to close InputStream when calling this method.
     */
    fun readAllWithHeader(file: File): List<Map<String, String?>> {
        val br = file.inputStream().bufferedReader(charsetCode)
        return open(br) { readAllWithHeaderAsSequence().toList() }
    }

    /**
     * read csv data with header, and convert into List<Map<String, String?>>
     *
     * No need to close InputStream when calling this method.
     */
    fun readAllWithHeader(ips: InputStream): List<Map<String, String?>> {
        val br = ips.bufferedReader(charsetCode)
        return open(br) { readAllWithHeaderAsSequence().toList() }
    }

    /**
     * open inputStreamReader and execute reading process.
     *
     * If you want to control read flow precisely, use this method.
     * Otherwise, use utility method (e.g. CsvReader.readAll ).
     *
     * Usage example:
     * <pre>
     *   val data: Sequence<List<String?>> = csvReader().open("test.csv") {
     *       readAllAsSequence()
     *           .map { fields -> fields.map { it.trim() } }
     *           .map { fields -> fields.map { if(it.isBlank()) null else it } }
     *   }
     * </pre>
     */
    fun <T> open(fileName: String, read: CsvFileReader.() -> T): T {
        return open(File(fileName), read)
    }

    /**
     * open inputStreamReader and execute reading process on a **suspending** function.
     *
     * If you want to control read flow precisely, use this method.
     * Otherwise, use utility method (e.g. CsvReader.readAll ).
     *
     * Usage example:
     * <pre>
     *   val data: Sequence<List<String?>> = csvReader().openAsync("test.csv") {
     *       readAllAsSequence()
     *           .map { fields -> fields.map { it.trim() } }
     *           .map { fields -> fields.map { if(it.isBlank()) null else it } }
     *   }
     * </pre>
     */
    suspend fun <T> openAsync(fileName: String, read: suspend CsvFileReader.() -> T): T {
        return openAsync(File(fileName), read)
    }

    /**
     * open inputStreamReader and execute reading process.
     *
     * If you want to control read flow precisely, use this method.
     * Otherwise, use utility method (e.g. CsvReader.readAll ).
     *
     * Usage example:
     * @see open method
     */
    fun <T> open(file: File, read: CsvFileReader.() -> T): T {
        val br = file.inputStream().bufferedReader(charsetCode)
        return open(br, read)
    }

    /**
     * open inputStreamReader and execute reading process on a **suspending** function.
     *
     * If you want to control read flow precisely, use this method.
     * Otherwise, use utility method (e.g. CsvReader.readAll ).
     *
     * Usage example:
     * @see openAsync method
     */
    suspend fun <T> openAsync(file: File, read: suspend CsvFileReader.() -> T): T {
        val br = file.inputStream().bufferedReader(charsetCode)
        return openAsync(br, read)
    }

    /**
     * open inputStreamReader and execute reading process on a **suspending** function.
     *
     * If you want to control read flow precisely, use this method.
     * Otherwise, use utility method (e.g. CsvReader.readAll ).
     *
     * Usage example:
     * @see open method
     */
    fun <T> open(ips: InputStream, read: CsvFileReader.() -> T): T {
        val br = ips.bufferedReader(charsetCode)
        return open(br, read)
    }

    /**
     * open inputStreamReader and execute reading process on a **suspending** function.
     *
     * If you want to control read flow precisely, use this method.
     * Otherwise, use utility method (e.g. CsvReader.readAll ).
     *
     * Usage example:
     * @see openAsync method
     */
    suspend fun <T> openAsync(ips: InputStream, read: suspend CsvFileReader.() -> T): T {
        val br = ips.bufferedReader(charsetCode)
        return openAsync(br, read)
    }

    private fun <T> open(br: Reader, doRead: CsvFileReader.() -> T): T {
        val reader = CsvFileReader(ctx, br, logger)
        return reader.use {
            reader.doRead()
        }
    }

    private suspend fun <T> openAsync(br: Reader, doRead: suspend CsvFileReader.() -> T): T {
        val reader = CsvFileReader(ctx, br, logger)
        return reader.use {
            reader.doRead()
        }
    }
}

private inline fun <R> CsvFileReader.use(block: (CsvFileReader) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when (exception) {
            null -> close()
            else ->
                try {
                    close()
                } catch (t: Throwable) {
                    exception.addSuppressed(t)
                }
        }
    }
}
