package com.doma.alsan.helper.utils

import android.net.Uri

class DeepLink(
    val uri: Uri?
) {
    private fun getScheme(): String? {
        return uri?.scheme
    }

    private fun getAuthority(): String? {
        return uri?.authority
    }

    private fun getFirstPath(): String? {
        return uri?.pathSegments?.firstOrNull()
    }

    private fun getSecondPath(): String? {
        return uri?.pathSegments?.getOrNull(1)
    }

    fun getQueryParamOfOrNull(key: String): String? {
        return uri?.getQueryParameter(key)
    }

    private fun isAlsanScheme() = getScheme() == SCHEME

    fun isLogin() = isAlsanScheme() && getAuthority() == LOGIN
    fun isHome() = isAlsanScheme() && getAuthority() == HOME
    fun isAnimeList() = isAlsanScheme() && getAuthority() == ANIME_LIST
    fun isMangaList() = isAlsanScheme() && getAuthority() == MANGA_LIST
    fun isNotifications() = isAlsanScheme() && getAuthority() == NOTIFICATIONS
    fun isProfile() = isAlsanScheme() && getAuthority() == PROFILE
    fun isAppSettings() = isAlsanScheme() && getAuthority() == SETTINGS && getFirstPath() == APP_SETTINGS
    fun isAniListSettings() = isAlsanScheme() && getAuthority() == SETTINGS && getFirstPath() == ANILIST_SETTINGS
    fun isListSettings() = isAlsanScheme() && getAuthority() == SETTINGS && getFirstPath() == LIST_SETTINGS
    fun isSpoiler() = isAlsanScheme() && getAuthority() == SPOILER

    private fun isAniListAuthority() = getAuthority() == ANILIST_AUTHORITY
    fun getAniListPageId() = getSecondPath()

    fun isAnime() = isAniListAuthority() && getFirstPath() == ANILIST_ANIME && getSecondPath() != null
    fun isManga() = isAniListAuthority() && getFirstPath() == ANILIST_MANGA && getSecondPath() != null
    fun isCharacter() = isAniListAuthority() && getFirstPath() == ANILIST_CHARACTER && getSecondPath() != null
    fun isStaff() = isAniListAuthority() && getFirstPath() == ANILIST_STAFF && getSecondPath() != null
    fun isStudio() = isAniListAuthority() && getFirstPath() == ANILIST_STUDIO && getSecondPath() != null
    fun isUser() = isAniListAuthority() && getFirstPath() == ANILIST_USER && getSecondPath() != null
    fun isActivity() = isAniListAuthority() && getFirstPath() == ANILIST_ACTIVITY && getSecondPath() != null

    companion object {
        fun create(uri: Uri?): DeepLink {
            return DeepLink(uri)
        }

        // region AL-chan
        private const val SCHEME = "alsan"

        private const val LOGIN = "login"
        private const val HOME = "home"
        private const val ANIME_LIST = "animelist"
        private const val MANGA_LIST = "mangalist"
        private const val NOTIFICATIONS = "notifications"
        private const val PROFILE = "profile"

        private const val SETTINGS = "settings"
        private const val APP_SETTINGS = "app"
        private const val ANILIST_SETTINGS = "anilist"
        private const val LIST_SETTINGS = "list"

        private const val SPOILER = "spoiler"

        fun generateNotifications() = generateDeepLink(NOTIFICATIONS)
        fun generateAppSettings() = generateDeepLink(SETTINGS, APP_SETTINGS)
        fun generateAniListSettings() = generateDeepLink(SETTINGS, ANILIST_SETTINGS)
        fun generateListSettings() = generateDeepLink(SETTINGS, LIST_SETTINGS)

        private fun generateDeepLink(vararg paths: String): DeepLink {
            return create(Uri.parse("${SCHEME}://${paths.joinToString("/")}"))
        }
        // endregion

        // region AniList
        private const val ANILIST_AUTHORITY = "anilist.co"
        private const val ANILIST_ANIME = "anime"
        private const val ANILIST_MANGA = "manga"
        private const val ANILIST_CHARACTER = "character"
        private const val ANILIST_STAFF = "staff"
        private const val ANILIST_STUDIO = "studio"
        private const val ANILIST_USER = "user"
        private const val ANILIST_ACTIVITY = "activity"
        // endregion

        const val QUERY_PARAM_DATA = "data"
    }
}