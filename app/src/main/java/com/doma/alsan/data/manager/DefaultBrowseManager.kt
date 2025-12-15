package com.doma.alsan.data.manager

import com.doma.alsan.BuildConfig
import com.doma.alsan.data.localstorage.SharedPreferencesHandler
import com.doma.alsan.data.response.SpotifyAccessToken
import com.doma.alsan.helper.enums.ListType

class DefaultBrowseManager(private val sharedPreferencesHandler: SharedPreferencesHandler) : BrowseManager {

    override var othersListType: ListType
        get() = sharedPreferencesHandler.othersListType ?: ListType.LINEAR
        set(value) { sharedPreferencesHandler.othersListType = value }

    override val youTubeApiKey: String
        get() = BuildConfig.YOUTUBE_API_KEY

    override val spotifyApiKey: String
        get() = BuildConfig.SPOTIFY_API_KEY

    override var spotifyAccessToken: SpotifyAccessToken
        get() = sharedPreferencesHandler.spotifyAccessToken ?: SpotifyAccessToken()
        set(value) { sharedPreferencesHandler.spotifyAccessToken = value }

    override var spotifyAccessTokenLastRetrieve: Long
        get() = sharedPreferencesHandler.spotifyAccessTokenLastRetrieve ?: 0
        set(value) { sharedPreferencesHandler.spotifyAccessTokenLastRetrieve = value }
}