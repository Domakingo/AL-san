package com.doma.alsan.data.response.anilist

import com.doma.alsan.type.NotificationType


data class NotificationOption(
    val type: NotificationType? = null,
    var enabled: Boolean = false
)