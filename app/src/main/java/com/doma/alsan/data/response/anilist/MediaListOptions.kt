package com.doma.alsan.data.response.anilist

import com.doma.alsan.type.ScoreFormat


data class MediaListOptions(
    var scoreFormat: ScoreFormat? = null,
    var rowOrder: String = "",
    val animeList: MediaListTypeOptions = MediaListTypeOptions(),
    val mangaList: MediaListTypeOptions = MediaListTypeOptions()
)