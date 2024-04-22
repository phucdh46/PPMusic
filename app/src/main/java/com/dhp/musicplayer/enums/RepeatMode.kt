package com.dhp.musicplayer.enums

enum class RepeatMode(val value: Int) {
    NONE(0),
    ONE(1),
    ALL(2);

    companion object {
        // Helper function to find enum by value
        fun fromInt(value: Int): RepeatMode {
            return values().find { it.value == value } ?: NONE // Default to NONE if not found
        }
    }
}