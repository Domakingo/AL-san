package com.doma.alsan.data.converter

import com.doma.alsan.data.response.SpotifyAccessToken
import com.doma.alsan.data.response.spotify.SpotifyAccessTokenResponse

fun SpotifyAccessTokenResponse.convert(): SpotifyAccessToken {
    return SpotifyAccessToken(
        accessToken = accessToken ?: "",
        expiresIn = expiresIn ?: 0
    )
}