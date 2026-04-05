package com.doma.alsan.data.network.retrofit

import com.doma.alsan.data.response.mal.AnimeResponse
import com.doma.alsan.data.response.mal.EpisodeListResponse
import com.doma.alsan.data.response.mal.MangaResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanRestService {

    @GET("manga/{mangaMalId}/full")
    fun getMangaDetails(@Path("mangaMalId") mangaMalId: Int): Observable<MangaResponse>

    @GET("anime/{animeMalId}/full")
    fun getAnimeDetails(@Path("animeMalId") animeMalId: Int): Observable<AnimeResponse>

    @GET("anime/{animeMalId}/episodes")
    fun getAnimeEpisodes(@Path("animeMalId") animeMalId: Int, @Query("page") page: Int): Observable<EpisodeListResponse>
}