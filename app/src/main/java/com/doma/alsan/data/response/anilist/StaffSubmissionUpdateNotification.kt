package com.doma.alsan.data.response.anilist

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.type.NotificationType

data class StaffSubmissionUpdateNotification(
    override val id: Int = 0,
    override val type: NotificationType = NotificationType.STAFF_SUBMISSION_UPDATE,
    val contexts: List<String> = listOf(),
    val status: String = "",
    val notes: String = "",
    override val createdAt: Int = 0,
    val staff: Staff = Staff()
) : Notification {
    override fun getMessage(appSetting: AppSetting): String {
        return if (contexts.size >= 3)
            "${contexts[0]}${staff.name.userPreferred}${contexts[1]}${status}${contexts[2]}"
        else
            "${staff.name.userPreferred} submission update"
    }
}
