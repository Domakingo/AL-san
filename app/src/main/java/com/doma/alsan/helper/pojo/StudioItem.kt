package com.doma.alsan.helper.pojo

import com.doma.alsan.data.response.anilist.Studio

class StudioItem(
    val studio: Studio = Studio(),
    val viewType: Int = 0
) {
    companion object {
        const val VIEW_TYPE_MEDIA = 100
    }
}