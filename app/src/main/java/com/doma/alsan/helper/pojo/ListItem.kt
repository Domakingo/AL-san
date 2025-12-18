package com.doma.alsan.helper.pojo

data class ListItem<T>(
    val text: String = "",
    val stringResources: List<Int> = listOf(),
    val data: T,
    val isHighlighted: Boolean = false,
    val useCardLayout: Boolean = false
) {
    constructor(stringResource: Int, data: T) : this("{0}", listOf(stringResource), data)
    constructor(text: String, data: T) : this(text, listOf(), data)
    constructor(text: String, data: T, isHighlighted: Boolean) : this(text, listOf(), data, isHighlighted)
    constructor(text: String, data: T, isHighlighted: Boolean, useCardLayout: Boolean) : this(text, listOf(), data, isHighlighted, useCardLayout)
}