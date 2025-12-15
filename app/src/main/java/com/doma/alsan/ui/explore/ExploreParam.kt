package com.doma.alsan.ui.explore

import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.helper.enums.SearchCategory

data class ExploreParam(
    val searchCategory: SearchCategory,
    val mediaFilter: MediaFilter?
)
