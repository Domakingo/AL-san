package com.doma.alsan.helper.pojo

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.User

data class HomeAdapterComponent(
    val user: User? = null,
    val appSetting: AppSetting = AppSetting()
)
