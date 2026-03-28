package com.doma.alsan.data.response.anilist

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.type.NotificationType

data class MediaSubmissionUpdateNotification(
    override val id: Int = 0,
    override val type: NotificationType = NotificationType.MEDIA_SUBMISSION_UPDATE,
    val contexts: List<String> = listOf(),
    val status: String = "",
    val notes: String = "",
    override val createdAt: Int = 0,
    val media: Media = Media()
) : Notification {
    override fun getMessage(appSetting: AppSetting): String {
        val statusLower = status.lowercase()
        val (emoji, statusText) = when {
            statusLower.contains("partially") -> "🟡" to "partially accepted"
            statusLower.contains("accepted") -> "🟢" to "accepted"
            statusLower.contains("rejected") -> "🔴" to "rejected"
            else -> "⚪" to statusLower
        }

        return "$emoji Your media submission for **${media.getTitle(appSetting)}** was **$statusText**"
    }
}