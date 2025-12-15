package com.doma.alsan.data.response.anilist

import com.doma.alsan.type.MediaFormat
import com.doma.alsan.type.MediaRankType
import com.doma.alsan.type.MediaSeason


data class MediaRank(
    val id: Int = 0,
    val rank: Int = 0,
    val type: MediaRankType? = null,
    val format: MediaFormat? = null,
    val year: Int = 0,
    val season: MediaSeason? = null,
    val allTime: Boolean = false,
    val context: String = ""
)