package com.doma.alsan.data.repository

import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.response.HomeData
import com.doma.alsan.helper.enums.Source
import com.doma.alsan.data.response.Genre
import com.doma.alsan.data.response.anilist.*
import com.doma.alsan.helper.enums.ReviewSort
import com.doma.alsan.helper.enums.Sort
import com.doma.alsan.type.MediaSeason
import com.doma.alsan.type.MediaType
import com.doma.alsan.type.ReviewRating
import com.doma.alsan.type.UserTitleLanguage
import io.reactivex.rxjava3.core.Observable

interface ContentRepository {
    fun getHomeData(source: Source? = null): Observable<HomeData>
    fun getGenres(): Observable<List<Genre>>
    fun getTags(): Observable<List<MediaTag>>
    fun searchMedia(searchQuery: String, type: MediaType, mediaFilter: MediaFilter?, page: Int): Observable<Page<Media>>
    fun searchCharacter(searchQuery: String, page: Int): Observable<Page<Character>>
    fun searchStaff(searchQuery: String, page: Int): Observable<Page<Staff>>
    fun searchStudio(searchQuery: String, page: Int): Observable<Page<Studio>>
    fun searchUser(searchQuery: String, page: Int): Observable<Page<User>>
    fun getSeasonal(page: Int, year: Int, season: MediaSeason, sort: Sort, titleLanguage: UserTitleLanguage, orderByDescending: Boolean, onlyShowOnList: Boolean?, showAdult: Boolean): Observable<Page<Media>>
    fun getAiringSchedule(page: Int, airingAtGreater: Int, airingAtLesser: Int): Observable<Page<AiringSchedule>>
    fun getReviews(mediaId: Int?, userId: Int?, mediaType: MediaType?, sort: ReviewSort, page: Int): Observable<Page<Review>>
    fun rateReview(id: Int, rating: ReviewRating): Observable<Review>
}