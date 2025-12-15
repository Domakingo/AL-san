package com.doma.alsan.data.converter

import com.doma.alsan.data.response.VideoSearch
import com.doma.alsan.data.response.youtube.VideoSearchResponse

fun VideoSearchResponse.convert(): VideoSearch {
    return VideoSearch(
        videoId = items?.firstOrNull()?.id?.videoId ?: ""
    )
}