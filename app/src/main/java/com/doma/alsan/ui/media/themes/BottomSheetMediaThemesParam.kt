package com.doma.alsan.ui.media.themes

import com.doma.alsan.data.response.AnimeTheme
import com.doma.alsan.data.response.AnimeThemeEntry
import com.doma.alsan.data.response.anilist.Media

data class BottomSheetMediaThemesParam(
    val media: Media,
    val animeTheme: AnimeTheme,
    val animeThemeEntry: AnimeThemeEntry?
)