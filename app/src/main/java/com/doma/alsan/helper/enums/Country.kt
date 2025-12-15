package com.doma.alsan.helper.enums

import com.doma.alsan.helper.extensions.convertFromSnakeCase

enum class Country(val iso: String) {
    JAPAN("JP"),
    SOUTH_KOREA("KR"),
    CHINA("CN"),
    TAIWAN("TW")
}

fun Country.getString(): String {
    return this.name.convertFromSnakeCase(true)
}
