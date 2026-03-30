package com.doma.alsan.data.response.anilist

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.type.NotificationType

data class AiringNotification(
    override val id: Int = 0,
    override val type: NotificationType = NotificationType.AIRING,
    val animeId: Int = 0,
    val episode: Int = 0,
    val contexts: List<String> = listOf(),
    override val createdAt: Int = 0,
    val media: Media = Media()
) : Notification {
    override fun getMessage(appSetting: AppSetting): String {
        val title = media.getTitle(appSetting)
        return when {
            contexts.size >= 3 -> "${contexts[0]}${episode}${contexts[1]}$title${contexts[2]}"
            contexts.size == 2 -> "${contexts[0]}${episode}${contexts[1]}$title"
            contexts.size == 1 -> "${contexts[0]}${episode} $title"
            else -> "Episode $episode $title"
        }
    }
}