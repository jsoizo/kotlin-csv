package com.github.doyaaaaaken.kotlincsv.event.skip

class NotifySkipEvent: INotifySkippedEvent {
    val listeners = mutableMapOf<String, SkipNotify>()
    override fun notify(skippedRow: SkippedRow) {
        listeners.forEach { (_, value) -> value.onSkipped(skippedRow) }
    }
}