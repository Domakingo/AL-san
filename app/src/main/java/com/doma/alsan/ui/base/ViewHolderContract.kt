package com.doma.alsan.ui.base

interface ViewHolderContract<T> {
    fun bind(item: T, index: Int)
}