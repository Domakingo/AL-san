package com.doma.alsan.data.response.anilist

data class MediaStats(
    val scoreDistribution: List<ScoreDistribution> = listOf(),
    val statusDistribution: List<StatusDistribution> = listOf()
)