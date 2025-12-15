package com.doma.alsan.data.datasource

import com.apollographql.apollo3.api.ApolloResponse
import com.doma.alsan.MediaListCollectionQuery
import com.doma.alsan.MediaListCollectionTrimmedQuery
import com.doma.alsan.MediaWithMediaListQuery
import com.doma.alsan.SaveMediaListEntryMutation
import com.doma.alsan.data.response.anilist.FuzzyDate
import com.doma.alsan.type.MediaListStatus
import com.doma.alsan.type.MediaType
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface MediaListDataSource {
    fun getMediaListCollectionQuery(userId: Int, mediaType: MediaType): Observable<ApolloResponse<MediaListCollectionQuery.Data>>
    fun getMediaListCollectionTrimmedQuery(userId: Int, mediaType: MediaType): Observable<ApolloResponse<MediaListCollectionTrimmedQuery.Data>>
    fun getMediaWithMediaListQuery(mediaId: Int): Observable<ApolloResponse<MediaWithMediaListQuery.Data>>
    fun updateMediaListEntry(
        id: Int?,
        mediaId: Int?,
        status: MediaListStatus,
        score: Double,
        progress: Int,
        progressVolumes: Int?,
        repeat: Int,
        priority: Int,
        isPrivate: Boolean,
        notes: String,
        hiddenFromStatusLists: Boolean,
        customLists: List<String>?,
        advancedScores: List<Double>?,
        startedAt: FuzzyDate?,
        completedAt: FuzzyDate?
    ): Observable<ApolloResponse<SaveMediaListEntryMutation.Data>>
    fun deleteMediaListEntry(
        id: Int
    ): Completable
    fun updateMediaListScore(
        id: Int,
        score: Double,
        advancedScores: List<Double>?
    ): Observable<ApolloResponse<SaveMediaListEntryMutation.Data>>
    fun updateMediaListProgress(
        id: Int,
        status: MediaListStatus?,
        repeat: Int?,
        progress: Int?,
        progressVolumes: Int?
    ): Observable<ApolloResponse<SaveMediaListEntryMutation.Data>>
    fun updateMediaListStatus(
        mediaId: Int,
        status: MediaListStatus
    ): Observable<ApolloResponse<SaveMediaListEntryMutation.Data>>
}