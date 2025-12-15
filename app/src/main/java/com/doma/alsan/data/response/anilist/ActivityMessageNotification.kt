package com.doma.alsan.data.response.anilist

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.type.NotificationType

data class ActivityMessageNotification(
    override val id: Int = 0,
    val userId: Int = 0,
    override val type: NotificationType = NotificationType.ACTIVITY_MESSAGE,
    val activityId: Int = 0,
    val context: String = "",
    override val createdAt: Int = 0,
    val message: MessageActivity? = null,
    val user: User = User()
) : Notification {
    override fun getMessage(appSetting: AppSetting): String {
        return "${user.name}${context}"
    }
}