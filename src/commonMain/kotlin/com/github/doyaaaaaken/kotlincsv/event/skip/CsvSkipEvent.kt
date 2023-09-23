package com.github.doyaaaaaken.kotlincsv.event.skip


fun interface SkipNotify {
    fun onSkipped(row: SkippedRow)
}

/**
 * interface function that will be triggered when skipping either insufficient or excess number of fields
 */
fun interface INotifySkippedEvent {
   fun notify(skippedRow: SkippedRow)
}


fun skipNotification(block: NotifySkipEvent.() -> Unit): INotifySkippedEvent {
    return NotifySkipEvent().apply(block)
}