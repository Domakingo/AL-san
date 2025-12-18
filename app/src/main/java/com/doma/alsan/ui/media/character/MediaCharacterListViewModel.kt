package com.doma.alsan.ui.media.character

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.repository.BrowseRepository
import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.data.response.anilist.CharacterEdge
import com.doma.alsan.helper.extensions.applyScheduler
import com.doma.alsan.helper.extensions.getNonUnknownValues
import com.doma.alsan.helper.extensions.getString
import com.doma.alsan.helper.extensions.getStringResource
import com.doma.alsan.helper.pojo.ListItem
import com.doma.alsan.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import com.doma.alsan.type.StaffLanguage
import com.doma.alsan.helper.enums.MediaType

class MediaCharacterListViewModel(
    private val userRepository: UserRepository,
    private val browseRepository: BrowseRepository
) : BaseViewModel<MediaCharacterListParam>() {

    private val _appSetting = PublishSubject.create<AppSetting>()
    val appSetting: Observable<AppSetting>
        get() = _appSetting

    private val _characters = BehaviorSubject.createDefault<List<CharacterEdge>>(listOf())
    val characters: Observable<List<CharacterEdge>>
        get() = _characters

    private val _emptyLayoutVisibility = BehaviorSubject.createDefault(false)
    val emptyLayoutVisibility: Observable<Boolean>
        get() = _emptyLayoutVisibility

    private val _voiceActorLanguages = PublishSubject.create<List<ListItem<StaffLanguage>>>()
    val voiceActorLanguages: Observable<List<ListItem<StaffLanguage>>>
        get() = _voiceActorLanguages

    private var mediaId = 0
    private var mediaType = MediaType.ANIME
    private var selectedLanguage = StaffLanguage.JAPANESE

    private var hasNextPage = false
    private var currentPage = 0

    // All characters for search functionality
    private var allCharacters: List<CharacterEdge> = listOf()
    private var isLoadingAllCharacters = false
    private var allCharactersLoaded = false
    private var currentSearchQuery: String = ""

    override fun loadData(param: MediaCharacterListParam) {
        loadOnce {
            mediaId = param.mediaId
            mediaType = param.mediaType

            disposables.add(
                userRepository.getAppSetting()
                    .applyScheduler()
                    .subscribe {
                        _appSetting.onNext(it)
                        loadCharacters()
                    }
            )
        }
    }

    fun reloadData() {
        allCharacters = listOf()
        allCharactersLoaded = false
        currentSearchQuery = ""
        loadCharacters()
    }

    fun loadNextPage() {
        if ((state == State.LOADED || state == State.ERROR) && hasNextPage && currentSearchQuery.isEmpty()) {
            val currentCharacters = ArrayList(_characters.value ?: listOf())
            currentCharacters.add(null)
            _characters.onNext(currentCharacters)

            loadCharacters(true)
        }
    }

    private fun loadCharacters(isLoadingNextPage: Boolean = false) {
        if (!isLoadingNextPage)
            _loading.onNext(true)

        state = State.LOADING

        disposables.add(
            browseRepository.getMediaCharacters(mediaId, if (isLoadingNextPage) currentPage + 1 else 1, selectedLanguage)
                .applyScheduler()
                .doFinally {
                    if (!isLoadingNextPage) {
                        _loading.onNext(false)
                        _emptyLayoutVisibility.onNext(_characters.value.isNullOrEmpty())
                    }
                }
                .subscribe(
                    { (pageInfo, characterEdges) ->
                        hasNextPage = pageInfo.hasNextPage
                        currentPage = pageInfo.currentPage

                        if (isLoadingNextPage) {
                            val currentCharacters = ArrayList(_characters.value ?: listOf())
                            currentCharacters.remove(null)
                            currentCharacters.addAll(characterEdges)
                            _characters.onNext(currentCharacters)
                            // Also update allCharacters cache
                            allCharacters = allCharacters + characterEdges
                        } else {
                            _characters.onNext(characterEdges)
                            allCharacters = characterEdges
                        }

                        state = State.LOADED
                    },
                    {
                        if (isLoadingNextPage) {
                            val currentCharacters = ArrayList(_characters.value ?: listOf())
                            currentCharacters.remove(null)
                            _characters.onNext(currentCharacters)
                        }

                        _error.onNext(it.getStringResource())
                        state = State.ERROR
                    }
                )
        )
    }

    fun searchCharacters(query: String) {
        currentSearchQuery = query.trim().lowercase()
        
        if (currentSearchQuery.isEmpty()) {
            // Show normal paginated list
            _characters.onNext(allCharacters)
            _emptyLayoutVisibility.onNext(allCharacters.isEmpty())
            return
        }

        // If we haven't loaded all characters yet, do it now
        if (!allCharactersLoaded && hasNextPage && !isLoadingAllCharacters) {
            loadAllCharacters()
            return
        }

        // Filter locally
        val filteredCharacters = allCharacters.filter { edge ->
            edge.node.name.userPreferred.lowercase().contains(currentSearchQuery) ||
            edge.node.name.full?.lowercase()?.contains(currentSearchQuery) == true ||
            edge.node.name.native?.lowercase()?.contains(currentSearchQuery) == true ||
            edge.node.name.first?.lowercase()?.contains(currentSearchQuery) == true ||
            edge.node.name.last?.lowercase()?.contains(currentSearchQuery) == true ||
            (mediaType == MediaType.ANIME && edge.voiceActorRoles.any { vaRole -> 
                vaRole.voiceActor.name.userPreferred.lowercase().contains(currentSearchQuery) ||
                vaRole.voiceActor.name.full?.lowercase()?.contains(currentSearchQuery) == true ||
                vaRole.voiceActor.name.native?.lowercase()?.contains(currentSearchQuery) == true
            })
        }
        
        _characters.onNext(filteredCharacters)
        _emptyLayoutVisibility.onNext(filteredCharacters.isEmpty())
    }

    private fun loadAllCharacters() {
        if (isLoadingAllCharacters) return
        isLoadingAllCharacters = true
        _loading.onNext(true)

        loadAllCharactersRecursive(currentPage + 1)
    }

    private fun loadAllCharactersRecursive(page: Int) {
        disposables.add(
            browseRepository.getMediaCharacters(mediaId, page, selectedLanguage)
                .applyScheduler()
                .subscribe(
                    { (pageInfo, characterEdges) ->
                        allCharacters = allCharacters + characterEdges

                        if (pageInfo.hasNextPage) {
                            loadAllCharactersRecursive(page + 1)
                        } else {
                            allCharactersLoaded = true
                            isLoadingAllCharacters = false
                            _loading.onNext(false)
                            // Re-run search with all characters loaded
                            searchCharacters(currentSearchQuery)
                        }
                    },
                    {
                        isLoadingAllCharacters = false
                        _loading.onNext(false)
                        _error.onNext(it.getStringResource())
                        // Still filter with what we have
                        searchCharacters(currentSearchQuery)
                    }
                )
        )
    }

    fun updateVoiceActorLanguage(newLanguage: StaffLanguage) {
        selectedLanguage = newLanguage
        allCharacters = listOf()
        allCharactersLoaded = false
        reloadData()
    }

    fun loadVoiceActorLanguages() {
        if (mediaType == MediaType.MANGA) return

        // Make a request without language filter to get all available VAs
        // and extract the unique languages
        _loading.onNext(true)
        
        disposables.add(
            browseRepository.getMediaCharacters(mediaId, 1, null)
                .applyScheduler()
                .doFinally { _loading.onNext(false) }
                .subscribe(
                    { (_, characterEdges) ->
                        // Extract unique languages from all voice actors
                        val availableLanguages = characterEdges
                            .flatMap { it.voiceActorRoles }
                            .mapNotNull { it.voiceActor.language.ifBlank { null } }
                            .distinct()
                            .mapNotNull { language ->
                                StaffLanguage.values().find { 
                                    it.name.equals(language.replace(" ", "_"), ignoreCase = true) 
                                }
                            }
                            .sortedBy { it.ordinal }
                        
                        if (availableLanguages.isNotEmpty()) {
                            _voiceActorLanguages.onNext(availableLanguages.map { lang -> ListItem(lang.getString(), lang) })
                        } else {
                            // Fallback to showing all languages if none found
                            _voiceActorLanguages.onNext(getNonUnknownValues<StaffLanguage>().map { lang -> ListItem(lang.getString(), lang) })
                        }
                    },
                    {
                        // On error, show all languages as fallback
                        _voiceActorLanguages.onNext(getNonUnknownValues<StaffLanguage>().map { lang -> ListItem(lang.getString(), lang) })
                    }
                )
        )
    }

    fun getSelectedLanguage(): StaffLanguage = selectedLanguage
}