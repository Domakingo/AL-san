package com.doma.alsan.data.response.anilist

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.type.NotificationType

data class FollowingNotification(
    override val id: Int = 0,
    val userId: Int = 0,
    override val type: NotificationType = NotificationType.FOLLOWING,
    val context: String = "",
    override val createdAt: Int = 0,
    val user: User = User()
) : Notification {
    override fun getMessage(appSetting: AppSetting): String {
        return "${user.name}${context}"
    }
}