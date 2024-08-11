package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext

/**
 * CSV Reader class
 *
 * @author doyaaaaaken
 */
actual class CsvReader actual constructor(
    ctx: CsvReaderContext
) : CsvStringReader(ctx)
