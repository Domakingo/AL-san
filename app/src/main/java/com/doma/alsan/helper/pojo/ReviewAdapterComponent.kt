package com.doma.alsan.helper.pojo

import com.doma.alsan.data.entity.AppSetting

data class ReviewAdapterComponent(
    val appSetting: AppSetting = AppSetting(),
    val isMediaReview: Boolean = true,
    val isUserReview: Boolean = true
)
