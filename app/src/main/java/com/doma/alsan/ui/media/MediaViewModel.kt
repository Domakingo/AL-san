package com.doma.alsan.ui.media

import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.repository.BrowseRepository
import com.doma.alsan.data.repository.MediaListRepository
import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.data.response.Anime
import com.doma.alsan.data.response.Manga
import com.doma.alsan.data.response.anilist.AiringSchedule
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.MediaExternalLink
import com.doma.alsan.data.response.anilist.PageInfo
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.helper.enums.Source
import com.doma.alsan.helper.extensions.applyScheduler
import com.doma.alsan.helper.extensions.getMediaType
import com.doma.alsan.helper.extensions.getString
import com.doma.alsan.helper.extensions.getStringResource
import com.doma.alsan.helper.pojo.MediaItem
import com.doma.alsan.helper.pojo.NullableItem
import com.doma.alsan.helper.service.clipboard.ClipboardService
import com.doma.alsan.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlin.collections.*
import com.doma.alsan.data.response.Episode
import com.doma.alsan.type.MediaFormat
import com.doma.alsan.type.MediaListStatus
import com.doma.alsan.type.MediaRelation
import com.doma.alsan.type.MediaStatus
import com.doma.alsan.type.StaffLanguage
import com.doma.alsan.helper.pojo.ListItem
import com.doma.alsan.helper.extensions.getNonUnknownValues
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class MediaViewModel(
    private val browseRepository: BrowseRepository,
    private val userRepository: UserRepository,
    private val mediaListRepository: MediaListRepository,
    private val clipboardService: ClipboardService
) : BaseViewModel<MediaParam>() {

    private val _mediaAdapterComponent = PublishSubject.create<AppSetting>()
    val mediaAdapterComponent: Observable<AppSetting>
        get() = _mediaAdapterComponent

    private val _bannerImage = BehaviorSubject.createDefault("")
    val bannerImage: Observable<String>
        get() = _bannerImage

    private val _coverImage = BehaviorSubject.createDefault("")
    val coverImage: Observable<String>
        get() = _coverImage

    private val _mediaTitle = BehaviorSubject.createDefault("")
    val mediaTitle: Observable<String>
        get() = _mediaTitle

    private val _mediaYear = BehaviorSubject.createDefault("")
    val mediaYear: Observable<String>
        get() = _mediaYear

    private val _mediaYearVisibility = BehaviorSubject.createDefault(false)
    val mediaYearVisibility: Observable<Boolean>
        get() = _mediaYearVisibility

    private val _mediaFormat = BehaviorSubject.createDefault(NullableItem<MediaFormat>(null))
    val mediaFormat: Observable<NullableItem<MediaFormat>>
        get() = _mediaFormat

    private val _mediaLength = BehaviorSubject.createDefault(0 to MediaType.ANIME)
    val mediaLength: Observable<Pair<Int, MediaType>>
        get() = _mediaLength

    private val _mediaLengthVisibility = BehaviorSubject.createDefault(false)
    val mediaLengthVisibility: Observable<Boolean>
        get() = _mediaLengthVisibility

    private val _airingSchedule = BehaviorSubject.createDefault(NullableItem<AiringSchedule>(null))
    val airingSchedule: Observable<NullableItem<AiringSchedule>>
        get() = _airingSchedule

    private val _averageScore = BehaviorSubject.createDefault(0)
    val averageScore: Observable<Int>
        get() = _averageScore

    private val _favorites = BehaviorSubject.createDefault(0)
    val favorites: Observable<Int>
        get() = _favorites

    private val _addToListButtonText = BehaviorSubject.createDefault("")
    val addToListButtonText: Observable<String>
        get() = _addToListButtonText

    private val _mediaItemList = BehaviorSubject.createDefault(listOf<MediaItem>())
    val mediaItemList: Observable<List<MediaItem>>
        get() = _mediaItemList

    private val _coverImageUrlForPreview = PublishSubject.create<String>()
    val coverImageUrlForPreview: Observable<String>
        get() = _coverImageUrlForPreview

    private val _bannerImageUrlForPreview = PublishSubject.create<String>()
    val bannerImageUrlForPreview: Observable<String>
        get() = _bannerImageUrlForPreview

    private val _voiceActorLanguages = PublishSubject.create<List<ListItem<StaffLanguage>>>()
    val voiceActorLanguages: Observable<List<ListItem<StaffLanguage>>>
        get() = _voiceActorLanguages

    private val _pagedEpisodes = PublishSubject.create<Triple<List<Episode>, Int, Int>>()
    val pagedEpisodes: Observable<Triple<List<Episode>, Int, Int>>
        get() = _pagedEpisodes

    private val _mediaMetadata = BehaviorSubject.create<Media>()
    val mediaMetadata: Observable<Media>
        get() = _mediaMetadata

    private val _currentProgress = BehaviorSubject.createDefault(0)
    val currentProgress: Observable<Int>
        get() = _currentProgress

    private var availableLanguagesList: List<StaffLanguage>? = null

    private val _selectedLanguage = BehaviorSubject.createDefault(StaffLanguage.JAPANESE)
    val selectedLanguage: Observable<StaffLanguage>
        get() = _selectedLanguage

    private val _episodeCurrentPage = BehaviorSubject.createDefault(1)
    val episodeCurrentPage: Observable<Int>
        get() = _episodeCurrentPage

    val episodeCurrentPageValue: Int
        get() = _episodeCurrentPage.value ?: 1

    private val _episodeTotalPages = BehaviorSubject.createDefault(1)
    val episodeTotalPages: Observable<Int>
        get() = _episodeTotalPages

    val episodeTotalPagesValue: Int
        get() = _episodeTotalPages.value ?: 1

    enum class MediaTab(val stringRes: Int) {
        DETAILS(R.string.details),
        CHARACTERS(R.string.characters),
        EPISODES(R.string.episodes),
        STAFF(R.string.staff),
        RECOMMENDATIONS(R.string.recommendations)
    }

    private val _currentTab = BehaviorSubject.createDefault(MediaTab.DETAILS)
    val currentTab: Observable<MediaTab>
        get() = _currentTab

    private var currentMediaTabList = listOf<MediaTab>()

    private var mediaId = 0

    private var media = Media()
    private var appSetting = AppSetting()

    private var isCharactersLoaded = false
    private var isStaffLoaded = false
    private var isEpisodesLoaded = false

    private val mediaRelationPriority = mapOf(
        Pair(MediaRelation.SOURCE, 0),
        Pair(MediaRelation.ADAPTATION, 1),
        Pair(MediaRelation.PARENT, 2),
        Pair(MediaRelation.PREQUEL, 3),
        Pair(MediaRelation.SEQUEL, 4),
        Pair(MediaRelation.ALTERNATIVE, 5),
        Pair(MediaRelation.SIDE_STORY, 6),
        Pair(MediaRelation.SPIN_OFF, 7),
        Pair(MediaRelation.SUMMARY, 8),
        Pair(MediaRelation.COMPILATION, 9),
        Pair(MediaRelation.CONTAINS, 10),
        Pair(MediaRelation.CHARACTER, 11),
        Pair(MediaRelation.OTHER, 12)
    )

    override fun loadData(param: MediaParam) {
        loadOnce {
            mediaId = param.mediaId

            disposables.add(
                userRepository.getIsAuthenticated().zipWith(userRepository.getAppSetting()) { isAuthenticated, appSetting ->
                    return@zipWith isAuthenticated to appSetting
                }
                    .applyScheduler()
                    .subscribe { (isAuthenticated, appSetting) ->
                        this.appSetting = appSetting
                        _isAuthenticated.onNext(isAuthenticated)
                        _mediaAdapterComponent.onNext(appSetting)
                        loadMedia()
                    }
            )

            if (media.getId() != 0)
                checkMediaList()

            disposables.add(
                mediaListRepository.refreshMediaListTrigger
                    .applyScheduler()
                    .subscribe { (mediaType, mediaList) ->
                        _addToListButtonText.onNext(mediaList?.status?.getString(mediaType) ?: "")
                    }
            )
        }
    }

    fun reloadData() {
        loadMedia()
    }

    private fun checkMediaList() {
        if (media.getId() == 0)
            return

        if (_isAuthenticated.value != true) {
            _isAuthenticated.onNext(_isAuthenticated.value ?: false)
            return
        }

        disposables.add(
            userRepository.getViewer(Source.CACHE)
                .flatMap {
                    mediaListRepository.getMediaListCollection(Source.CACHE, it, media.type?.getMediaType() ?: MediaType.ANIME)
                }
                .applyScheduler()
                .subscribe(
                    { mediaListCollection ->
                        var itemFound = false

                        mediaListCollection.lists.forEach collection@{ mediaListGroup ->
                            mediaListGroup.entries.forEach { mediaList ->
                                if (mediaList.media.getId() == mediaId) {
                                    _addToListButtonText.onNext(mediaList.status?.getString(media.type?.getMediaType() ?: MediaType.ANIME) ?: "")
                                    this@MediaViewModel.media = this@MediaViewModel.media.copy(mediaListEntry = mediaList)
                                    _currentProgress.onNext(mediaList.progress ?: 0)
                                    _mediaItemList.onNext(_mediaItemList.value.orEmpty().map { it.copy(media = this@MediaViewModel.media) })
                                    itemFound = true
                                    return@collection
                                }
                            }
                        }

                        if (!itemFound) {
                            _addToListButtonText.onNext("")
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun fetchEpisodes(malId: Int, page: Int) {
        disposables.add(
            browseRepository.getAnimeEpisodes(malId, page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { episodesPair ->
                        var totalPages = episodesPair.second
                        val animeTotalEpisodes = this.media.episodes ?: (this.media.nextAiringEpisode?.episode?.minus(1)) ?: 0
                        
                        if (totalPages == 1 && (animeTotalEpisodes > 100 || (page == 1 && episodesPair.first.size >= 100))) {
                             val countToUse = if (animeTotalEpisodes > 100) animeTotalEpisodes else (if (episodesPair.first.size >= 100) 1000 else 0)
                             if (countToUse > 0) {
                                 totalPages = java.lang.Math.ceil(countToUse.toDouble() / 100.0).toInt()
                             }
                        }
                        
                        _episodeCurrentPage.onNext(page)
                        _episodeTotalPages.onNext(totalPages)
                        
                        if (episodesPair.first.isNotEmpty()) {
                            // Update local media object to reflect new episodes
                            media = media.copy(episodeList = episodesPair.first)
                            isEpisodesLoaded = true
                            
                            _pagedEpisodes.onNext(Triple(episodesPair.first, page, totalPages))
                            updateMediaItemList()
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    private fun loadMedia() {
        _loading.onNext(true)

        disposables.add(
            browseRepository.getMedia(mediaId)
                .flatMap { media ->
                    if (media.idMal == null)
                        return@flatMap Observable.just(media)

                    return@flatMap when (media.type) {
                        com.doma.alsan.type.MediaType.ANIME -> {
                            browseRepository.getAnimeDetails(media.idMal).onErrorReturn { Anime() }.map { anime ->
                                media.copy(
                                    openings = anime.openings,
                                    endings = anime.endings
                                )
                            }
                        }
                        com.doma.alsan.type.MediaType.MANGA -> {
                            browseRepository.getMangaDetails(media.idMal).onErrorReturn { Manga() }.map {
                                media.copy(mangaSerialization = it.serializations)
                            }
                        }
                        else -> {
                            Observable.just(media)
                        }
                    }
                }
                .applyScheduler()
                .doOnNext { media ->
                    // Emit basic info immediately for UI responsiveness
                    this.media = media
                    _mediaMetadata.onNext(media)
                    _bannerImage.onNext(media.bannerImage)
                    _coverImage.onNext(media.getCoverImage(appSetting))
                    _mediaTitle.onNext(media.getTitle(appSetting))
                    _mediaYear.onNext(media.startDate?.year?.toString() ?: "TBA")
                    _mediaYearVisibility.onNext(media.startDate?.year != null || media.status == MediaStatus.NOT_YET_RELEASED)
                    _mediaFormat.onNext(NullableItem(media.format))
                    
                    // Trigger pre-fetching in background
                    loadCharacters(false)
                    loadStaff(false)
                    if (media.idMal != null && media.type == com.doma.alsan.type.MediaType.ANIME) {
                        fetchEpisodes(media.idMal!!, 1, false)
                    }
                }
                .doFinally { _loading.onNext(false) }
                .map { media ->
                    media.relations.edges = media.relations.edges.sortedBy { mediaRelationPriority[it.relationType] ?: mediaRelationPriority.size }
                    
                    val mediaItemList = ArrayList<MediaItem>()

                    media to mediaItemList
                }
                .applyScheduler()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { (media, _) ->
                        this.media = media
                        _mediaMetadata.onNext(media)
                        
                        checkMediaList()
                        if (media.type == com.doma.alsan.type.MediaType.ANIME) {
                            calculateVoiceActorLanguages()
                        }
                        
                        updateMediaItemList()
                    },
                    { error ->
                        _loading.onNext(false)
                        error.printStackTrace()
                    }
                )
        )
    }

    fun setTab(tab: MediaTab) {
        if (_currentTab.value == tab) return
        _currentTab.onNext(tab)
        
        when (tab) {
            MediaTab.CHARACTERS -> if (!isCharactersLoaded) loadCharacters() else updateMediaItemList()
            MediaTab.STAFF -> if (!isStaffLoaded) loadStaff() else updateMediaItemList()
            MediaTab.EPISODES -> if (!isEpisodesLoaded) fetchEpisodes(media.idMal ?: 0, 1) else updateMediaItemList()
            else -> updateMediaItemList()
        }
    }

    private fun loadCharacters(updateUi: Boolean = true) {
        if (updateUi) _loading.onNext(true)
        disposables.add(
            browseRepository.getMediaCharacters(mediaId, 1, null)
                .applyScheduler()
                .doFinally { if (updateUi) _loading.onNext(false) }
                .subscribe(
                    { pagePair ->
                        media = media.copy(characters = media.characters.copy(edges = pagePair.second))
                        isCharactersLoaded = true
                        calculateVoiceActorLanguages()
                        if (updateUi || _currentTab.value == MediaTab.CHARACTERS) updateMediaItemList()
                    },
                    { it.printStackTrace() }
                )
        )
    }

    private fun loadStaff(updateUi: Boolean = true) {
        if (updateUi) _loading.onNext(true)
        disposables.add(
            browseRepository.getMediaStaff(mediaId, 1)
                .applyScheduler()
                .doFinally { if (updateUi) _loading.onNext(false) }
                .subscribe(
                    { pagePair ->
                        media = media.copy(staff = media.staff.copy(edges = pagePair.second))
                        isStaffLoaded = true
                        if (updateUi || _currentTab.value == MediaTab.STAFF) updateMediaItemList()
                    },
                    { it.printStackTrace() }
                )
        )
    }

    fun fetchEpisodes(malId: Int, page: Int, updateUi: Boolean = true) {
        if (updateUi) _loading.onNext(true)
        disposables.add(
            browseRepository.getAnimeEpisodes(malId, page)
                .applyScheduler()
                .doFinally { if (updateUi) _loading.onNext(false) }
                .subscribe(
                    { result ->
                        val episodesList = result.first
                        media = media.copy(episodeList = episodesList)
                        _episodeCurrentPage.onNext(page)
                        _episodeTotalPages.onNext(result.second)
                        isEpisodesLoaded = true
                        if (updateUi || _currentTab.value == MediaTab.EPISODES) updateMediaItemList()
                    },
                    { it.printStackTrace() }
                )
        )
    }

    fun updateMediaItemList() {
        val tab = _currentTab.value ?: return
        val mediaItemList = ArrayList<MediaItem>()

        when (tab) {
            MediaTab.EPISODES -> {
                if (media.episodeList?.isNotEmpty() == true) {
                    val episodes = media.episodeList!!.toMutableList()
                    // Move last watched episode to the top if possible
                    val currentProgress = _currentProgress.value ?: 0
                    if (currentProgress > 0) {
                        val lastWatchedIndex = episodes.indexOfFirst { it.number == currentProgress }
                        if (lastWatchedIndex != -1) {
                            val lastWatched = episodes.removeAt(lastWatchedIndex)
                            episodes.add(0, lastWatched)
                        }
                    }
                    
                    episodes.forEach { ep ->
                        mediaItemList.add(MediaItem(viewType = MediaItem.VIEW_TYPE_EPISODE_ITEM, episode = ep, isCurrent = ep.number == currentProgress))
                    }
                    
                    // Add pagination item at the end
                    val pagination = PageInfo(
                        total = 0,
                        perPage = 100,
                        currentPage = episodeCurrentPageValue,
                        lastPage = episodeTotalPagesValue,
                        hasNextPage = episodeCurrentPageValue < episodeTotalPagesValue
                    )
                    if (episodeTotalPagesValue > 1) {
                        mediaItemList.add(
                            MediaItem(
                                media = media,
                                viewType = MediaItem.VIEW_TYPE_EPISODE_PAGINATION,
                                pagination = pagination
                            )
                        )
                    }
                } else if (media.idMal != null && media.type == com.doma.alsan.type.MediaType.ANIME) {
                    fetchEpisodes(media.idMal!!, 1)
                }
            }
            MediaTab.CHARACTERS -> {
                if (media.type == com.doma.alsan.type.MediaType.ANIME) {
                    mediaItemList.add(MediaItem(viewType = MediaItem.VIEW_TYPE_CHARACTER_LANGUAGE))
                }
                media.characters.edges.forEach { edge ->
                    mediaItemList.add(MediaItem(viewType = MediaItem.VIEW_TYPE_CHARACTER_ITEM, characterEdge = edge))
                }
            }
            MediaTab.DETAILS -> {
                mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_INFO))

                if (media.genres.isNotEmpty()) {
                    mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_GENRE))
                }

                if (media.tags.isNotEmpty()) {
                    mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_TAGS))
                }

                media.openings?.let { openings ->
                    if (openings.isNotEmpty()) {
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_THEMES_OPENING, themeGroup = openings.firstOrNull()?.group ?: ""))
                    }
                }

                media.endings?.let { endings ->
                    if (endings.isNotEmpty()) {
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_THEMES_ENDING, themeGroup = endings.firstOrNull()?.group ?: ""))
                    }
                }
                
                mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_LINKS))
            }
            MediaTab.STAFF -> {
                media.staff.edges.forEach { edge ->
                    mediaItemList.add(MediaItem(viewType = MediaItem.VIEW_TYPE_STAFF_ITEM, staffEdge = edge))
                }
            }
            MediaTab.RECOMMENDATIONS -> {
                if (media.relations.edges.isNotEmpty())
                    mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_RELATIONS))

                if (media.recommendations.nodes.isNotEmpty())
                    mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_RECOMMENDATIONS))
            }
        }

        _mediaItemList.onNext(mediaItemList)
    }

    fun loadCoverImage() {
        if (media.coverImage.extraLarge.isNotBlank())
            _coverImageUrlForPreview.onNext(media.coverImage.extraLarge)
    }

    fun loadBannerImage() {
        if (media.bannerImage.isNotBlank())
            _bannerImageUrlForPreview.onNext(media.bannerImage)
    }


    fun updateShouldShowSpoilerTags(shouldShowSpoiler: Boolean) {
        val currentMediaListItems = _mediaItemList.value ?: return
        val tagsSectionIndex = currentMediaListItems.indexOfFirst { it.viewType == MediaItem.VIEW_TYPE_TAGS }
        if (tagsSectionIndex != -1) {
            currentMediaListItems[tagsSectionIndex].showSpoilerTags = shouldShowSpoiler
            _mediaItemList.onNext(currentMediaListItems)
        }
    }

    fun copyExternalLink(mediaExternalLink: MediaExternalLink) {
        disposables.add(
            clipboardService.copyPlainText(mediaExternalLink.url)
                .applyScheduler()
                .subscribe(
                    {
                        _success.onNext(R.string.link_copied)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun copyText(text: String, successMessage: Int = R.string.text_copied) {
        disposables.add(
            clipboardService.copyPlainText(text)
                .applyScheduler()
                .subscribe(
                    {
                        _success.onNext(successMessage)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun changeThemeGroup(viewType: Int, newGroup: String) {
        val currentMediaListItems = _mediaItemList.value ?: return
        val themeSectionIndex = currentMediaListItems.indexOfFirst { it.viewType == viewType }
        if (themeSectionIndex != -1) {
            currentMediaListItems[themeSectionIndex].themeGroup = newGroup
            _mediaItemList.onNext(currentMediaListItems)
        }
    }

    private fun calculateVoiceActorLanguages() {
        disposables.add(
            Observable.fromCallable {
                media.characters.edges
                    .flatMap { it.voiceActorRoles }
                    .mapNotNull { it.voiceActor.language.ifBlank { null } }
                    .distinct()
                    .mapNotNull { language ->
                        getNonUnknownValues<StaffLanguage>().find { 
                            it.name.equals(language.replace(" ", "_"), ignoreCase = true) 
                        }
                    }
                    .sortedBy { it.ordinal }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { languages ->
                availableLanguagesList = languages
            }
        )
    }

    fun loadVoiceActorLanguages() {
        availableLanguagesList?.let { languages ->
            _voiceActorLanguages.onNext(languages.map { ListItem(it.getString(), it) })
            return
        }

        calculateVoiceActorLanguages()
        availableLanguagesList?.let { languages ->
            _voiceActorLanguages.onNext(languages.map { ListItem(it.getString(), it) })
        }
    }

    fun updateVoiceActorLanguage(newLanguage: StaffLanguage) {
        _selectedLanguage.onNext(newLanguage)
        // Just notify the adapter to update the displayed voice actors
        // No need to reload media, the data is already there
    }

    fun getSelectedLanguage(): StaffLanguage = _selectedLanguage.value ?: StaffLanguage.JAPANESE

    fun getAvailableLanguages(): List<StaffLanguage> {
        return availableLanguagesList ?: listOf()
    }
}
