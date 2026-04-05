package com.doma.alsan.data.repository

import com.doma.alsan.data.converter.convert
import com.doma.alsan.data.datasource.BrowseDataSource
import com.doma.alsan.data.manager.BrowseManager
import com.doma.alsan.data.response.Anime
import com.doma.alsan.data.response.Episode
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
import com.doma.alsan.helper.utils.AnimeThemesException
import com.doma.alsan.helper.utils.TimeUtil
import com.doma.alsan.type.*
import convert
import java.util.HashMap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*

class DefaultBrowseRepository(
    private val browseDataSource: BrowseDataSource,
    private val browseManager: BrowseManager
) : BrowseRepository {

    private val userIdToUserMap = HashMap<Int, User>()

    override fun getUser(id: Int?, name: String?, sort: List<UserStatisticsSort>): Observable<User> {
        return if (userIdToUserMap.containsKey(id)) {
            Observable.just(userIdToUserMap[id] ?: User())
        } else {
            browseDataSource.getUserQuery(id, name, sort).map {
                val newUser = it.data?.convert()
                if (newUser != null) {
                    userIdToUserMap[newUser.id] = newUser
                }
                newUser ?: User()
            }
        }
    }

    override fun getOthersListType(): Observable<ListType> {
        return Observable.just(browseManager.othersListType)
    }

    override fun updateOthersListType(newListType: ListType) {
        browseManager.othersListType = newListType
    }

    override fun getMedia(id: Int): Observable<Media> {
        return browseDataSource.getMediaQuery(id).map {
            it.data?.convert() ?: Media()
        }
    }

    override fun getMediaCharacters(
        id: Int,
        page: Int,
        language: StaffLanguage?
    ): Observable<Pair<PageInfo, List<CharacterEdge>>> {
        return browseDataSource.getMediaCharactersQuery(id, page, language).map {
            val characterConnection = it.data?.convert() ?: return@map Pair(PageInfo(), listOf())
            characterConnection.pageInfo to characterConnection.edges
        }
    }

    override fun getMediaStaff(id: Int, page: Int): Observable<Pair<PageInfo, List<StaffEdge>>> {
        return browseDataSource.getMediaStaffQuery(id, page).map {
            val staffConnection = it.data?.convert() ?: return@map Pair(PageInfo(), listOf())
            staffConnection.pageInfo to staffConnection.edges
        }
    }

    override fun getMediaFollowingMediaList(id: Int, page: Int): Observable<Page<MediaList>> {
        return browseDataSource.getMediaFollowingMediaListQuery(id, page).map {
            it.data?.convert() ?: Page()
        }
    }

    override fun getMediaActivity(id: Int, page: Int): Observable<Page<ListActivity>> {
        return browseDataSource.getMediaActivityQuery(id, page).map {
            it.data?.convert() ?: Page()
        }
    }

    override fun getCharacter(id: Int, page: Int, sort: List<MediaSort>, type: MediaType?, onList: Boolean?): Observable<Character> {
        return browseDataSource.getCharacterQuery(id, page, sort, type, onList).map {
            it.data?.convert() ?: Character()
        }
    }

    override fun getStaff(
        id: Int,
        page: Int,
        staffMediaSort: List<MediaSort>,
        characterSort: List<CharacterSort>,
        characterMediaSort: List<MediaSort>,
        onList: Boolean?
    ): Observable<Staff> {
        return browseDataSource.getStaffQuery(id, page, staffMediaSort, characterSort, characterMediaSort, onList).map {
            it.data?.convert() ?: Staff()
        }
    }

    override fun getStudio(id: Int, page: Int, sort: List<MediaSort>, onList: Boolean?): Observable<Studio> {
        return browseDataSource.getStudioQuery(id, page, sort, onList).map {
            it.data?.convert() ?: Studio()
        }
    }

    override fun getMangaDetails(malId: Int): Observable<Manga> {
        return browseDataSource.getMangaDetails(malId).map {
            it.convert()
        }
    }

    override fun getAnimeDetails(malId: Int): Observable<Anime> {
        var getFromMal = false
        return Observable.just(true)
            .flatMap {
                if (!getFromMal) {
                    getAnimeDetailsFromAnimeThemes(malId)
                        .doOnError { getFromMal = true }
                        .map {
                            if (it.id == 0)
                                throw AnimeThemesException()
                            else
                                it
                        }
                } else {
                    getFromMal = false
                    getAnimeDetailsFromMal(malId)
                }
            }
            .retry { times, throwable ->
                if (throwable is AnimeThemesException) {
                    getFromMal = true
                    true
                } else {
                    false
                }
            }
    }

    private fun getAnimeDetailsFromAnimeThemes(malId: Int): Observable<Anime> {
        return browseDataSource.getAnimeDetailsFromAnimeThemes(malId).map {
            it.convert()
        }
    }

    private fun getAnimeDetailsFromMal(malId: Int): Observable<Anime> {
        return browseDataSource.getAnimeDetailsFromMal(malId).map {
            it.convert()
        }
    }

    override fun getAnimeEpisodes(malId: Int, page: Int, fetchAll: Boolean): Observable<Pair<List<Episode>, Int>> {
        android.util.Log.d("AL-san-EP", "Fetching episodes for malId: $malId, page: $page")
        return browseDataSource.getAnimeEpisodes(malId, page)
            .subscribeOn(Schedulers.io())
            .flatMap { response ->
                val currentEpisodes = mapEpisodes(response.data)
                android.util.Log.d("AL-san-EP", "Pagination object present: ${response.pagination != null}")
                android.util.Log.d("AL-san-EP", "lastVisiblePage: ${response.pagination?.lastVisiblePage}, hasNextPage: ${response.pagination?.hasNextPage}")
                val totalPages = response.pagination?.lastVisiblePage ?: page
                android.util.Log.d("AL-san-EP", "Fetched ${currentEpisodes.size} episodes for malId: $malId, page: $page. Final totalPages calculated: $totalPages")
                if (fetchAll && response.pagination?.hasNextPage == true) {
                    getAnimeEpisodes(malId, page + 1, fetchAll)
                        .map { (nextEpisodes, _) -> (currentEpisodes + nextEpisodes) to totalPages }
                } else {
                    Observable.just(currentEpisodes to totalPages)
                }
            }
    }

    private fun mapEpisodes(data: List<com.doma.alsan.data.response.mal.EpisodeDataResponse>?): List<Episode> {
        return data?.map { ep ->
            Episode(
                number = ep.malId ?: 0,
                title = ep.title ?: "",
                titleJapanese = ep.titleJapanese ?: "",
                titleRomanji = ep.titleRomanji ?: "",
                aired = ep.aired ?: "",
                filler = ep.filler ?: false,
                recap = ep.recap ?: false,
                url = ep.url ?: ""
            )
        } ?: listOf()
    }

    override fun getYouTubeVideo(searchQuery: String): Observable<VideoSearch> {
        return browseDataSource.getYouTubeVideo(browseManager.youTubeApiKey, searchQuery).map {
            it.convert()
        }
    }

    override fun getSpotifyTrack(searchQuery: String): Observable<TrackSearch> {
        return Observable.just(browseManager.spotifyAccessToken)
            .flatMap { accessToken ->
                if (accessToken.accessToken == "" || TimeUtil.getCurrentTimeInMillis() >= browseManager.spotifyAccessTokenLastRetrieve + accessToken.expiresIn.toLong() * 1000 ) {
                    browseDataSource.getSpotifyAccessToken()
                        .map {
                            browseManager.spotifyAccessToken = it.convert()
                            browseManager.spotifyAccessTokenLastRetrieve = TimeUtil.getCurrentTimeInMillis()
                            Unit
                        }
                } else {
                    Observable.just(Unit)
                }
            }
            .flatMap {
                browseDataSource.getSpotifyTrack(searchQuery).map { it.convert() }
            }
    }
}