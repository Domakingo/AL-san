package com.doma.alsan.helper.enums

import com.doma.alsan.helper.extensions.convertFromSnakeCase

enum class ListType {
    LINEAR,
    GRID,
    SIMPLIFIED,
    ALBUM
}

fun ListType.getString(): String {
    return name.convertFromSnakeCase()
}