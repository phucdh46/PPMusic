package com.dhp.musicplayer.enums

enum class RepeatModes(val value: Int) {
    NONE(0),
    ONE(1),
    ALL(2);

    companion object {
        // Helper function to find enum by value
        fun fromInt(value: Int): RepeatModes {
            return values().find { it.value == value } ?: NONE // Default to NONE if not found
        }
    }
}