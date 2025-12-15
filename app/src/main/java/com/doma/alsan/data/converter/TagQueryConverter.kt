package com.doma.alsan.data.converter

import com.doma.alsan.TagQuery
import com.doma.alsan.data.response.anilist.MediaTag

fun TagQuery.Data.convert(): List<MediaTag> {
    return MediaTagCollection?.mapNotNull {
        MediaTag(
            id = it?.id ?: 0,
            name = it?.name ?: "",
            description = it?.description ?: "",
            category = it?.category ?: "",
            rank = it?.rank ?: 0,
            isGeneralSpoiler = it?.isGeneralSpoiler ?: false,
            isMediaSpoiler = it?.isMediaSpoiler ?: false,
            isAdult = it?.isAdult ?: false
        )
    } ?: listOf()
}