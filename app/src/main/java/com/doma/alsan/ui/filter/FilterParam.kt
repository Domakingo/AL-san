package com.doma.alsan.ui.filter

import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.type.ScoreFormat

data class FilterParam(
    val mediaFilter: MediaFilter,
    val mediaType: MediaType,
    val scoreFormat: ScoreFormat,
    val isUserList: Boolean,
    val hasBigList: Boolean,
    val isCurrentUser: Boolean
)