package com.doma.alsan.data.localstorage

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.entity.CalendarSetting
import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.entity.ListStyle
import com.doma.alsan.data.response.SpotifyAccessToken
import com.doma.alsan.helper.enums.ListType

interface SharedPreferencesHandler {
    var bearerToken: String?
    var guestLogin: Boolean?
    var animeListStyle: ListStyle?
    var mangaListStyle: ListStyle?
    var animeFilter: MediaFilter?
    var mangaFilter: MediaFilter?
    var appSetting: AppSetting?
    var calendarSetting: CalendarSetting?
    var followingCount: Int?
    var followersCount: Int?
    var animeListEntryCount: Int?
    var mangaListEntryCount: Int?
    var othersListType: ListType?
    var lastNotificationId: Int?
    var lastAnnouncementId: String?
    var spotifyAccessToken: SpotifyAccessToken?
    var spotifyAccessTokenLastRetrieve: Long?
}