package com.doma.alsan.ui.common

import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.helper.enums.Source
import com.doma.alsan.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class BottomSheetMediaQuickDetailViewModel(private val userRepository: UserRepository) : BaseViewModel<Unit>() {

    private val _appSetting = PublishSubject.create<AppSetting>()
    val appSetting: Observable<AppSetting>
        get() = _appSetting

    override fun loadData(param: Unit) {
        loadOnce {
            disposables.add(
                userRepository.getAppSetting()
                    .subscribe {
                        _appSetting.onNext(it)
                    }
            )
        }
    }
}