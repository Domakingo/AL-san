package com.doma.alsan.helper.pojo

import com.doma.alsan.data.response.anilist.MediaList

/**
 * Represents an item in the media list.
 * Can be a title header, a single media list entry, or a collapsed group of related series.
 */
data class MediaListItem(
    val mediaList: MediaList = MediaList(),
    val title: String = "",
    val viewType: Int = 0,
    // For collapsed groups
    val collapsedGroup: CollapsedSeriesGroup? = null
) {
    companion object {
        const val VIEW_TYPE_TITLE = 100
        const val VIEW_TYPE_MEDIA_LIST = 200
        const val VIEW_TYPE_COLLAPSED_GROUP = 300
    }
}

/**
 * Represents a group of related series (e.g., all seasons of MHA) collapsed into one entry.
 */
data class CollapsedSeriesGroup(
    val mediaLists: List<MediaList>,
    val franchiseName: String,
    val averageScore: Double,
    val totalEntries: Int,
    val representativeMedia: MediaList // The one to show cover image, etc.
)