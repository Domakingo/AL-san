package com.doma.alsan.ui.login

import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.helper.enums.Source
import com.doma.alsan.helper.extensions.applyScheduler
import com.doma.alsan.helper.extensions.getStringResource
import com.doma.alsan.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class LoginViewModel(private val userRepository: UserRepository) : BaseViewModel<Unit>() {

    private val _loginTrigger = PublishSubject.create<Unit>()
    val loginTrigger: Observable<Unit>
        get() = _loginTrigger

    override fun loadData(param: Unit) = Unit

    fun login(bearerToken: String) {
        _loading.onNext(true)
        userRepository.saveBearerToken(bearerToken)

        disposables.add(
            userRepository.getViewer(Source.NETWORK)
                .applyScheduler()
                .doFinally {
                    _loading.onNext(false)
                }
                .subscribe(
                    {
                        _loginTrigger.onNext(Unit)
                    },
                    {
                        _error.onNext(it.getStringResource())
                    }
                )
        )
    }

    fun loginAsGuest() {
        userRepository.loginAsGuest()
        _loginTrigger.onNext(Unit)
    }
}