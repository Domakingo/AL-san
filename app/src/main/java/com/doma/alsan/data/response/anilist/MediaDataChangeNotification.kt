package com.doma.alsan.data.response.anilist

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.type.NotificationType

data class MediaDataChangeNotification(
    override val id: Int = 0,
    override val type: NotificationType = NotificationType.MEDIA_DATA_CHANGE,
    val mediaId: Int = 0,
    val context: String = "",
    val reason: String = "",
    override val createdAt: Int = 0,
    val media: Media = Media()
) : Notification {
    override fun getMessage(appSetting: AppSetting): String {
        return "${media.getTitle(appSetting)}${context}"
    }
}