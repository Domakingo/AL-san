package com.doma.alsan.data.response.anilist

import com.doma.alsan.type.MediaListStatus


data class StatusDistribution(
    val status: MediaListStatus? = null,
    val amount: Int = 0
)