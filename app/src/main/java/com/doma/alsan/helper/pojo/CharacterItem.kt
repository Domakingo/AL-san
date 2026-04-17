package com.doma.alsan.helper.pojo

import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.MediaEdge
import com.doma.alsan.data.response.anilist.StaffEdge
import com.doma.alsan.data.response.anilist.StaffRoleType

data class CharacterItem(
    val character: Character = Character(),
    val voiceActor: StaffRoleType? = null,
    val mediaEdge: MediaEdge? = null,
    val staffEdge: StaffEdge? = null,
    var showFullDescription: Boolean = false,
    val title: String = "",
    val viewType: Int = 0
) {
    companion object {
        const val VIEW_TYPE_DETAILS = 100
        const val VIEW_TYPE_MEDIA_GROUP = 200
        const val VIEW_TYPE_MEDIA_ITEM = 300
        const val VIEW_TYPE_VOICE_ACTOR_ITEM = 400
        const val VIEW_TYPE_STATS = 500
    }
}