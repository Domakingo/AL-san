package com.doma.alsan.data.converter

import com.doma.alsan.data.response.Announcement
import com.doma.alsan.data.response.github.AnnouncementResponse

fun AnnouncementResponse.convert(): Announcement {
    return Announcement(
        id = id ?: "",
        fromDate = fromDate ?: "",
        untilDate = untilDate?: "",
        message = message ?: "",
        appVersion = appVersion?.toIntOrNull() ?: 0,
        requiredUpdate = requiredUpdate == "1"
    )
}