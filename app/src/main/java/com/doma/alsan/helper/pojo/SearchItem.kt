package com.doma.alsan.helper.pojo

import com.doma.alsan.data.response.anilist.*
import com.doma.alsan.helper.enums.SearchCategory

data class SearchItem(
    val media: Media = Media(),
    val character: Character = Character(),
    val staff: Staff = Staff(),
    val studio: Studio = Studio(),
    val user: User = User(),
    val searchCategory: SearchCategory = SearchCategory.ANIME
)