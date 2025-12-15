package com.doma.alsan.data.response.anilist

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.type.NotificationType

interface Notification {
    val id: Int
    val type: NotificationType
    val createdAt: Int
    fun getMessage(appSetting: AppSetting): String
}