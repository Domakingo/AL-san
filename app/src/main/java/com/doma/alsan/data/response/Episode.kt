package com.doma.alsan.data.response

data class Episode(
    val number: Int = 0,
    val title: String = "",
    val titleJapanese: String = "",
    val titleRomanji: String = "",
    val aired: String = "",
    val filler: Boolean = false,
    val recap: Boolean = false,
    val url: String = ""
)
