package com.doma.alsan.helper.service.pushnotification

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Notification

data class PushNotificationParam(
    val notifications: List<Notification>,
    val unreadNotificationCount: Int,
    val appSetting: AppSetting,
    val lastNotificationId: Int
)
