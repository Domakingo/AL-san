package com.doma.alsan.ui.medialist

import java.util.LinkedHashMap

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.repository.BrowseRepository
import com.doma.alsan.data.repository.MediaListRepository
import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.data.response.anilist.MediaListOptions
import com.doma.alsan.helper.enums.Source
import com.doma.alsan.R
import com.doma.alsan.helper.extensions.applyScheduler
import com.doma.alsan.helper.extensions.getMediaType
import com.doma.alsan.helper.extensions.getStringResource
import com.doma.alsan.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.zip
import io.reactivex.rxjava3.subjects.PublishSubject

class BottomSheetMediaListQuickDetailViewModel(
    private val userRepository: UserRepository,
    private val browseRepository: BrowseRepository,
    private val mediaListRepository: MediaListRepository
) : BaseViewModel<BottomSheetMediaListQuickDetailParam>() {

    private val _settings = PublishSubject.create<Pair<MediaListOptions, AppSetting>>()
    val settings: Observable<Pair<MediaListOptions, AppSetting>>
        get() = _settings

    override fun loadData(param: BottomSheetMediaListQuickDetailParam) {
        loadOnce {
            val isViewer = param.userId == 0

            disposables.add(
                zip(
                    if (isViewer) userRepository.getViewer(Source.CACHE) else browseRepository.getUser(param.userId),
                    userRepository.getAppSetting()
                ) { user, appSetting ->
                    user.mediaListOptions to appSetting
                }
                    .applyScheduler()
                    .subscribe {
                        _settings.onNext(it)
                    }
            )
        }
    }

    fun updateCustomList(mediaList: com.doma.alsan.data.response.anilist.MediaList, name: String, enabled: Boolean) {
        @Suppress("UNCHECKED_CAST")
        val customListsMapping = (mediaList.customLists as? LinkedHashMap<String, Boolean>?)?.toMutableMap() ?: LinkedHashMap()
        customListsMapping[name] = enabled
        mediaList.customLists = customListsMapping
        
        val customListsList = customListsMapping.filter { it.value }.map { it.key }

        disposables.add(
            mediaListRepository.updateMediaListEntry(
                mediaList.media.type?.getMediaType() ?: com.doma.alsan.helper.enums.MediaType.ANIME,
                mediaList.id,
                mediaList.media.idAniList,
                mediaList.status ?: com.doma.alsan.type.MediaListStatus.PLANNING,
                mediaList.score,
                mediaList.progress,
                mediaList.progressVolumes,
                mediaList.repeat,
                mediaList.priority,
                mediaList.private,
                mediaList.notes,
                mediaList.hiddenFromStatusLists,
                customListsList,
                @Suppress("UNCHECKED_CAST")
                (mediaList.advancedScores as? LinkedHashMap<String, Double>?)?.values?.toList(),
                mediaList.startedAt,
                mediaList.completedAt
            )
                .applyScheduler()
                .subscribe({
                    _success.onNext(R.string.entry_saved)
                }, {
                    _error.onNext(it.getStringResource())
                })
        )
    }
}