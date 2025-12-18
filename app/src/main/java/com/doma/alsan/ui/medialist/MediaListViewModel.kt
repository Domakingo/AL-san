package com.doma.alsan.ui.medialist

import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.repository.MediaListRepository
import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.data.response.anilist.*
import com.doma.alsan.helper.extensions.*
import com.doma.alsan.helper.pojo.ListItem
import com.doma.alsan.data.entity.ListStyle
import com.doma.alsan.data.repository.BrowseRepository
import com.doma.alsan.helper.enums.*
import com.doma.alsan.helper.pojo.MediaListAdapterComponent
import com.doma.alsan.helper.pojo.MediaListItem
import com.doma.alsan.helper.pojo.CollapsedSeriesGroup
import com.doma.alsan.helper.service.clipboard.ClipboardService
import com.doma.alsan.helper.utils.TimeUtil
import com.doma.alsan.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import com.doma.alsan.type.MediaListStatus
import com.doma.alsan.type.MediaStatus
import com.doma.alsan.type.ScoreFormat
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class MediaListViewModel(
    private val mediaListRepository: MediaListRepository,
    private val userRepository: UserRepository,
    private val browseRepository: BrowseRepository,
    private val clipboardService: ClipboardService
) : BaseViewModel<Unit>() {

    private val _toolbarTitle = BehaviorSubject.createDefault(R.string.anime_list)
    val toolbarTitle: Observable<Int>
        get() = _toolbarTitle

    private val _toolbarSubtitle = BehaviorSubject.createDefault("")
    val toolbarSubtitle: Observable<String>
        get() = _toolbarSubtitle

    private val _menuItemCustomiseListVisibility = BehaviorSubject.createDefault(false)
    val menuItemCustomiseListVisibility: Observable<Boolean>
        get() = _menuItemCustomiseListVisibility

    private val _menuItemChangeListTypeVisibility = BehaviorSubject.createDefault(false)
    val menuItemChangeListTypeVisibility: Observable<Boolean>
        get() = _menuItemChangeListTypeVisibility

    private val _mediaListAdapterComponent = PublishSubject.create<MediaListAdapterComponent>()
    val mediaListAdapterComponent: Observable<MediaListAdapterComponent>
        get() = _mediaListAdapterComponent

    private val _mediaListItems = BehaviorSubject.createDefault<List<MediaListItem>>(listOf())
    val mediaListItems: Observable<List<MediaListItem>>
        get() = _mediaListItems

    private val _listSections = PublishSubject.create<List<ListItem<String>>>()
    val listSections: Observable<List<ListItem<String>>>
        get() = _listSections

    private val _scoreValues = PublishSubject.create<Pair<MediaList, ScoreFormat>>()
    val scoreValues: Observable<Pair<MediaList, ScoreFormat>>
        get() = _scoreValues

    private val _progressValues = PublishSubject.create<Pair<MediaList, Boolean>>()
    val progressValues: Observable<Pair<MediaList, Boolean>>
        get() = _progressValues

    private val _setToWatchingDialog = PublishSubject.create<Triple<MediaList, Int, Boolean>>()
    val setToWatchingDialog: Observable<Triple<MediaList, Int, Boolean>> // media list, new progress, isProgressVolume
        get() = _setToWatchingDialog

    private val _setToCompletedDialog = PublishSubject.create<Triple<MediaList, Int, Boolean>>()
    val setToCompletedDialog: Observable<Triple<MediaList, Int, Boolean>> // media list, new progress, isProgressVolume
        get() = _setToCompletedDialog

    private val _listTypes = PublishSubject.create<List<ListItem<ListType>>>()
    val listTypes: Observable<List<ListItem<ListType>>>
        get() = _listTypes

    private val _randomMedia = PublishSubject.create<Media>()
    val randomMedia: Observable<Media>
        get() = _randomMedia

    private val _isCollapsedMode = BehaviorSubject.createDefault(false)
    val isCollapsedMode: Observable<Boolean>
        get() = _isCollapsedMode

    private val _fabOptions = PublishSubject.create<List<ListItem<String>>>()
    val fabOptions: Observable<List<ListItem<String>>>
        get() = _fabOptions

    var mediaType: MediaType = MediaType.ANIME
    var userId = 0

    var isViewer = true
    var user = User()
    var appSetting = AppSetting()
    var listStyle = ListStyle()
    var mediaFilter = MediaFilter()
    var hasBigList = false
    private var isAllListPositionAtTop = true

    private var rawMediaListCollection: MediaListCollection? = null // needed when applying filter
    private var currentMediaListCollection: MediaListCollection? = null // needed to show number of entries in each section
    private var currentMediaListItems: List<MediaListItem> = listOf() // needed for search

    private var selectedSectionIndex = 0
    private var searchKeyword = ""

    override fun loadData(param: Unit) {
        loadOnce {
            isViewer = userId == 0

            _loading.onNext(true)

            _toolbarTitle.onNext(
                when (mediaType) {
                    MediaType.ANIME -> R.string.anime_list
                    MediaType.MANGA -> R.string.manga_list
                }
            )

            disposables.add(
                userRepository.getIsAuthenticated()
                    .filter { it }
                    .flatMap {
                        Observable.zip(
                            mediaListRepository.getListStyle(MediaType.valueOf(mediaType.name)),
                            userRepository.getAppSetting(),
                            mediaListRepository.getMediaFilter(mediaType),
                            if (isViewer) userRepository.getViewer(Source.CACHE) else browseRepository.getUser(userId),
                            browseRepository.getOthersListType()
                        ) { listStyle, appSetting, mediaFilter, user, othersListType ->
                            this.listStyle = if (isViewer) listStyle else ListStyle.getOthersListStyle(othersListType)
                            this.appSetting = appSetting
                            this.mediaFilter = if (isViewer) mediaFilter else MediaFilter(false)
                            isAllListPositionAtTop = when (mediaType) {
                                MediaType.ANIME -> appSetting.isAllAnimeListPositionAtTop
                                MediaType.MANGA -> appSetting.isAllMangaListPositionAtTop
                            }
                            return@zip user
                        }
                    }
                    .zipWith(mediaListRepository.getListBackground(mediaType)) { user, backgroundUri ->
                        return@zipWith user to backgroundUri
                    }
                    .applyScheduler()
                    .subscribe { (user, backgroundUri) ->
                        if (userId == 0)
                            userId = user.id

                        this.user = user

                        _menuItemCustomiseListVisibility.onNext(isViewer)
                        _menuItemChangeListTypeVisibility.onNext(!isViewer)

                        _mediaListAdapterComponent.onNext(
                            MediaListAdapterComponent(
                                isViewer,
                                listStyle,
                                appSetting,
                                user.mediaListOptions,
                                if (isViewer) backgroundUri.data else null
                            )
                        )

                        getMediaListCollection(state == State.LOADED || state == State.ERROR)
                    }
            )

            disposables.add(
                mediaListRepository.refreshMediaListTrigger
                    .filter { it.first == mediaType }
                    .applyScheduler()
                    .subscribe { (mediaType, newMediaList) ->
                        if (newMediaList == null) {
                            reloadData()
                        } else {
                            _loading.onNext(true)

                            // get all the index of the modified MediaList
                            var previousMediaList: MediaList? = null
                            val mediaListGroupIndex = ArrayList<Int>()
                            val mediaListIndex = ArrayList<Int>()
                            rawMediaListCollection?.lists?.forEachIndexed { groupIndex, mediaListGroup ->
                                mediaListGroup.entries.forEachIndexed { listIndex, mediaList ->
                                    if (mediaList.id == newMediaList.id) {
                                        mediaListGroupIndex.add(groupIndex)
                                        mediaListIndex.add(listIndex)
                                        previousMediaList = mediaList
                                    }
                                }
                            }

                            // reload if it's a new entry or when the status is changed or when the visibility is changed
                            if (previousMediaList == null ||
                                previousMediaList?.status != newMediaList.status ||
                                previousMediaList?.hiddenFromStatusLists != newMediaList.hiddenFromStatusLists
                            ) {
                                reloadData()
                                return@subscribe
                            }

                            // reload if the custom lists is changed
                            val oldCustomLists = previousMediaList?.customLists as? LinkedHashMap<String, Boolean>
                            val newCustomLists = newMediaList.customLists as? LinkedHashMap<String, Boolean>
                            newCustomLists?.forEach { (key, value) ->
                                if (oldCustomLists?.get(key) != value) {
                                    reloadData()
                                    return@subscribe
                                }
                            }

                            // modify the collection with the new MediaList
                            mediaListGroupIndex.zip(mediaListIndex).forEach { (groupIndex, listIndex) ->
                                rawMediaListCollection?.lists?.get(groupIndex)?.entries?.get(listIndex)?.apply {
                                    status = newMediaList.status
                                    score = newMediaList.score
                                    progress = newMediaList.progress
                                    progressVolumes = newMediaList.progressVolumes
                                    repeat = newMediaList.repeat
                                    priority = newMediaList.priority
                                    private = newMediaList.private
                                    notes = newMediaList.notes
                                    hiddenFromStatusLists = newMediaList.hiddenFromStatusLists
                                    customLists = newMediaList.customLists
                                    advancedScores = newMediaList.advancedScores
                                    startedAt = newMediaList.startedAt
                                    completedAt = newMediaList.completedAt
                                    updatedAt = newMediaList.updatedAt
                                    createdAt = newMediaList.createdAt
                                }
                            }

                            // emit the change
                            rawMediaListCollection?.let {
                                _mediaListItems.onNext(getFilteredAndSortedList(it))

                                if (searchKeyword.isNotBlank())
                                    filterByText(searchKeyword)

                                mediaListRepository.updateCacheMediaList(mediaType, it)
                            }

                            _loading.onNext(false)
                        }
                    }
            )


        }
    }

    fun updateListStyle(newListStyle: ListStyle) {
        listStyle = newListStyle

        disposables.add(
            mediaListRepository.getListBackground(mediaType)
                .applyScheduler()
                .subscribe { uri ->
                    _mediaListAdapterComponent.onNext(
                        MediaListAdapterComponent(
                            isViewer,
                            listStyle,
                            appSetting,
                            user.mediaListOptions,
                            uri.data
                        )
                    )

                    _mediaListItems.value?.let {
                        _mediaListItems.onNext(it)
                    }
                }
        )
    }

    fun updateMediaFilter(newFilter: MediaFilter) {
        mediaFilter = newFilter

        rawMediaListCollection?.let {
            val filteredAndSortedList = getFilteredAndSortedList(it)
            _mediaListItems.onNext(filteredAndSortedList)

            if (searchKeyword.isNotBlank())
                filterByText(searchKeyword)
        }
    }

    fun reloadData() {
        getMediaListCollection(true)
    }

    fun loadListSections() {
        currentMediaListCollection?.lists?.let { groups ->
            val sections = ArrayList<ListItem<String>>()
            var totalEntries = 0
            val listFromCurrentGroups = groups.map {
                totalEntries += it.entries.size
                val formattedTitle = "${it.name} (${it.entries.size})"
                ListItem(formattedTitle, formattedTitle)
            }
            sections.addAll(listFromCurrentGroups)

            val allListItem = ListItem("All ($totalEntries)", "All")
            if (isAllListPositionAtTop) {
                sections.add(0, allListItem)
            } else {
                sections.add(allListItem)
            }

            _listSections.onNext(sections)
        }
    }

    fun loadFabOptions() {
        val options = ArrayList<ListItem<String>>()
        
        // Add list sections
        currentMediaListCollection?.lists?.let { groups ->
            var totalEntries = 0
            val listFromCurrentGroups = groups.mapIndexed { index, group ->
                totalEntries += group.entries.size
                val formattedTitle = "${group.name} (${group.entries.size})"
                // Calculate expected index: if "All" is at top, section indices are offset by 1
                val expectedIndex = if (isAllListPositionAtTop) index + 1 else index
                val isSelected = selectedSectionIndex == expectedIndex
                ListItem(formattedTitle, "section:$index", isSelected)
            }
            
            // Check if "All" is selected
            val allSelectedIndex = if (isAllListPositionAtTop) 0 else groups.size
            val isAllSelected = selectedSectionIndex == allSelectedIndex
            val allListItem = ListItem("All ($totalEntries)", "section:all", isAllSelected)
            
            if (isAllListPositionAtTop) {
                options.add(allListItem)
                options.addAll(listFromCurrentGroups)
            } else {
                options.addAll(listFromCurrentGroups)
                options.add(allListItem)
            }
        }
        
        // Add Advanced Options at the end (in a centered card)
        options.add(ListItem(R.string.advanced_options, "advanced_options", isHighlighted = true, useCardLayout = true))
        
        _fabOptions.onNext(options)
    }

    fun showSelectedSectionMediaList(index: Int) {
        val currentGroups = currentMediaListCollection?.lists ?: listOf()
        selectedSectionIndex = index
        val mediaListItems = getMediaListItems(currentGroups, index)
        _mediaListItems.onNext(mediaListItems)

        if (searchKeyword.isNotBlank())
            filterByText(searchKeyword)
    }

    fun filterByText(query: String) {
        searchKeyword = query
        val filteredMediaListItems = ArrayList<MediaListItem>()

        var isLastItemTitle = false
        currentMediaListItems.forEachIndexed { index, mediaListItem ->
            if (mediaListItem.viewType == MediaListItem.VIEW_TYPE_TITLE) {
                if (isLastItemTitle) {
                    filteredMediaListItems.removeAt(filteredMediaListItems.lastIndex)
                }
                filteredMediaListItems.add(mediaListItem)
                isLastItemTitle = true
            } else if (
                mediaListItem.mediaList.media.title.romaji.contains(query, true) ||
                mediaListItem.mediaList.media.title.english.contains(query, true) ||
                mediaListItem.mediaList.media.title.native.contains(query, true) ||
                mediaListItem.mediaList.media.synonyms.find { synonym -> synonym.contains(query, true) } != null ||
                mediaListItem.mediaList.notes.contains(query, true)
            ) {
                filteredMediaListItems.add(mediaListItem)
                isLastItemTitle = false
            } else if (index == currentMediaListItems.lastIndex && isLastItemTitle) {
                filteredMediaListItems.removeAt(filteredMediaListItems.lastIndex)
            }
        }

        _mediaListItems.onNext(filteredMediaListItems)
    }

    fun pickRandomPlanningMedia() {
        val collection = rawMediaListCollection
        if (collection == null) return

        val planningEntries = ArrayList<MediaList>()
        collection.lists.forEach { group ->
            group.entries.forEach { entry ->
                if (entry.status == MediaListStatus.PLANNING && entry.media.status != MediaStatus.NOT_YET_RELEASED) {
                    planningEntries.add(entry)
                }
            }
        }

        if (planningEntries.isNotEmpty()) {
            _randomMedia.onNext(planningEntries.random().media)
        } else {
            // Optional: notify error
        }
    }

    private fun getMediaListCollection(isReloading: Boolean = false) {
        if (isReloading)
            _loading.onNext(true)

        disposables.add(
            mediaListRepository.getMediaListCollection(Source.NETWORK, user, mediaType)
                .applyScheduler()
                .doFinally { _loading.onNext(false) }
                .subscribe(
                    { mediaListCollection ->
                        rawMediaListCollection = mediaListCollection
                        val filteredAndSortedList = getFilteredAndSortedList(mediaListCollection)
                        _mediaListItems.onNext(filteredAndSortedList)

                        if (searchKeyword.isNotBlank())
                            filterByText(searchKeyword)

                        if (mediaType == MediaType.ANIME)
                            mediaListRepository.triggerReleasingToday()
                        
                        state = State.LOADED

                        disposables.add(
                            mediaListRepository.hasBigList(user, mediaType)
                                .applyScheduler()
                                .subscribe(
                                    {
                                        this.hasBigList = it
                                    },
                                    {
                                        it.printStackTrace()
                                    }
                                )
                        )
                    },
                    {
                        getMediaListCollectionFromCache()
                    }
                )
        )
    }

    /**
     * Purposely not handled in Repository because need to inform user when loaded from cache
     */
    private fun getMediaListCollectionFromCache() {
        disposables.add(
            mediaListRepository.getMediaListCollection(Source.CACHE, user, mediaType)
                .applyScheduler()
                .subscribe(
                    {
                        rawMediaListCollection = it
                        val filteredAndSortedList = getFilteredAndSortedList(it)
                        _mediaListItems.onNext(filteredAndSortedList)

                        if (searchKeyword.isNotBlank())
                            filterByText(searchKeyword)

                        _success.onNext(
                            when (mediaType) {
                                MediaType.ANIME -> R.string.anime_list_is_loaded_from_cache
                                MediaType.MANGA -> R.string.manga_list_is_loaded_from_cache
                            }
                        )

                        state = State.LOADED
                    },
                    {
                        _error.onNext(it.getStringResource())
                        state = State.ERROR
                    }
                )
        )
    }

    private fun getFilteredAndSortedList(mediaListCollection: MediaListCollection): List<MediaListItem> {
        val list = ArrayList<MediaListItem>()

        val groupWithSortedAndFilteredEntries = ArrayList<MediaListGroup>()
        mediaListCollection.lists.forEach { mediaListGroup ->
            val sortedEntries = getSortedEntries(mediaListGroup.entries)
            val filteredEntries = getFilteredEntries(sortedEntries)
            groupWithSortedAndFilteredEntries.add(mediaListGroup.copy(entries = filteredEntries))
        }
        val sortedGroups = getSortedGroups(groupWithSortedAndFilteredEntries)
        list.addAll(getMediaListItems(sortedGroups, selectedSectionIndex))

        return list
    }

    private fun getSortedEntries(entries: List<MediaList>): List<MediaList> {
        if (entries.isEmpty()) return listOf()

        val rowOrder = try {
            ListOrder.values().find { it.value == user.mediaListOptions.rowOrder } ?: ListOrder.TITLE
        } catch (e: IllegalArgumentException) {
            ListOrder.TITLE
        }

        val entriesSortedByTitle = entries.sortedBy { it.media.getTitle(appSetting) }
        val isDescending = mediaFilter.orderByDescending

        return when (mediaFilter.sort) {
            Sort.TITLE -> if (isDescending) entriesSortedByTitle.reversed() else entriesSortedByTitle
            Sort.SCORE -> sortUsing(entriesSortedByTitle, isDescending) { score }
            Sort.PROGRESS -> sortUsing(entriesSortedByTitle, isDescending) { progress }
            Sort.LAST_UPDATED -> sortUsing(entriesSortedByTitle, isDescending) { updatedAt }
            Sort.LAST_ADDED -> sortUsing(entriesSortedByTitle, isDescending) { id ?: 0 }
            Sort.START_DATE -> sortUsing(entriesSortedByTitle, isDescending) { TimeUtil.getMillisFromFuzzyDate(startedAt) }
            Sort.COMPLETED_DATE -> sortUsing(entriesSortedByTitle, isDescending) { TimeUtil.getMillisFromFuzzyDate(completedAt) }
            Sort.RELEASE_DATE -> sortUsing(entriesSortedByTitle, isDescending) { TimeUtil.getMillisFromFuzzyDate(media.startDate) }
            Sort.AVERAGE_SCORE -> sortUsing(entriesSortedByTitle, isDescending) { media.averageScore }
            Sort.POPULARITY -> sortUsing(entriesSortedByTitle, isDescending) { media.popularity }
            Sort.FAVORITES -> sortUsing(entriesSortedByTitle, isDescending) { media.favourites }
            Sort.TRENDING -> sortUsing(entriesSortedByTitle, isDescending) { media.trending }
            Sort.PRIORITY -> sortUsing(entriesSortedByTitle, isDescending) { priority }
            Sort.NEXT_AIRING -> {
                val defaultValueForNullAiringTime = if (isDescending) Int.MIN_VALUE else Int.MAX_VALUE
                sortUsing(entriesSortedByTitle, isDescending) { media.nextAiringEpisode?.timeUntilAiring ?: defaultValueForNullAiringTime }
            }
            else -> {
                when (rowOrder) {
                    ListOrder.SCORE -> sortUsing(entriesSortedByTitle, true) { score }
                    ListOrder.TITLE -> entriesSortedByTitle
                    ListOrder.LAST_UPDATED -> sortUsing(entriesSortedByTitle, true) { updatedAt }
                    ListOrder.LAST_ADDED -> sortUsing(entriesSortedByTitle, true) { id ?: 0 }
                }
            }
        }
    }

    private fun <T : Comparable<T>> sortUsing(unsortedList: List<MediaList>, sortByDescending: Boolean, comparison: MediaList.() -> T): List<MediaList> {
        return if (sortByDescending) {
            unsortedList.sortedByDescending { it.comparison() }
        } else {
            unsortedList.sortedBy { it.comparison() }
        }
    }


    private fun getFilteredEntries(entries: List<MediaList>): List<MediaList> {
        if (entries.isEmpty()) return listOf()

        val filterEntries = ArrayList(entries)

        if (mediaFilter.mediaFormats.isNotEmpty())
            filterEntries.removeAll { !mediaFilter.mediaFormats.contains(it.media.format) }

        if (mediaFilter.mediaStatuses.isNotEmpty())
            filterEntries.removeAll { !mediaFilter.mediaStatuses.contains(it.media.status) }

        if (mediaFilter.mediaSources.isNotEmpty())
            filterEntries.removeAll { !mediaFilter.mediaSources.contains(it.media.source) }

        if (mediaFilter.countries.isNotEmpty())
            filterEntries.removeAll { !mediaFilter.countries.map { it.iso } .contains(it.media.countryOfOrigin) }

        if (mediaFilter.mediaSeasons.isNotEmpty())
            filterEntries.removeAll { !mediaFilter.mediaSeasons.contains(it.media.season) }

        if (mediaFilter.minYear != null)
            filterEntries.removeAll { it.media.startDate?.year == null || mediaFilter.minYear!! > it.media.startDate.year }

        if (mediaFilter.maxYear != null)
            filterEntries.removeAll { it.media.startDate?.year == null || mediaFilter.maxYear!! < it.media.startDate.year }

        if (mediaFilter.minEpisodes != null) {
            filterEntries.removeAll {
                val episodes = when (mediaType) {
                    MediaType.ANIME -> it.media.episodes
                    MediaType.MANGA -> it.media.chapters
                }
                episodes == null || mediaFilter.minEpisodes!! > episodes
            }
        }

        if (mediaFilter.maxEpisodes != null) {
            filterEntries.removeAll {
                val episodes = when (mediaType) {
                    MediaType.ANIME -> it.media.episodes
                    MediaType.MANGA -> it.media.chapters
                }
                episodes == null || mediaFilter.maxEpisodes!! < episodes
            }
        }

        if (mediaFilter.minDuration != null) {
            filterEntries.removeAll {
                val durations = when (mediaType) {
                    MediaType.ANIME -> it.media.duration
                    MediaType.MANGA -> it.media.volumes
                }
                durations == null || mediaFilter.minDuration!! > durations
            }
        }

        if (mediaFilter.maxDuration != null) {
            filterEntries.removeAll {
                val durations = when (mediaType) {
                    MediaType.ANIME -> it.media.duration
                    MediaType.MANGA -> it.media.volumes
                }
                durations == null || mediaFilter.maxDuration!! < durations
            }
        }

        if (mediaFilter.minAverageScore != null)
            filterEntries.removeAll { mediaFilter.minAverageScore!! > it.media.averageScore }

        if (mediaFilter.maxAverageScore != null)
            filterEntries.removeAll { mediaFilter.maxAverageScore!! < it.media.averageScore }

        if (mediaFilter.minPopularity != null)
            filterEntries.removeAll { mediaFilter.minPopularity!! > it.media.popularity }

        if (mediaFilter.maxPopularity != null)
            filterEntries.removeAll { mediaFilter.maxPopularity!! < it.media.popularity }

        if (mediaFilter.streamingOn.isNotEmpty()) {
            filterEntries.removeAll { mediaList ->
                !mediaFilter.streamingOn
                    .map {
                        it.id
                    }
                    .any { id ->
                        mediaList.media.externalLinks
                            .map { it.siteId }
                            .contains(id)
                    }
            }
        }

        if (mediaFilter.includedGenres.isNotEmpty()) {
            filterEntries.removeAll { mediaList ->
                !mediaFilter.includedGenres
                    .map {
                        it.lowercase()
                    }
                    .any { genre ->
                        mediaList.media.genres
                            .map { it.name.lowercase() }
                            .contains(genre)
                    }
            }
        }

        if (mediaFilter.excludedGenres.isNotEmpty()) {
            filterEntries.removeAll { mediaList ->
                mediaFilter.excludedGenres
                    .map {
                        it.lowercase()
                    }
                    .any { genre ->
                        mediaList.media.genres
                            .map { it.name.lowercase() }
                            .contains(genre)
                    }
            }
        }

        if (mediaFilter.includedTags.isNotEmpty()) {
            filterEntries.removeAll { mediaList ->
                !mediaFilter.includedTags
                    .map {
                        it.id
                    }
                    .any { tag ->
                        mediaList.media.tags
                            .filter { it.rank > mediaFilter.minTagPercentage }
                            .map { it.id }
                            .contains(tag)
                    }
            }
        }

        if (mediaFilter.excludedTags.isNotEmpty()) {
            filterEntries.removeAll { mediaList ->
                mediaFilter.excludedTags
                    .map {
                        it.id
                    }
                    .any { tag ->
                        mediaList.media.tags
                            .filter { it.rank > mediaFilter.minTagPercentage }
                            .map { it.id }
                            .contains(tag)
                    }
            }
        }

        if (mediaFilter.minUserScore != null)
            filterEntries.removeAll { mediaFilter.minUserScore!! > it.score }

        if (mediaFilter.maxUserScore != null)
            filterEntries.removeAll { mediaFilter.maxUserScore!! < it.score }

        if (mediaFilter.minUserStartYear != null)
            filterEntries.removeAll { it.startedAt?.year == null || mediaFilter.minUserStartYear!! > it.startedAt?.year!! }

        if (mediaFilter.maxUserStartYear != null)
            filterEntries.removeAll { it.startedAt?.year == null || mediaFilter.maxUserStartYear!! < it.startedAt?.year!! }

        if (mediaFilter.minUserCompletedYear != null)
            filterEntries.removeAll { it.completedAt?.year == null || mediaFilter.minUserCompletedYear!! > it.completedAt?.year!! }

        if (mediaFilter.maxUserCompletedYear != null)
            filterEntries.removeAll { it.completedAt?.year == null || mediaFilter.maxUserCompletedYear!! < it.completedAt?.year!! }

        if (mediaFilter.minUserPriority != null) {
            filterEntries.removeAll { mediaFilter.minUserPriority!! > it.priority }
        }

        if (mediaFilter.maxUserPriority != null) {
            filterEntries.removeAll { mediaFilter.maxUserPriority!! < it.priority }
        }

        if (mediaFilter.isDoujin != null) {
            filterEntries.removeAll { mediaFilter.isDoujin == it.media.isLicensed }
        }

        return filterEntries
    }

    private fun getSortedGroups(groups: List<MediaListGroup>): List<MediaListGroup> {
        val (sectionOrder, customList, defaultList) = when (mediaType) {
            MediaType.ANIME -> {
                Triple(
                    user.mediaListOptions.animeList.sectionOrder,
                    user.mediaListOptions.animeList.customLists,
                    if (user.mediaListOptions.animeList.splitCompletedSectionByFormat)
                        mediaListRepository.defaultAnimeListSplitCompletedSectionByFormat
                    else
                        mediaListRepository.defaultAnimeList
                )
            }
            MediaType.MANGA -> {
                Triple(
                    user.mediaListOptions.mangaList.sectionOrder,
                    user.mediaListOptions.mangaList.customLists,
                    if (user.mediaListOptions.mangaList.splitCompletedSectionByFormat)
                        mediaListRepository.defaultMangaListSplitCompletedSectionByFormat
                    else
                        mediaListRepository.defaultMangaList
                )
            }
        }

        val normalizedGroups = mutableSetOf<MediaListGroup>()

        sectionOrder.forEach { section ->
            val group = groups.find { it.name == section }
            if (group != null) normalizedGroups.add(group)
        }

        customList.forEach { custom ->
            val group = groups.find { it.name == custom && it.isCustomList }
            if (group != null) { normalizedGroups.add(group) }
        }

        defaultList.forEach { default ->
            val group = groups.find { it.name == default && !it.isCustomList }
            if (group != null) normalizedGroups.add(group)
        }

        currentMediaListCollection = MediaListCollection(normalizedGroups.toList())

        return normalizedGroups.toList()
    }

    private fun getMediaListItems(groups: List<MediaListGroup>, index: Int = 0): List<MediaListItem> {
        val list = ArrayList<MediaListItem>()
        val isCollapsed = _isCollapsedMode.value ?: false

        val isAllList = if (isAllListPositionAtTop) {
            index == 0
        } else {
            index == groups.size
        }

        if (isAllList) {
            groups.forEach { group ->
                if (group.entries.isNotEmpty()) {
                    list.add(MediaListItem(title = group.name, viewType = MediaListItem.VIEW_TYPE_TITLE))
                    
                    // Apply collapse grouping only to Completed section
                    val isCompletedSection = group.status == MediaListStatus.COMPLETED ||
                        group.name.equals("Completed", ignoreCase = true) ||
                        group.name.contains("Completed", ignoreCase = true)
                    
                    if (isCollapsed && isCompletedSection) {
                        list.addAll(groupMediaByFranchise(group.entries))
                    } else {
                        list.addAll(group.entries.map { MediaListItem(mediaList = it, viewType = MediaListItem.VIEW_TYPE_MEDIA_LIST) })
                    }
                }
            }

            val itemCount = list.count { 
                it.viewType == MediaListItem.VIEW_TYPE_MEDIA_LIST || 
                it.viewType == MediaListItem.VIEW_TYPE_COLLAPSED_GROUP 
            }
            _toolbarSubtitle.onNext("All ($itemCount)")
        } else {
            // "All" list is just for display, not actually stored
            // It does not exist in "groups"
            // That is why we should calculate the actual index without "All" list
            var selectedIndex = if (isAllListPositionAtTop) index - 1 else index
            if (selectedIndex >= groups.size)
                selectedIndex = groups.lastIndex
            
            val selectedGroup = groups[selectedIndex]
            
            // Apply collapse grouping only to Completed section
            val isCompletedSection = selectedGroup.status == MediaListStatus.COMPLETED ||
                selectedGroup.name.equals("Completed", ignoreCase = true) ||
                selectedGroup.name.contains("Completed", ignoreCase = true)
            
            if (isCollapsed && isCompletedSection) {
                list.addAll(groupMediaByFranchise(selectedGroup.entries))
            } else {
                list.addAll(selectedGroup.entries.map { MediaListItem(mediaList = it, viewType = MediaListItem.VIEW_TYPE_MEDIA_LIST) })
            }

            _toolbarSubtitle.onNext("${selectedGroup.name} (${list.size})")
        }

        currentMediaListItems = list

        return list
    }

    fun loadScoreValues(mediaList: MediaList) {
        val scoreFormat = user.mediaListOptions.scoreFormat ?: ScoreFormat.POINT_100
        _scoreValues.onNext(mediaList to scoreFormat)
    }

    fun updateScore(mediaList: MediaList, newScore: Double, newAdvancedScores: LinkedHashMap<String, Double>?) {
        _loading.onNext(true)
        disposables.add(
            mediaListRepository.updateMediaListScore(mediaType, mediaList.id ?: 0, newScore, newAdvancedScores?.map { it.value })
                .applyScheduler()
                .doFinally {
                    _loading.onNext(false)
                }
                .subscribe(
                    {
                        // do nothing
                    },
                    {
                        _error.onNext(it.getStringResource())
                    }
                )
        )
    }

    fun loadProgressValues(mediaList: MediaList, isProgressVolume: Boolean) {
        _progressValues.onNext(mediaList to isProgressVolume)
    }

    fun updateProgress(mediaList: MediaList, newProgress: Int, isProgressVolume: Boolean) {
        val currentProgress = when (mediaList.media.type) {
            com.doma.alsan.type.MediaType.ANIME -> mediaList.progress
            com.doma.alsan.type.MediaType.MANGA -> if (isProgressVolume) (mediaList.progressVolumes ?: 0) else mediaList.progress
            else -> 0
        }

        if (currentProgress == newProgress)
            return

        val maxProgress = when (mediaList.media.type) {
            com.doma.alsan.type.MediaType.ANIME -> mediaList.media.episodes
            com.doma.alsan.type.MediaType.MANGA -> if (isProgressVolume) mediaList.media.volumes else mediaList.media.chapters
            else -> null
        }

        if (maxProgress != null && newProgress >= maxProgress) {
            if (mediaList.status != MediaListStatus.COMPLETED) {
                _setToCompletedDialog.onNext(Triple(mediaList, newProgress, isProgressVolume))
                return
            }
        } else {
            if (mediaList.status == MediaListStatus.PLANNING ||
                mediaList.status == MediaListStatus.PAUSED ||
                mediaList.status == MediaListStatus.DROPPED
            ) {
                _setToWatchingDialog.onNext(Triple(mediaList, newProgress, isProgressVolume))
                return
            }
        }

        updateProgress(mediaList.id ?: 0, null, null, newProgress, isProgressVolume)
    }

    fun updateProgress(mediaList: MediaList, status: MediaListStatus?, newProgress: Int, isProgressVolume: Boolean) {
        val repeat = if (status == MediaListStatus.COMPLETED && mediaList.status == MediaListStatus.REPEATING) {
            mediaList.repeat + 1
        } else
            null

        updateProgress(mediaList.id ?: 0, status ?: mediaList.status, repeat,  newProgress, isProgressVolume)
    }

    private fun updateProgress(mediaListId: Int, status: MediaListStatus?, repeat: Int?, progress: Int, isProgressVolume: Boolean) {
        _loading.onNext(true)

        disposables.add(
            mediaListRepository.updateMediaListProgress(
                mediaType,
                mediaListId,
                status,
                repeat,
                if (isProgressVolume) null else progress,
                if (isProgressVolume) progress else null
            )
                .applyScheduler()
                .doFinally {
                    _loading.onNext(false)
                }
                .subscribe(
                    {
                        // do nothing
                    },
                    {
                        _error.onNext(it.getStringResource())
                    }
                )
        )
    }

    fun loadListTypes() {
        _listTypes.onNext(ListType.values().map { ListItem(it.getString(), it) })
    }

    fun updateListType(newListType: ListType) {
        browseRepository.updateOthersListType(newListType)

        _mediaListAdapterComponent.onNext(
            MediaListAdapterComponent(
                isViewer,
                listStyle.copy(listType = newListType),
                appSetting,
                user.mediaListOptions,
                null
            )
        )

        _mediaListItems.value?.let {
            _mediaListItems.onNext(it)
        }
    }

    fun copyText(text: String) {
        disposables.add(
            clipboardService.copyPlainText(text)
                .applyScheduler()
                .subscribe {
                    _success.onNext(R.string.text_copied)
                }
        )
    }

    /**
     * Toggles between collapsed and expanded view for the Completed section.
     * In collapsed mode, related series (sequels, prequels, etc.) are grouped together
     * and show the average score across all entries.
     */
    fun toggleCollapsedMode() {
        val newValue = !(_isCollapsedMode.value ?: false)
        _isCollapsedMode.onNext(newValue)
        
        rawMediaListCollection?.let {
            val filteredAndSortedList = getFilteredAndSortedList(it)
            _mediaListItems.onNext(filteredAndSortedList)
            
            if (searchKeyword.isNotBlank())
                filterByText(searchKeyword)
        }
    }

    /**
     * Groups related media entries by their franchise using the relations data.
     * Uses a Union-Find approach to group all related media together.
     */
    private fun groupMediaByFranchise(entries: List<MediaList>): List<MediaListItem> {
        if (entries.isEmpty()) return listOf()
        
        val mediaMap = entries.associateBy { it.media.idAniList }
        val parent = mutableMapOf<Int, Int>()
        
        fun find(id: Int): Int {
            if (parent[id] == null) parent[id] = id
            if (parent[id] != id) parent[id] = find(parent[id]!!)
            return parent[id]!!
        }
        
        fun union(id1: Int, id2: Int) {
            val root1 = find(id1)
            val root2 = find(id2)
            if (root1 != root2) {
                parent[root1] = root2
            }
        }
        
        entries.forEach { parent[it.media.idAniList] = it.media.idAniList }
        
        entries.forEach { mediaList ->
            mediaList.media.relations.edges.forEach { edge ->
                val relatedId = edge.node.idAniList
                if (mediaMap.containsKey(relatedId)) {
                    union(mediaList.media.idAniList, relatedId)
                }
            }
        }
        
        val groups = mutableMapOf<Int, MutableList<MediaList>>()
        entries.forEach { mediaList ->
            val root = find(mediaList.media.idAniList)
            groups.getOrPut(root) { mutableListOf() }.add(mediaList)
        }
        
        val result = mutableListOf<MediaListItem>()
        groups.values.forEach { groupEntries ->
            if (groupEntries.size == 1) {
                result.add(MediaListItem(
                    mediaList = groupEntries.first(),
                    viewType = MediaListItem.VIEW_TYPE_MEDIA_LIST
                ))
            } else {
                val scoredEntries = groupEntries.filter { it.score > 0 }
                val averageScore = if (scoredEntries.isNotEmpty()) {
                    scoredEntries.map { it.score }.average()
                } else {
                    0.0
                }
                
                // Use the earliest entry as representative (usually the first season)
                val representative = groupEntries.minByOrNull { 
                    it.media.startDate?.let { date -> 
                        (date.year ?: 9999) * 10000 + (date.month ?: 12) * 100 + (date.day ?: 31)
                    } ?: Int.MAX_VALUE
                } ?: groupEntries.first()
                
                val franchiseName = representative.media.getTitle(appSetting)
                    .replace(Regex("\\s*(Season|Part|Cour|S)\\s*\\d+.*", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("\\s*\\d+(st|nd|rd|th)\\s*(Season|Part|Cour).*", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("\\s*[IVX]+$"), "") // Roman numerals at end
                    .trim()
                
                val collapsedGroup = CollapsedSeriesGroup(
                    mediaLists = groupEntries.sortedBy { 
                        it.media.startDate?.let { date -> 
                            (date.year ?: 9999) * 10000 + (date.month ?: 12) * 100 + (date.day ?: 31)
                        } ?: Int.MAX_VALUE
                    },
                    franchiseName = if (franchiseName.isNotBlank()) franchiseName else representative.media.getTitle(appSetting),
                    averageScore = averageScore,
                    totalEntries = groupEntries.size,
                    representativeMedia = representative
                )
                
                result.add(MediaListItem(
                    mediaList = representative,
                    viewType = MediaListItem.VIEW_TYPE_COLLAPSED_GROUP,
                    collapsedGroup = collapsedGroup
                ))
            }
        }
        
        // Apply sorting based on user's selected sort option
        val isDescending = mediaFilter.orderByDescending
        
        return when (mediaFilter.sort) {
            Sort.SCORE -> {
                val sorted = result.sortedBy { item ->
                    item.collapsedGroup?.averageScore ?: item.mediaList.score
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.PROGRESS -> {
                val sorted = result.sortedBy { item ->
                    if (item.collapsedGroup != null) {
                        item.collapsedGroup.mediaLists.sumOf { it.progress }
                    } else {
                        item.mediaList.progress
                    }
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.LAST_UPDATED -> {
                val sorted = result.sortedBy { item ->
                    if (item.collapsedGroup != null) {
                        item.collapsedGroup.mediaLists.maxOf { it.updatedAt }
                    } else {
                        item.mediaList.updatedAt
                    }
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.LAST_ADDED -> {
                val sorted = result.sortedBy { item ->
                    if (item.collapsedGroup != null) {
                        item.collapsedGroup.mediaLists.maxOf { it.id ?: 0 }
                    } else {
                        item.mediaList.id ?: 0
                    }
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.START_DATE -> {
                val sorted = result.sortedBy { item ->
                    if (item.collapsedGroup != null) {
                        item.collapsedGroup.mediaLists.mapNotNull { TimeUtil.getMillisFromFuzzyDate(it.startedAt) }.minOrNull() ?: Long.MAX_VALUE
                    } else {
                        TimeUtil.getMillisFromFuzzyDate(item.mediaList.startedAt)
                    }
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.COMPLETED_DATE -> {
                val sorted = result.sortedBy { item ->
                    if (item.collapsedGroup != null) {
                        item.collapsedGroup.mediaLists.mapNotNull { TimeUtil.getMillisFromFuzzyDate(it.completedAt) }.maxOrNull() ?: Long.MIN_VALUE
                    } else {
                        TimeUtil.getMillisFromFuzzyDate(item.mediaList.completedAt)
                    }
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.RELEASE_DATE -> {
                val sorted = result.sortedBy { item ->
                    TimeUtil.getMillisFromFuzzyDate(item.mediaList.media.startDate)
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.AVERAGE_SCORE -> {
                val sorted = result.sortedBy { item ->
                    item.mediaList.media.averageScore
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.POPULARITY -> {
                val sorted = result.sortedBy { item ->
                    item.mediaList.media.popularity
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.FAVORITES -> {
                val sorted = result.sortedBy { item ->
                    if (item.collapsedGroup != null) {
                        item.collapsedGroup.mediaLists.sumOf { it.media.favourites }
                    } else {
                        item.mediaList.media.favourites
                    }
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.TRENDING -> {
                val sorted = result.sortedBy { item ->
                    item.mediaList.media.trending
                }
                if (isDescending) sorted.reversed() else sorted
            }
            Sort.PRIORITY -> {
                val sorted = result.sortedBy { item ->
                    if (item.collapsedGroup != null) {
                        item.collapsedGroup.mediaLists.maxOf { it.priority }
                    } else {
                        item.mediaList.priority
                    }
                }
                if (isDescending) sorted.reversed() else sorted
            }
            else -> {
                // Default: sort by title/franchise name
                result.sortedBy { 
                    it.collapsedGroup?.franchiseName?.lowercase() 
                        ?: it.mediaList.media.getTitle(appSetting).lowercase() 
                }
            }
        }
    }
}