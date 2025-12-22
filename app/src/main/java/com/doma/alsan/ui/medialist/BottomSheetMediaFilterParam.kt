package com.doma.alsan.ui.medialist

import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.response.anilist.MediaListGroup
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.type.ScoreFormat

/**
 * Parameters for the compact media filter bottom sheet dialog.
 */
data class BottomSheetMediaFilterParam(
    val mediaFilter: MediaFilter,
    val mediaType: MediaType,
    val scoreFormat: ScoreFormat,
    val isUserList: Boolean,
    val hasBigList: Boolean,
    val isViewer: Boolean,
    val listSections: List<MediaListGroup>,
    val selectedSectionIndex: Int,
    val isAllListPositionAtTop: Boolean
)
