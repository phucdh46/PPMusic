package com.dhp.musicplayer.utils
import android.util.Log

object Log {
    fun d(log: String) {
        val stackTrace = Exception().stackTrace[1]
        var fileName = stackTrace.fileName
        if (fileName == null) fileName =
            "" // It is necessary if you want to use proguard obfuscation.
        val info = (//stackTrace.methodName +
                " (" + fileName + ":"
                + stackTrace.lineNumber + ")")
        Log.d("DHP", "$info: $log")
    }
}