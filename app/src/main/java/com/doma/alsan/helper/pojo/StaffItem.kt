package com.doma.alsan.helper.pojo

import com.doma.alsan.data.response.anilist.CharacterEdge
import com.doma.alsan.data.response.anilist.MediaEdge
import com.doma.alsan.data.response.anilist.Staff

data class StaffItem(
    val staff: Staff = Staff(),
    val characterEdge: CharacterEdge? = null,
    val mediaEdge: MediaEdge? = null,
    var showFullDescription: Boolean = false,
    val title: String = "",
    val viewType: Int = 0
) {
    companion object {
        const val VIEW_TYPE_DETAILS = 100
        const val VIEW_TYPE_CHARACTER_GROUP = 200
        const val VIEW_TYPE_CHARACTER_ITEM = 300
        const val VIEW_TYPE_MEDIA_GROUP = 400
        const val VIEW_TYPE_MEDIA_ITEM = 500
        const val VIEW_TYPE_STATS = 600
    }
}