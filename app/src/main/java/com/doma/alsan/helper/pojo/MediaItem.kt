package com.doma.alsan.helper.pojo

import com.doma.alsan.data.response.Episode
import com.doma.alsan.data.response.anilist.CharacterEdge
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.PageInfo
import com.doma.alsan.data.response.anilist.StaffEdge

data class MediaItem(
    val media: Media = Media(),
    val viewType: Int = 0,
    var showFullDescription: Boolean = false,
    var showSpoilerTags: Boolean = false,
    var themeGroup: String = "",
    val episodes: List<Episode> = listOf(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    var characterEdge: CharacterEdge? = null,
    var staffEdge: StaffEdge? = null,
    var episode: Episode? = null,
    var pagination: PageInfo? = null,
    var isCurrent: Boolean = false
) {
    companion object {
        const val VIEW_TYPE_SYNOPSIS = 100
        const val VIEW_TYPE_CHARACTERS = 200
        const val VIEW_TYPE_INFO = 300
        const val VIEW_TYPE_GENRE = 400
        const val VIEW_TYPE_TAGS = 500
        const val VIEW_TYPE_THEMES_OPENING = 600
        const val VIEW_TYPE_THEMES_ENDING = 601
        const val VIEW_TYPE_STATS = 700
        const val VIEW_TYPE_RELATIONS = 800
        const val VIEW_TYPE_RECOMMENDATIONS = 900
        const val VIEW_TYPE_TRAILERS = 1000
        const val VIEW_TYPE_LINKS = 1100
        const val VIEW_TYPE_EPISODES = 1200
        const val VIEW_TYPE_STAFF = 1300
        const val VIEW_TYPE_CHARACTER_ITEM = 201
        const val VIEW_TYPE_STAFF_ITEM = 1301
        const val VIEW_TYPE_CHARACTER_LANGUAGE = 202
        const val VIEW_TYPE_EPISODE_ITEM = 1201
        const val VIEW_TYPE_EPISODE_PAGINATION = 1202
    }
}