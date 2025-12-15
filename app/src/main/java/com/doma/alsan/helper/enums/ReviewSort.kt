package com.doma.alsan.helper.enums

import android.content.Context
import com.doma.alsan.R

enum class ReviewSort {
    NEWEST,
    OLDEST,
    MOST_UPVOTE,
    FEWEST_UPVOTE
}

fun ReviewSort.getString(context: Context): String {
    return context.getString(getStringResource())
}

fun ReviewSort.getStringResource(): Int {
    return when (this) {
        ReviewSort.NEWEST -> R.string.newest
        ReviewSort.OLDEST -> R.string.oldest
        ReviewSort.MOST_UPVOTE -> R.string.most_upvote
        ReviewSort.FEWEST_UPVOTE -> R.string.fewest_upvote
    }
}

fun ReviewSort.getAniListReviewSort(): com.doma.alsan.type.ReviewSort {
    return when (this) {
        ReviewSort.NEWEST -> com.doma.alsan.type.ReviewSort.CREATED_AT_DESC
        ReviewSort.OLDEST -> com.doma.alsan.type.ReviewSort.CREATED_AT
        ReviewSort.MOST_UPVOTE -> com.doma.alsan.type.ReviewSort.RATING_DESC
        ReviewSort.FEWEST_UPVOTE -> com.doma.alsan.type.ReviewSort.RATING
    }
}