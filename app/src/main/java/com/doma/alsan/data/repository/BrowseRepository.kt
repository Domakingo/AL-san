package com.doma.alsan.data.repository

import com.doma.alsan.data.response.Anime
import com.doma.alsan.data.response.Manga
import com.doma.alsan.data.response.TrackSearch
import com.doma.alsan.data.response.VideoSearch
import com.doma.alsan.data.response.anilist.*
import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.CharacterEdge
import com.doma.alsan.data.response.anilist.ListActivity
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.MediaList
import com.doma.alsan.data.response.anilist.Page
import com.doma.alsan.data.response.anilist.PageInfo
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.data.response.anilist.StaffEdge
import com.doma.alsan.data.response.anilist.Studio
import com.doma.alsan.data.response.anilist.User
import com.doma.alsan.helper.enums.ListType
import com.doma.alsan.type.*
import io.reactivex.rxjava3.core.Observable

interface BrowseRepository {
    fun getUser(id: Int? = null, name: String? = null, sort: List<UserStatisticsSort> = listOf(UserStatisticsSort.COUNT_DESC)): Observable<User>
    fun getOthersListType(): Observable<ListType>
    fun updateOthersListType(newListType: ListType)
    fun getMedia(id: Int): Observable<Media>
    fun getMediaCharacters(id: Int, page: Int, language: StaffLanguage): Observable<Pair<PageInfo, List<CharacterEdge>>>
    fun getMediaStaff(id: Int, page: Int): Observable<Pair<PageInfo, List<StaffEdge>>>
    fun getMediaFollowingMediaList(id: Int, page: Int): Observable<Page<MediaList>>
    fun getMediaActivity(id: Int, page: Int): Observable<Page<ListActivity>>
    fun getCharacter(id: Int, page: Int, sort: List<MediaSort> = listOf(MediaSort.POPULARITY_DESC), type: MediaType? = null, onList: Boolean? = null): Observable<Character>
    fun getStaff(
        id: Int,
        page: Int,
        staffMediaSort: List<MediaSort> = listOf(MediaSort.POPULARITY_DESC),
        characterSort: List<CharacterSort> = listOf(CharacterSort.FAVOURITES_DESC),
        characterMediaSort: List<MediaSort> = listOf(MediaSort.POPULARITY_DESC),
        onList: Boolean? = null
    ): Observable<Staff>
    fun getStudio(id: Int, page: Int, sort: List<MediaSort> = listOf(MediaSort.POPULARITY_DESC), onList: Boolean? = null): Observable<Studio>

    fun getMangaDetails(malId: Int): Observable<Manga>
    fun getAnimeDetails(malId: Int): Observable<Anime>
    fun getYouTubeVideo(searchQuery: String): Observable<VideoSearch>
    fun getSpotifyTrack(searchQuery: String): Observable<TrackSearch>
}