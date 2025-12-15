package com.doma.alsan.ui.media.mediastats

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.repository.BrowseRepository
import com.doma.alsan.data.repository.ContentRepository
import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.helper.extensions.applyScheduler
import com.doma.alsan.helper.pojo.MediaStatsItem
import com.doma.alsan.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

class MediaStatsViewModel(
    private val userRepository: UserRepository
) : BaseViewModel<MediaStatsParam>() {

    private val _appSetting = PublishSubject.create<AppSetting>()
    val appSetting: Observable<AppSetting>
        get() = _appSetting

    private val _mediaStatsItems = BehaviorSubject.createDefault(listOf<MediaStatsItem>())
    val mediaStatsItems: Observable<List<MediaStatsItem>>
        get() = _mediaStatsItems

    private lateinit var media: Media

    override fun loadData(param: MediaStatsParam) {
        loadOnce {
            media = param.media

            disposables.add(
                userRepository.getAppSetting()
                    .applyScheduler()
                    .subscribe {
                        _appSetting.onNext(it)

                        val items = ArrayList<MediaStatsItem>()

                        items.add(MediaStatsItem(media, viewType = MediaStatsItem.VIEW_TYPE_STATS_HEADER))
                        items.add(MediaStatsItem(media, viewType = MediaStatsItem.VIEW_TYPE_STATS))

                        if (media.rankings.isNotEmpty()) {
                            items.add(MediaStatsItem(media, viewType = MediaStatsItem.VIEW_TYPE_RANKING_HEADER))
                            items.addAll(
                                media.rankings.map {
                                    MediaStatsItem(mediaRank = it, viewType = MediaStatsItem.VIEW_TYPE_RANKING)
                                }
                            )
                        }

                        if (!media.stats?.statusDistribution.isNullOrEmpty()) {
                            items.add(MediaStatsItem(media, viewType = MediaStatsItem.VIEW_TYPE_STATUS_DISTRIBUTION_HEADER))
                            items.add(MediaStatsItem(media, viewType = MediaStatsItem.VIEW_TYPE_STATUS_DISTRIBUTION))
                        }

                        if (!media.stats?.scoreDistribution.isNullOrEmpty()) {
                            items.add(MediaStatsItem(media, viewType = MediaStatsItem.VIEW_TYPE_SCORE_DISTRIBUTION_HEADER))
                            items.add(MediaStatsItem(media, viewType = MediaStatsItem.VIEW_TYPE_SCORE_DISTRIBUTION))
                        }

                        _mediaStatsItems.onNext(items)
                    }
            )
        }
    }
}