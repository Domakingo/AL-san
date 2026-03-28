package com.doma.alsan.data.response.anilist

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.type.NotificationType

data class CharacterSubmissionUpdateNotification(
    override val id: Int = 0,
    override val type: NotificationType = NotificationType.CHARACTER_SUBMISSION_UPDATE,
    val contexts: List<String> = listOf(),
    val status: String = "",
    val notes: String = "",
    override val createdAt: Int = 0,
    val character: Character = Character()
) : Notification {
    override fun getMessage(appSetting: AppSetting): String {
        val statusLower = status.lowercase()
        val (emoji, statusText) = when {
            statusLower.contains("partially") -> "🟡" to "partially accepted"
            statusLower.contains("accepted") -> "🟢" to "accepted"
            statusLower.contains("rejected") -> "🔴" to "rejected"
            else -> "⚪" to statusLower
        }

        return "$emoji Your character submission for <primary>${character.name.userPreferred}</primary> was <primary>$statusText</primary>"
    }
}