package com.doma.alsan.helper.pojo

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.helper.enums.ListType

data class SeasonalAdapterComponent(
    val listType: ListType = ListType.LINEAR,
    val appSetting: AppSetting = AppSetting()
)