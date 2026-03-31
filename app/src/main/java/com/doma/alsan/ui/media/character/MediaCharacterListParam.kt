package com.doma.alsan.ui.media.character

import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.type.StaffLanguage

data class MediaCharacterListParam(
    val mediaId: Int,
    val mediaType: MediaType = MediaType.ANIME,
    val selectedLanguage: StaffLanguage? = null,
    val availableLanguages: List<StaffLanguage>? = null
)