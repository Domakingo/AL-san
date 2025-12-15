package com.doma.alsan.data.converter

import com.doma.alsan.GenreQuery
import com.doma.alsan.data.response.Genre

fun GenreQuery.Data.convert(): List<Genre> {
    return GenreCollection?.mapNotNull { Genre(it ?: "") } ?: listOf()
}