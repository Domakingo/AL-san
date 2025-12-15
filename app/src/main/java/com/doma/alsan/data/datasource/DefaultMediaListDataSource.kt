package com.doma.alsan.data.datasource

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx3.rxSingle
import com.doma.alsan.DeleteMediaListEntryMutation
import com.doma.alsan.MediaListCollectionQuery
import com.doma.alsan.MediaListCollectionTrimmedQuery
import com.doma.alsan.MediaWithMediaListQuery
import com.doma.alsan.SaveMediaListEntryMutation
import com.doma.alsan.data.network.apollo.ApolloHandler
import com.doma.alsan.data.response.anilist.FuzzyDate
import com.doma.alsan.type.FuzzyDateInput
import com.doma.alsan.type.MediaListStatus
import com.doma.alsan.type.MediaType
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class DefaultMediaListDataSource(
    private val apolloHandler: ApolloHandler,
    private val statusVersion: Int,
    private val sourceVersion: Int
) : MediaListDataSource {

    override fun getMediaListCollectionQuery(
        userId: Int,
        mediaType: MediaType
    ): Observable<ApolloResponse<MediaListCollectionQuery.Data>> {
        val query = MediaListCollectionQuery(
            Optional.present(userId),
            Optional.present(mediaType),
            Optional.present(statusVersion),
            Optional.present(sourceVersion)
        )
        return apolloHandler.apolloClient.query(query).rxSingle().toObservable()
    }

    override fun getMediaListCollectionTrimmedQuery(
        userId: Int,
        mediaType: MediaType
    ): Observable<ApolloResponse<MediaListCollectionTrimmedQuery.Data>> {
        val query = MediaListCollectionTrimmedQuery(
            Optional.present(userId),
            Optional.present(mediaType),
            Optional.present(statusVersion),
            Optional.present(sourceVersion)
        )
        return apolloHandler.apolloClient.query(query).rxSingle().toObservable()
    }

    override fun getMediaWithMediaListQuery(
        mediaId: Int
    ): Observable<ApolloResponse<MediaWithMediaListQuery.Data>> {
        val query = MediaWithMediaListQuery(
            Optional.present(mediaId)
        )
        return apolloHandler.apolloClient.query(query).rxSingle().toObservable()
    }

    override fun updateMediaListEntry(
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
    ): Observable<ApolloResponse<SaveMediaListEntryMutation.Data>> {
        val mutation = SaveMediaListEntryMutation(
            id = Optional.presentIfNotNull(id),
            mediaId = Optional.presentIfNotNull(mediaId),
            status = Optional.present(status),
            score = Optional.present(score),
            progress = Optional.present(progress),
            progressVolumes = Optional.present(progressVolumes),
            repeat = Optional.present(repeat),
            priority = Optional.present(priority),
            isPrivate = Optional.present(isPrivate),
            notes = Optional.present(notes),
            hiddenFromStatusLists = Optional.present(hiddenFromStatusLists),
            customLists = Optional.present(customLists),
            advancedScores = Optional.present(advancedScores),
            startedAt = Optional.present(
                FuzzyDateInput(
                    year = Optional.present(startedAt?.year),
                    month = Optional.present(startedAt?.month),
                    day = Optional.present(startedAt?.day)
                )
            ),
            completedAt = Optional.present(
                FuzzyDateInput(
                    year = Optional.present(completedAt?.year),
                    month = Optional.present(completedAt?.month),
                    day = Optional.present(completedAt?.day)
                )
            )
        )
        return apolloHandler.apolloClient.mutation(mutation).rxSingle().toObservable()
    }

    override fun deleteMediaListEntry(id: Int): Completable {
        val mutation = DeleteMediaListEntryMutation(Optional.present(id))
        return apolloHandler.apolloClient.mutation(mutation).rxSingle().ignoreElement()
    }

    override fun updateMediaListScore(
        id: Int,
        score: Double,
        advancedScores: List<Double>?
    ): Observable<ApolloResponse<SaveMediaListEntryMutation.Data>> {
        val mutation = SaveMediaListEntryMutation(
            id = Optional.present(id),
            score = Optional.present(score),
            advancedScores = Optional.presentIfNotNull(advancedScores)
        )
        return apolloHandler.apolloClient.mutation(mutation).rxSingle().toObservable()
    }

    override fun updateMediaListProgress(
        id: Int,
        status: MediaListStatus?,
        repeat: Int?,
        progress: Int?,
        progressVolumes: Int?
    ): Observable<ApolloResponse<SaveMediaListEntryMutation.Data>> {
        val mutation = SaveMediaListEntryMutation(
            id = Optional.present(id),
            status = Optional.presentIfNotNull(status),
            repeat = Optional.presentIfNotNull(repeat),
            progress = Optional.presentIfNotNull(progress),
            progressVolumes = Optional.presentIfNotNull(progressVolumes)
        )
        return apolloHandler.apolloClient.mutation(mutation).rxSingle().toObservable()
    }

    override fun updateMediaListStatus(
        mediaId: Int,
        status: MediaListStatus
    ): Observable<ApolloResponse<SaveMediaListEntryMutation.Data>> {
        val mutation = SaveMediaListEntryMutation(
            mediaId = Optional.present(mediaId),
            status = Optional.present(status)
        )
        return apolloHandler.apolloClient.mutation(mutation).rxSingle().toObservable()
    }
}