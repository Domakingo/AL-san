package com.doma.alsan.ui.settings.account

import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.ui.base.BaseViewModel

class AccountSettingsViewModel(private val userRepository: UserRepository) : BaseViewModel<Unit>() {

    override fun loadData(param: Unit) = Unit

    fun logout() {
        userRepository.logout()
    }
}