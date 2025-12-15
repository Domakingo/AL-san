package com.doma.alsan.ui.activity

import com.doma.alsan.helper.enums.ActivityListPage

data class ActivityListParam(
    val activityListPage: ActivityListPage,
    val userId: Int
)