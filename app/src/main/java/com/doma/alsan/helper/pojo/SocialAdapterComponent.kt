package com.doma.alsan.helper.pojo

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.User

data class SocialAdapterComponent(
    val viewer: User? = null,
    val appSetting: AppSetting = AppSetting()
)