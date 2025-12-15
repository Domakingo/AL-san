package com.doma.alsan.ui.medialist

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.repository.BrowseRepository
import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.data.response.anilist.MediaListOptions
import com.doma.alsan.helper.enums.Source
import com.doma.alsan.helper.extensions.applyScheduler
import com.doma.alsan.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.zip
import io.reactivex.rxjava3.subjects.PublishSubject

class BottomSheetMediaListQuickDetailViewModel(
    private val userRepository: UserRepository,
    private val browseRepository: BrowseRepository
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
}