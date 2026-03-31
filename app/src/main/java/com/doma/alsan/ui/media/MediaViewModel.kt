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

    private var availableLanguagesList: List<StaffLanguage>? = null

    private val _selectedLanguage = BehaviorSubject.createDefault(StaffLanguage.JAPANESE)
    val selectedLanguage: Observable<StaffLanguage>
        get() = _selectedLanguage

    private var mediaId = 0

    private var media = Media()
    private var appSetting = AppSetting()

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

    private fun loadMedia() {
        _loading.onNext(true)

        disposables.add(
            browseRepository.getMedia(mediaId)
                .flatMap { media ->
                    if (media.idMal == null)
                        return@flatMap Observable.just(media)

                    return@flatMap when (media.type) {
                        com.doma.alsan.type.MediaType.ANIME -> {
                            browseRepository.getAnimeDetails(media.idMal).onErrorReturn { Anime() }.map {
                                media.copy(openings = it.openings, endings = it.endings)
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
                .doFinally { _loading.onNext(false) }
                .map { media ->
                    media.relations.edges = media.relations.edges.sortedBy { mediaRelationPriority[it.relationType] ?: mediaRelationPriority.size }
                    
                    val mediaItemList = ArrayList<MediaItem>()

                    if (media.genres.isNotEmpty())
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_GENRE))

                    if (media.description.isNotBlank())
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_SYNOPSIS))

                    if (media.characters.edges.isNotEmpty())
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_CHARACTERS))

                    mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_INFO))

                    if (media.tags.isNotEmpty())
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_TAGS))

                    if (media.openings?.isNotEmpty() == true)
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_THEMES_OPENING, themeGroup = media.openings.firstOrNull()?.group ?: ""))

                    if (media.endings?.isNotEmpty() == true)
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_THEMES_ENDING, themeGroup = media.endings.firstOrNull()?.group ?: ""))

                    if (media.staff.edges.isNotEmpty())
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_STAFF))

                    if (media.relations.edges.isNotEmpty())
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_RELATIONS))

                    if (media.recommendations.nodes.isNotEmpty())
                        mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_RECOMMENDATIONS))

                    mediaItemList.add(MediaItem(media, MediaItem.VIEW_TYPE_LINKS))
                    
                    media to mediaItemList
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { (media, mediaItemList) ->
                        this.media = media

                        checkMediaList()

                        _bannerImage.onNext(media.bannerImage)
                        _coverImage.onNext(media.getCoverImage(appSetting))
                        _mediaTitle.onNext(media.getTitle(appSetting))
                        _mediaYear.onNext(media.startDate?.year?.toString() ?: "TBA")
                        _mediaYearVisibility.onNext(media.startDate?.year != null || media.status == MediaStatus.NOT_YET_RELEASED)
                        _mediaFormat.onNext(NullableItem(media.format))
                        _mediaLength.onNext((media.getLength() ?: 0) to (media.type?.getMediaType() ?: MediaType.ANIME))
                        _mediaLengthVisibility.onNext(media.getLength() != null && media.getLength() != 0)
                        _airingSchedule.onNext(NullableItem(media.nextAiringEpisode))

                        _averageScore.onNext(media.averageScore)
                        _favorites.onNext(media.favourites)
                        
                        // Pre-calculate available languages for characters
                        if (media.type == com.doma.alsan.type.MediaType.ANIME) {
                            calculateVoiceActorLanguages()
                        }

                        _mediaItemList.onNext(mediaItemList)
                    },
                    {
                        _error.onNext(it.getStringResource())
                    }
                )
        )
    }

    fun loadCoverImage() {
        if (media.coverImage.extraLarge.isNotBlank())
            _coverImageUrlForPreview.onNext(media.coverImage.extraLarge)
    }

    fun loadBannerImage() {
        if (media.bannerImage.isNotBlank())
            _bannerImageUrlForPreview.onNext(media.bannerImage)
    }

    fun updateShouldShowFullDescription(shouldShowFullDescription: Boolean) {
        val currentMediaListItems = _mediaItemList.value ?: return
        val descriptionSectionIndex = currentMediaListItems.indexOfFirst { it.viewType == MediaItem.VIEW_TYPE_SYNOPSIS }
        if (descriptionSectionIndex != -1) {
            currentMediaListItems[descriptionSectionIndex].showFullDescription = shouldShowFullDescription
            _mediaItemList.onNext(currentMediaListItems)
        }
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

    fun copyText(text: String) {
        disposables.add(
            clipboardService.copyPlainText(text)
                .applyScheduler()
                .subscribe(
                    {
                        _success.onNext(R.string.text_copied)
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
                        StaffLanguage.values().find { 
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