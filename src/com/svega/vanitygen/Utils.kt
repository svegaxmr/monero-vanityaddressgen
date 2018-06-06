package com.svega.vanitygen

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.Duration

object Utils {
    fun copyToClipboard(str: String) {
        val selection = StringSelection(str)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(selection, selection)
    }

    fun formatDuration(duration: Duration): String {
        val seconds = duration.seconds
        val absSeconds = Math.abs(seconds)
        val positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                absSeconds % 3600 / 60,
                absSeconds % 60)
        return if (seconds < 0) "-$positive" else positive
    }
}