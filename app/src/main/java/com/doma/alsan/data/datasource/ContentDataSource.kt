package com.doma.alsan.data.datasource

import com.apollographql.apollo3.api.ApolloResponse
import com.doma.alsan.*
import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.helper.enums.ReviewSort
import com.doma.alsan.helper.enums.Sort
import com.doma.alsan.type.MediaSeason
import com.doma.alsan.type.MediaType
import com.doma.alsan.type.ReviewRating
import com.doma.alsan.type.UserTitleLanguage
import io.reactivex.rxjava3.core.Observable

interface ContentDataSource {
    fun getHomeQuery(): Observable<ApolloResponse<HomeDataQuery.Data>>
    fun getGenres(): Observable<ApolloResponse<GenreQuery.Data>>
    fun getTags(): Observable<ApolloResponse<TagQuery.Data>>
    fun searchMedia(searchQuery: String, type: MediaType, mediaFilter: MediaFilter?, page: Int): Observable<ApolloResponse<SearchMediaQuery.Data>>
    fun searchCharacter(searchQuery: String, page: Int): Observable<ApolloResponse<SearchCharacterQuery.Data>>
    fun searchStaff(searchQuery: String, page: Int): Observable<ApolloResponse<SearchStaffQuery.Data>>
    fun searchStudio(searchQuery: String, page: Int): Observable<ApolloResponse<SearchStudioQuery.Data>>
    fun searchUser(searchQuery: String, page: Int): Observable<ApolloResponse<SearchUserQuery.Data>>
    fun getSeasonal(
        page: Int,
        year: Int,
        season: MediaSeason,
        sort: Sort,
        titleLanguage: UserTitleLanguage,
        orderByDescending: Boolean,
        onlyShowOnList: Boolean?,
        showAdult: Boolean
    ): Observable<ApolloResponse<SearchMediaQuery.Data>>

    fun getAiringSchedule(page: Int, airingAtGreater: Int, airingAtLesser: Int): Observable<ApolloResponse<AiringScheduleQuery.Data>>

    fun getReviews(mediaId: Int?, userId: Int?, mediaType: MediaType?, sort: ReviewSort, page: Int): Observable<ApolloResponse<ReviewQuery.Data>>
    fun rateReview(id: Int, rating: ReviewRating): Observable<ApolloResponse<RateReviewMutation.Data>>
}