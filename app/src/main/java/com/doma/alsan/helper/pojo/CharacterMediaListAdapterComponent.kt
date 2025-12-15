package com.doma.alsan.helper.pojo

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.type.MediaSort

data class CharacterMediaListAdapterComponent(
    val appSetting: AppSetting = AppSetting(),
    val mediaSort: MediaSort = MediaSort.POPULARITY_DESC
)