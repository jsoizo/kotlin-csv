package com.jsoizo.kotlincsv.reader.internal

/**
 * Pull-based single-char window over a character stream.
 *
 * [cur] holds the current, not-yet-consumed UTF-16 code unit, or [EOF].
 * [advance] moves the window one char forward and returns the new [cur].
 * After construction the window is primed: [cur] is the first char or [EOF].
 *
 * Lookahead is expressed as "advance, then inspect [cur]", so refill logic
 * lives in one place and chunk boundaries are invisible to the parser.
 * `Int` with an EOF sentinel avoids `Char?` boxing on all targets.
 */
internal abstract class CharCursor {
    var cur: Int = EOF
        protected set

    /** Move the window one char forward; returns the new [cur], or [EOF]. */
    abstract fun advance(): Int

    companion object {
        const val EOF = -1
    }
}

internal class IteratorCharCursor(iterator: Iterator<Char>) : CharCursor() {
    // String.asSequence() etc. expose a CharIterator; nextChar() skips boxing.
    private val charIterator: CharIterator? = iterator as? CharIterator
    private val boxedIterator: Iterator<Char> = iterator

    init {
        advance()
    }

    override fun advance(): Int {
        val chars = charIterator
        cur = when {
            chars != null -> if (chars.hasNext()) chars.nextChar().code else EOF
            else -> if (boxedIterator.hasNext()) boxedIterator.next().code else EOF
        }
        return cur
    }
}

/**
 * Cursor over a chunked char source. [readInto] fills the buffer and returns
 * the number of chars written; `<= 0` signals EOF. [readInto] may be invoked
 * again after signalling EOF and must then keep returning `<= 0` (both
 * `java.io.Reader` and the kotlinx-io adapter satisfy this), which keeps
 * [advance] idempotent at EOF without extra state.
 */
internal class ChunkCharCursor(
    private val readInto: (CharArray) -> Int,
    bufferSize: Int,
) : CharCursor() {
    private val buffer = CharArray(bufferSize)
    private var length = 0
    private var position = 0

    init {
        advance()
    }

    override fun advance(): Int {
        cur = if (position < length || refill()) buffer[position++].code else EOF
        return cur
    }

    private fun refill(): Boolean {
        length = readInto(buffer)
        position = 0
        return length > 0
    }
}
