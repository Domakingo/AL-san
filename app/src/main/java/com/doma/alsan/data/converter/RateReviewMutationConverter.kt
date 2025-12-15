package com.doma.alsan.data.converter

import com.doma.alsan.RateReviewMutation
import com.doma.alsan.data.response.anilist.Review

fun RateReviewMutation.Data.convert(): Review {
    return Review(
        id = RateReview?.id ?: 0,
        rating = RateReview?.rating ?: 0,
        ratingAmount = RateReview?.ratingAmount ?: 0,
        userRating = RateReview?.userRating
    )
}