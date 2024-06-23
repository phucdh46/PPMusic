package com.dhp.musicplayer.core.common.utils

import android.util.Log

object Logg {
    fun d(log: String) {
        val stackTrace = Exception().stackTrace[1]
        var fileName = stackTrace.fileName
        if (fileName == null) fileName =
            "" // It is necessary if you want to use proguard obfuscation.
        val info = (//stackTrace.methodName +
                " (" + fileName + ":"
                        + stackTrace.lineNumber + ")")
        Log.d("PPP", "$info: $log")
    }

}