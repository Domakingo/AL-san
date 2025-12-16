package com.doma.alsan.ui.media.character

import com.doma.alsan.helper.enums.MediaType

data class MediaCharacterListParam(
    val mediaId: Int,
    val mediaType: MediaType = MediaType.ANIME
)