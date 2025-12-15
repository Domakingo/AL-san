package com.doma.alsan.helper.enums

import com.doma.alsan.R

enum class MediaType {
    ANIME,
    MANGA
}

fun MediaType.getAniListMediaType(): com.doma.alsan.type.MediaType {
    return when (this) {
        MediaType.ANIME -> com.doma.alsan.type.MediaType.ANIME
        MediaType.MANGA -> com.doma.alsan.type.MediaType.MANGA
    }
}

fun MediaType.getStringResource(): Int {
    return when (this) {
        MediaType.ANIME -> R.string.anime
        MediaType.MANGA -> R.string.manga
    }
}