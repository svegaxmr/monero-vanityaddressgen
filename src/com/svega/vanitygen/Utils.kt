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
    const val VERSION = "1.1.0 alpha-1"
    const val DONATION_ADDRESS = "49SVega8pmD5wvb9vai2aC7xQ5vcwbgxfSGm2sEJELoDfx5quMq3b2Rgs9Ua4LfsrTek73fuiatGfEibNvAdS55HABBsJdG"
}