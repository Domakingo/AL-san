package com.doma.alsan.helper.pojo

import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.data.response.anilist.Studio
import com.doma.alsan.helper.enums.Favorite

data class FavoriteItem(
    val anime: Media? = null,
    val manga: Media? = null,
    val character: Character? = null,
    val staff: Staff? = null,
    val studio: Studio? = null,
    val favorite: Favorite
)