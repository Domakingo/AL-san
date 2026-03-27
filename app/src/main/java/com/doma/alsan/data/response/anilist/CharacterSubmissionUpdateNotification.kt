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
        return if (contexts.size >= 3)
            "${contexts[0]}${character.name.userPreferred}${contexts[1]}${status}${contexts[2]}"
        else
            "${character.name.userPreferred} submission update"
    }
}
