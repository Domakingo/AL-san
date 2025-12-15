package com.doma.alsan.data.response.anilist

import com.doma.alsan.type.RecommendationRating


data class Recommendation(
    val id: Int = 0,
    val rating: Int = 0,
    val userRating: RecommendationRating? = null,
    val mediaRecommendation: Media? = null
)