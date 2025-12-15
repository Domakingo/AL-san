package com.doma.alsan.ui.favorite

import com.doma.alsan.helper.enums.Favorite

data class FavoriteParam(
    val userId: Int,
    val favorite: Favorite
)