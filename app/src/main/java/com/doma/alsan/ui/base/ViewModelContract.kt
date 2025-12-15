package com.doma.alsan.ui.base

interface ViewModelContract<T> {
    fun loadData(param: T)
}