package com.doma.alsan.data.response.anilist

import com.doma.alsan.type.MediaListStatus


data class ListActivityOption(
    val disabled: Boolean = false,
    val type: MediaListStatus? = null
)