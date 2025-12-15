package com.doma.alsan.data.manager

import com.doma.alsan.data.response.SpotifyAccessToken
import com.doma.alsan.helper.enums.ListType

interface BrowseManager {
    var othersListType: ListType
    val youTubeApiKey: String
    val spotifyApiKey: String
    var spotifyAccessToken: SpotifyAccessToken
    var spotifyAccessTokenLastRetrieve: Long
}