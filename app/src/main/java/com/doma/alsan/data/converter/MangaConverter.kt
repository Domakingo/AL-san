package com.doma.alsan.data.converter

import com.doma.alsan.data.response.Manga
import com.doma.alsan.data.response.MangaSerialization
import com.doma.alsan.data.response.mal.MangaResponse

fun MangaResponse.convert(): Manga {
    return Manga(
        malId = data?.malId ?: 0,
        title = data?.title ?: "",
        serializations = data?.serializations?.map {
            MangaSerialization(
                name = it.name ?: "",
                url = it.url ?: ""
            )
        } ?: listOf()
    )
}