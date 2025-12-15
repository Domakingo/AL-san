package com.doma.alsan.data.converter

import com.doma.alsan.data.response.TrackSearch
import com.doma.alsan.data.response.spotify.TrackSearchResponse

fun TrackSearchResponse.convert(): TrackSearch {
    return TrackSearch(
        trackUrl = tracks?.items?.firstOrNull()?.externalUrls?.spotify ?: ""
    )
}