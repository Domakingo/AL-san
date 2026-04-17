package com.doma.alsan.helper.enums

enum class AppFontSize(val value: Float) {
    SMALL(0.9f),
    NORMAL(1.0f),
    LARGE(1.2f);
    
    companion object {
        fun fromInt(value: Int): AppFontSize {
            return entries.getOrElse(value) { NORMAL }
        }
    }
}
