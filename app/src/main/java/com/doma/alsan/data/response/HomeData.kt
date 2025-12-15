package com.doma.alsan.data.response

import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.Review

data class HomeData(
    val trendingAnime: List<Media> = listOf(),
    val trendingManga: List<Media> = listOf(),
    val newAnime: List<Media> = listOf(),
    val newManga: List<Media> = listOf()
)