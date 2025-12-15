package com.doma.alsan.data.manager

import android.net.Uri
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.entity.CalendarSetting
import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.response.anilist.User
import com.doma.alsan.data.entity.ListStyle
import com.doma.alsan.data.response.anilist.MediaListCollection
import com.doma.alsan.helper.pojo.NullableItem
import com.doma.alsan.helper.pojo.SaveItem
import io.reactivex.rxjava3.core.Observable

interface UserManager {
    var bearerToken: String?
    val isAuthenticated: Boolean
    var isLoggedInAsGuest: Boolean

    var animeListStyle: ListStyle
    var mangaListStyle: ListStyle
    var animeFilter: MediaFilter
    var mangaFilter: MediaFilter
    var appSetting: AppSetting
    var calendarSetting: CalendarSetting

    val animeListBackground: Observable<NullableItem<Uri>>
    val mangaListBackground: Observable<NullableItem<Uri>>
    fun saveAnimeListBackground(uri: Uri?): Observable<Unit>
    fun saveMangaListBackground(uri: Uri?): Observable<Unit>

    var viewerData: SaveItem<User>?
    var followingCount: Int?
    var followersCount: Int?
    var animeListEntryCount: Int?
    var mangaListEntryCount: Int?

    var animeList: SaveItem<MediaListCollection>?
    var mangaList: SaveItem<MediaListCollection>?

    var lastNotificationId: Int?

    var lastAnnouncementId: String?
}