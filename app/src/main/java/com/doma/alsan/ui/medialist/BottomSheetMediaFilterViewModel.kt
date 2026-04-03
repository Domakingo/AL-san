package com.doma.alsan.ui.medialist

import com.doma.alsan.R
import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.repository.ContentRepository
import com.doma.alsan.data.response.Genre
import com.doma.alsan.data.response.anilist.MediaListGroup
import com.doma.alsan.helper.enums.*
import com.doma.alsan.helper.extensions.applyScheduler
import com.doma.alsan.helper.extensions.getNonUnknownValues
import com.doma.alsan.helper.extensions.getString
import com.doma.alsan.helper.pojo.ListItem
import com.doma.alsan.ui.base.BaseViewModel
import com.doma.alsan.type.MediaFormat
import com.doma.alsan.type.MediaListStatus
import com.doma.alsan.type.MediaSeason
import com.doma.alsan.type.MediaSource
import com.doma.alsan.type.MediaStatus
import com.doma.alsan.type.ScoreFormat
import com.doma.alsan.type.UserTitleLanguage
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

class BottomSheetMediaFilterViewModel(
    private val contentRepository: ContentRepository
) : BaseViewModel<BottomSheetMediaFilterParam>() {

    // Current filter state
    private var currentMediaFilter = MediaFilter()
    
    // Observable states
    private val _sortBy = BehaviorSubject.create<Sort>()
    val sortBy: Observable<Sort> get() = _sortBy

    private val _orderByDescending = BehaviorSubject.create<Boolean>()
    val orderByDescending: Observable<Boolean> get() = _orderByDescending

    private val _mediaFormatsText = BehaviorSubject.create<String>()
    val mediaFormatsText: Observable<String> get() = _mediaFormatsText

    private val _mediaStatusesText = BehaviorSubject.create<String>()
    val mediaStatusesText: Observable<String> get() = _mediaStatusesText

    private val _mediaSourcesText = BehaviorSubject.create<String>()
    val mediaSourcesText: Observable<String> get() = _mediaSourcesText

    private val _mediaListStatusesText = BehaviorSubject.create<String>()
    val mediaListStatusesText: Observable<String> get() = _mediaListStatusesText

    private val _countriesText = BehaviorSubject.create<String>()
    val countriesText: Observable<String> get() = _countriesText

    private val _seasonsText = BehaviorSubject.create<String>()
    val seasonsText: Observable<String> get() = _seasonsText

    private val _yearsText = BehaviorSubject.create<String>()
    val yearsText: Observable<String> get() = _yearsText

    private val _includedGenresText = BehaviorSubject.create<String>()
    val includedGenresText: Observable<String> get() = _includedGenresText

    private val _excludedGenresText = BehaviorSubject.create<String>()
    val excludedGenresText: Observable<String> get() = _excludedGenresText

    private val _persistFilter = BehaviorSubject.create<Boolean>()
    val persistFilter: Observable<Boolean> get() = _persistFilter

    private val _seasonVisibility = BehaviorSubject.create<Boolean>()
    val seasonVisibility: Observable<Boolean> get() = _seasonVisibility

    private val _mediaListStatusVisibility = BehaviorSubject.create<Boolean>()
    val mediaListStatusVisibility: Observable<Boolean> get() = _mediaListStatusVisibility

    // Dialog triggers
    private val _sortByList = PublishSubject.create<List<ListItem<Sort>>>()
    val sortByList: Observable<List<ListItem<Sort>>> get() = _sortByList

    private val _mediaFormatList = PublishSubject.create<Pair<List<ListItem<MediaFormat>>, ArrayList<Int>>>()
    val mediaFormatList: Observable<Pair<List<ListItem<MediaFormat>>, ArrayList<Int>>> get() = _mediaFormatList

    private val _mediaStatusList = PublishSubject.create<Pair<List<ListItem<MediaStatus>>, ArrayList<Int>>>()
    val mediaStatusList: Observable<Pair<List<ListItem<MediaStatus>>, ArrayList<Int>>> get() = _mediaStatusList

    private val _mediaSourceList = PublishSubject.create<Pair<List<ListItem<MediaSource>>, ArrayList<Int>>>()
    val mediaSourceList: Observable<Pair<List<ListItem<MediaSource>>, ArrayList<Int>>> get() = _mediaSourceList

    private val _mediaListStatusList = PublishSubject.create<Pair<List<ListItem<MediaListStatus>>, ArrayList<Int>>>()
    val mediaListStatusList: Observable<Pair<List<ListItem<MediaListStatus>>, ArrayList<Int>>> get() = _mediaListStatusList

    private val _countryList = PublishSubject.create<Pair<List<ListItem<Country>>, ArrayList<Int>>>()
    val countryList: Observable<Pair<List<ListItem<Country>>, ArrayList<Int>>> get() = _countryList

    private val _seasonList = PublishSubject.create<Pair<List<ListItem<MediaSeason>>, ArrayList<Int>>>()
    val seasonList: Observable<Pair<List<ListItem<MediaSeason>>, ArrayList<Int>>> get() = _seasonList

    private val _genreList = PublishSubject.create<Pair<List<ListItem<String>>, ArrayList<Int>>>()
    val genreList: Observable<Pair<List<ListItem<String>>, ArrayList<Int>>> get() = _genreList

    // Result
    private val _filterResult = PublishSubject.create<Pair<MediaFilter, Int>>()
    val filterResult: Observable<Pair<MediaFilter, Int>> get() = _filterResult

    // Parameter storage
    var mediaType: MediaType = MediaType.ANIME
    var scoreFormat: ScoreFormat = ScoreFormat.POINT_100
    var isUserList: Boolean = true
    var hasBigList: Boolean = false
    var isViewer: Boolean = true
    var listSections: List<MediaListGroup> = listOf()
    var selectedSectionIndex: Int = 0
    var isAllListPositionAtTop: Boolean = true

    // Cached genre list for both include and exclude operations
    private var cachedGenres: List<String> = listOf()

    override fun loadData(param: BottomSheetMediaFilterParam) {
        currentMediaFilter = param.mediaFilter.copy()
        mediaType = param.mediaType
        scoreFormat = param.scoreFormat
        isUserList = param.isUserList
        hasBigList = param.hasBigList
        isViewer = param.isViewer
        listSections = param.listSections
        selectedSectionIndex = param.selectedSectionIndex
        isAllListPositionAtTop = param.isAllListPositionAtTop

        // Emit initial values
        _sortBy.onNext(currentMediaFilter.sort)
        _orderByDescending.onNext(currentMediaFilter.orderByDescending)
        _persistFilter.onNext(currentMediaFilter.persistFilter)
        _seasonVisibility.onNext(mediaType == MediaType.ANIME)
        _mediaListStatusVisibility.onNext(isUserList)

        updateDisplayTexts()
        
        // Load genres in background
        if (isUserList) {
            loadGenresFromUserList()
        } else {
            loadGenresFromApi()
        }
    }
    
    private fun loadGenresFromUserList() {
        disposables.add(
            Observable.fromCallable {
                val genres = HashSet<String>()
                listSections.forEach { group ->
                    group.entries.forEach { entry ->
                        entry.media.genres.forEach { genre ->
                            genres.add(genre.name)
                        }
                    }
                }
                
                // Ensure currently selected genres are visible so they can be deselected
                genres.addAll(currentMediaFilter.includedGenres)
                genres.addAll(currentMediaFilter.excludedGenres)
                
                genres.sorted()
            }
            .applyScheduler()
            .subscribe(
                { sortedGenres ->
                    cachedGenres = sortedGenres
                },
                {
                    loadGenresFromApi()
                }
            )
        )
    }

    private fun loadGenresFromApi() {
        disposables.add(
            contentRepository.getGenres()
                .applyScheduler()
                .subscribe(
                    { genres ->
                        cachedGenres = genres.map { it.name }.sorted()
                    },
                    { /* ignore errors */ }
                )
        )
    }

    private fun updateDisplayTexts() {
        // Format text
        _mediaFormatsText.onNext(
            if (currentMediaFilter.mediaFormats.isEmpty()) "All"
            else currentMediaFilter.mediaFormats.size.toString()
        )

        // Status text
        _mediaStatusesText.onNext(
            if (currentMediaFilter.mediaStatuses.isEmpty()) "All"
            else currentMediaFilter.mediaStatuses.size.toString()
        )

        // Source text
        _mediaSourcesText.onNext(
            if (currentMediaFilter.mediaSources.isEmpty()) "All"
            else currentMediaFilter.mediaSources.size.toString()
        )

        // List Status text
        _mediaListStatusesText.onNext(
            if (currentMediaFilter.mediaListStatuses.isEmpty()) "All"
            else currentMediaFilter.mediaListStatuses.size.toString()
        )

        // Country text
        _countriesText.onNext(
            if (currentMediaFilter.countries.isEmpty()) "All"
            else currentMediaFilter.countries.size.toString()
        )

        // Season text
        _seasonsText.onNext(
            if (currentMediaFilter.mediaSeasons.isEmpty()) "All"
            else currentMediaFilter.mediaSeasons.size.toString()
        )

        // Year text
        val minYear = currentMediaFilter.minYear
        val maxYear = currentMediaFilter.maxYear
        _yearsText.onNext(
            when {
                minYear != null && maxYear != null -> "$minYear-$maxYear"
                minYear != null -> "$minYear+"
                maxYear != null -> "-$maxYear"
                else -> "All"
            }
        )

        // Included genres text
        _includedGenresText.onNext(
            if (currentMediaFilter.includedGenres.isEmpty()) "None"
            else currentMediaFilter.includedGenres.size.toString()
        )

        // Excluded genres text
        _excludedGenresText.onNext(
            if (currentMediaFilter.excludedGenres.isEmpty()) "None"
            else currentMediaFilter.excludedGenres.size.toString()
        )
    }

    fun loadSortByOptions() {
        val sortList = ArrayList<ListItem<Sort>>()
        sortList.addAll(Sort.values().map { sort ->
            ListItem(sort.getStringResource(), sort)
        })
        if (!isUserList) {
            sortList.removeAll { it.data.getAniListMediaSort(UserTitleLanguage.ROMAJI, true) == null }
        }
        _sortByList.onNext(sortList)
    }

    fun updateSortBy(newSort: Sort) {
        currentMediaFilter.sort = newSort
        _sortBy.onNext(newSort)
    }

    fun toggleOrderBy() {
        val newValue = !currentMediaFilter.orderByDescending
        currentMediaFilter.orderByDescending = newValue
        _orderByDescending.onNext(newValue)
    }

    fun updatePersistFilter(persist: Boolean) {
        currentMediaFilter.persistFilter = persist
        _persistFilter.onNext(persist)
    }

    fun loadMediaFormats() {
        val animeFormats = listOf(
            MediaFormat.TV,
            MediaFormat.TV_SHORT,
            MediaFormat.MOVIE,
            MediaFormat.SPECIAL,
            MediaFormat.OVA,
            MediaFormat.ONA,
            MediaFormat.MUSIC
        )
        val mangaFormats = listOf(
            MediaFormat.MANGA,
            MediaFormat.ONE_SHOT,
            MediaFormat.NOVEL
        )
        val mediaFormats = when (mediaType) {
            MediaType.ANIME -> animeFormats
            MediaType.MANGA -> mangaFormats
        }

        val formats = ArrayList<ListItem<MediaFormat>>()
        formats.addAll(mediaFormats.map { ListItem(it.getString(), it) })

        val selectedIndex = ArrayList<Int>()
        currentMediaFilter.mediaFormats.forEach {
            val index = mediaFormats.indexOf(it)
            if (index != -1)
                selectedIndex.add(index)
        }

        _mediaFormatList.onNext(formats to selectedIndex)
    }

    fun updateMediaFormats(formats: List<MediaFormat>) {
        currentMediaFilter.mediaFormats = formats
        updateDisplayTexts()
    }

    fun loadMediaStatuses() {
        val mediaStatuses = getNonUnknownValues<MediaStatus>()
        val statuses = ArrayList<ListItem<MediaStatus>>()
        statuses.addAll(mediaStatuses.map { ListItem(it.getString(), it) })

        val selectedIndex = ArrayList<Int>()
        currentMediaFilter.mediaStatuses.forEach {
            val index = mediaStatuses.indexOf(it)
            if (index != -1)
                selectedIndex.add(index)
        }

        _mediaStatusList.onNext(statuses to selectedIndex)
    }

    fun updateMediaStatuses(statuses: List<MediaStatus>) {
        currentMediaFilter.mediaStatuses = statuses
        updateDisplayTexts()
    }

    fun loadMediaListStatuses() {
        val listStatuses = getNonUnknownValues<MediaListStatus>()
        val statuses = ArrayList<ListItem<MediaListStatus>>()
        statuses.addAll(listStatuses.map { ListItem(it.getString(mediaType), it) })

        val selectedIndex = ArrayList<Int>()
        currentMediaFilter.mediaListStatuses.forEach {
            val index = listStatuses.indexOf(it)
            if (index != -1)
                selectedIndex.add(index)
        }

        _mediaListStatusList.onNext(statuses to selectedIndex)
    }

    fun updateMediaListStatuses(statuses: List<MediaListStatus>) {
        currentMediaFilter.mediaListStatuses = statuses
        updateDisplayTexts()
    }

    fun loadMediaSources() {
        val mediaSources = getNonUnknownValues<MediaSource>()
        val sources = ArrayList<ListItem<MediaSource>>()
        sources.addAll(mediaSources.map { ListItem(it.getString(), it) })

        val selectedIndex = ArrayList<Int>()
        currentMediaFilter.mediaSources.forEach {
            val index = mediaSources.indexOf(it)
            if (index != -1)
                selectedIndex.add(index)
        }

        _mediaSourceList.onNext(sources to selectedIndex)
    }

    fun updateMediaSources(sources: List<MediaSource>) {
        currentMediaFilter.mediaSources = sources
        updateDisplayTexts()
    }

    fun loadCountries() {
        val mediaCountries = Country.values()
        val countries = ArrayList<ListItem<Country>>()
        countries.addAll(mediaCountries.map { ListItem(it.getString(), it) })

        val selectedIndex = ArrayList<Int>()
        currentMediaFilter.countries.forEach {
            val index = mediaCountries.indexOf(it)
            if (index != -1)
                selectedIndex.add(index)
        }

        _countryList.onNext(countries to selectedIndex)
    }

    fun updateCountries(countries: List<Country>) {
        currentMediaFilter.countries = countries
        updateDisplayTexts()
    }

    fun loadSeasons() {
        val mediaSeasons = getNonUnknownValues<MediaSeason>()
        val seasons = ArrayList<ListItem<MediaSeason>>()
        seasons.addAll(mediaSeasons.map { ListItem(it.getString(), it) })

        val selectedIndex = ArrayList<Int>()
        currentMediaFilter.mediaSeasons.forEach {
            val index = mediaSeasons.indexOf(it)
            if (index != -1)
                selectedIndex.add(index)
        }

        _seasonList.onNext(seasons to selectedIndex)
    }

    fun updateSeasons(seasons: List<MediaSeason>) {
        currentMediaFilter.mediaSeasons = seasons
        updateDisplayTexts()
    }

    fun loadIncludedGenres() {
        val genres = cachedGenres.ifEmpty { listOf() }
        val list = genres.map { genre -> ListItem(genre, genre) }
        
        val selectedIndex = ArrayList<Int>()
        currentMediaFilter.includedGenres.forEach { genre ->
            val index = genres.indexOf(genre)
            if (index != -1)
                selectedIndex.add(index)
        }
        
        _genreList.onNext(list to selectedIndex)
    }

    fun updateIncludedGenres(genres: List<String>) {
        currentMediaFilter.includedGenres = genres
        updateDisplayTexts()
    }

    fun loadExcludedGenres() {
        val genres = cachedGenres.ifEmpty { listOf() }
        val list = genres.map { genre -> ListItem(genre, genre) }
        
        val selectedIndex = ArrayList<Int>()
        currentMediaFilter.excludedGenres.forEach { genre ->
            val index = genres.indexOf(genre)
            if (index != -1)
                selectedIndex.add(index)
        }
        
        _genreList.onNext(list to selectedIndex)
    }

    fun updateExcludedGenres(genres: List<String>) {
        currentMediaFilter.excludedGenres = genres
        updateDisplayTexts()
    }

    fun updateYears(minYear: Int?, maxYear: Int?) {
        currentMediaFilter.minYear = minYear
        currentMediaFilter.maxYear = maxYear
        updateDisplayTexts()
    }

    fun applyFilter() {
        _filterResult.onNext(currentMediaFilter to selectedSectionIndex)
    }

    fun resetFilter() {
        currentMediaFilter = MediaFilter()
        _sortBy.onNext(currentMediaFilter.sort)
        _orderByDescending.onNext(currentMediaFilter.orderByDescending)
        _persistFilter.onNext(currentMediaFilter.persistFilter)
        updateDisplayTexts()
    }

    fun updateSelectedSection(index: Int) {
        selectedSectionIndex = index
    }

    fun getCurrentFilter(): MediaFilter = currentMediaFilter

    fun getMinYear(): Int? = currentMediaFilter.minYear
    fun getMaxYear(): Int? = currentMediaFilter.maxYear
}
