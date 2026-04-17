package com.doma.alsan.data.repository

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.entity.CalendarSetting
import com.doma.alsan.helper.enums.AppTheme
import com.doma.alsan.helper.enums.Source
import com.doma.alsan.data.response.NotificationData
import com.doma.alsan.data.response.anilist.Favourites
import com.doma.alsan.data.response.anilist.ListActivityOption
import com.doma.alsan.data.response.anilist.MediaListTypeOptions
import com.doma.alsan.data.response.anilist.NotificationOption
import com.doma.alsan.data.response.anilist.Page
import com.doma.alsan.data.response.anilist.User
import com.doma.alsan.data.response.anilist.UserStatisticTypes
import com.doma.alsan.helper.enums.Favorite
import com.doma.alsan.type.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface UserRepository {

    val refreshFavoriteTrigger: Observable<User>
    val unreadNotificationCount: Observable<Int>

    fun getIsLoggedInAsGuest(): Observable<Boolean>
    fun getIsAuthenticated(): Observable<Boolean>
    fun getViewer(
        source: Source? = null,
        sort: List<UserStatisticsSort> = listOf(UserStatisticsSort.COUNT_DESC)
    ): Observable<User>
    fun loginAsGuest()
    fun logoutAsGuest()
    fun logout()
    fun saveBearerToken(newBearerToken: String?)

    fun getFollowingAndFollowersCount(
        userId: Int,
        source: Source? = null
    ): Observable<Pair<Int, Int>>

    fun getFollowing(userId: Int, page: Int): Observable<Page<User>>
    fun getFollowers(userId: Int, page: Int): Observable<Page<User>>
    fun toggleFollow(userId: Int): Observable<Boolean>

    fun getUserStatistics(userId: Int, sort: UserStatisticsSort): Observable<UserStatisticTypes>
    fun getFavorites(userId: Int, page: Int): Observable<Favourites>
    fun updateFavoriteOrder(ids: List<Int>, favorite: Favorite): Observable<Favourites>
    fun toggleFavorite(
        animeId: Int? = null,
        mangaId: Int? = null,
        characterId: Int? = null,
        staffId: Int? = null,
        studioId: Int? = null
    ): Completable

    fun getAppSetting(): Observable<AppSetting>
    fun setAppSetting(newAppSetting: AppSetting?): Observable<Unit>

    fun getCalendarSetting(): Observable<CalendarSetting>
    fun setCalendarSetting(newCalendarSetting: CalendarSetting): Observable<Unit>

    fun getAppTheme(): AppTheme
    fun getFontSize(): com.doma.alsan.helper.enums.AppFontSize

    fun updateAniListSettings(
        titleLanguage: UserTitleLanguage,
        staffNameLanguage: UserStaffNameLanguage,
        activityMergeTime: Int,
        displayAdultContent: Boolean,
        airingNotifications: Boolean
    ): Observable<User>

    fun updateListSettings(
        scoreFormat: ScoreFormat,
        rowOrder: String,
        animeListOptions: MediaListTypeOptions,
        mangaListOptions: MediaListTypeOptions,
        disabledListActivity: List<ListActivityOption>
    ): Observable<User>

    fun updateNotificationsSettings(
        notificationOptions: List<NotificationOption>
    ): Observable<User>

    fun getNotifications(
        page: Int,
        typeIn: List<NotificationType>?,
        resetNotificationCount: Boolean
    ): Observable<NotificationData>

    fun getLatestUnreadNotificationCount(): Observable<Int>

    fun clearUnreadNotificationCount()

    fun getLastNotificationId(): Observable<Int>
    fun setLastNotificationId(lastNotificationId: Int)
}