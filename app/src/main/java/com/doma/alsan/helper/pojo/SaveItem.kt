package com.doma.alsan.helper.pojo

import com.doma.alsan.helper.utils.TimeUtil

class SaveItem<T>(
    val data: T,
    var saveTime: Long = TimeUtil.getCurrentTimeInMillis()
) {
}