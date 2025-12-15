package com.doma.alsan.data.network.retrofit

import com.doma.alsan.data.response.spotify.SpotifyAccessTokenResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SpotifyAuthRestService {

    @FormUrlEncoded
    @POST("token")
    fun getAccessToken(@Field("grant_type") grantType: String = "client_credentials"): Observable<SpotifyAccessTokenResponse>
}