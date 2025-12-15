package com.doma.alsan.data.response.anilist

import com.doma.alsan.data.entity.AppSetting

data class UserAvatar(
    val large: String = "",
    val medium: String = ""
) {
    fun getImageUrl(appSetting: AppSetting): String {
        return if (appSetting.useHighestQualityImage)
            large
        else
            medium
    }
}