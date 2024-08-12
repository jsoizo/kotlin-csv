package com.jsoizo.kotlincsv.client

internal interface Reader {
    /**
     * Reads a single character.
     * Returns: The character read, as an integer in the range 0 to 65535 (0x00-0xffff), or -1 if the end of the stream has been reached
     */
    fun read(): Int

    /**
     * Marks the present position in the stream.  Subsequent calls to reset()
     * will attempt to reposition the stream to this point.
     *
     * @param readAheadLimit   Limit on the number of characters that may be
     *                         read while still preserving the mark. An attempt
     *                         to reset the stream after reading characters
     *                         up to this limit or beyond may fail.
     *                         A limit value larger than the size of the input
     *                         buffer will cause a new buffer to be allocated
     *                         whose size is no smaller than limit.
     *                         Therefore large values should be used with care.
     *
     * @exception  IllegalArgumentException  If {@code readAheadLimit < 0}
     */
    fun mark(readAheadLimit: Int): Unit

    /**
     * Resets the stream to the most recent mark.
     */
    fun reset(): Unit


    fun close()
}

/**
 * Multiplatform implementation of the Reader interface that uses a String as the backing
 * data source.
 */
internal class StringReaderImpl(private val data: String) : Reader {
    private var nextChar = 0
    private var mark = -1

    override fun read(): Int {
        return if (nextChar == data.length) {
            -1
        } else {
            data[nextChar++].code
        }
    }

    override fun mark(readAheadLimit: Int) {
        mark = nextChar
    }

    override fun reset() {
        nextChar = mark
    }

    override fun close() {
    }
}