package com.doma.alsan.data.response

import com.doma.alsan.data.response.anilist.Notification
import com.doma.alsan.data.response.anilist.Page

data class NotificationData(
    val page: Page<Notification> = Page()
)