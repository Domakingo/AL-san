package com.doma.alsan.helper.pojo

import com.doma.alsan.data.response.anilist.MediaList

data class ReleasingTodayItem(
    val mediaList: MediaList,
    val episode: Int,
    val timeUntilAiring: Int
)