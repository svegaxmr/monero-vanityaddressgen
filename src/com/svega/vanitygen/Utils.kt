package com.svega.vanitygen

import com.svega.common.version.Extra
import com.svega.common.version.Version
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.Duration

object Utils {
    val VERSION = Version(1, 1, 1, Version.makeExtra(Extra.ALPHA, 1))
    val VERSION_STRING = VERSION.versionString
    const val DONATION_ADDRESS = "49SVega8pmD5wvb9vai2aC7xQ5vcwbgxfSGm2sEJELoDfx5quMq3b2Rgs9Ua4LfsrTek73fuiatGfEibNvAdS55HABBsJdG"
}