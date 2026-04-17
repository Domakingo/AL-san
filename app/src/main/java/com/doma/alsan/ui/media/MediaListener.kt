package com.doma.alsan.ui.media

import com.doma.alsan.data.response.AnimeTheme
import com.doma.alsan.data.response.AnimeThemeEntry
import com.doma.alsan.data.response.Episode
import com.doma.alsan.data.response.Genre
import com.doma.alsan.data.response.anilist.*
import com.doma.alsan.type.MediaSeason
import com.doma.alsan.type.MediaType

interface MediaListener {


    interface MediaInfoListener {
        fun copyTitle(title: String)
        fun navigateToExplore(type: MediaType, season: MediaSeason, seasonYear: Int)
    }

    interface MediaGenreListener {
        fun navigateToExplore(type: MediaType, genre: Genre)
    }

    interface MediaCharacterListener {
        fun navigateToCharacter(character: Character)
        fun navigateToStaff(staff: Staff)
        fun openLanguageDialog()
    }

    interface MediaStudioListener {
        fun navigateToStudio(studio: Studio)
    }

    interface MediaTagsListener {
        fun shouldShowSpoilers(show: Boolean)
        fun navigateToExplore(type: MediaType, tag: MediaTag)
        fun showDescription(tag: MediaTag)
    }

    interface MediaThemesListener {
        fun openThemeDialog(media: Media, animeTheme: AnimeTheme, animeThemeEntry: AnimeThemeEntry?)
        fun openGroupDialog(viewType: Int, groups: List<String>)
    }

    interface MediaStaffListener {
        fun navigateToStaff(staff: Staff)
    }

    interface MediaRelationsListener {
        fun navigateToMedia(media: Media)
    }

    interface MediaRecommendationsListener {
        fun navigateToMedia(media: Media)
    }

    interface MediaLinksListener {
        fun navigateToUrl(mediaExternalLink: MediaExternalLink)
        fun copyExternalLink(mediaExternalLink: MediaExternalLink)
    }

    interface MediaEpisodesListener {
        fun onEpisodeClick(episode: Episode)
        fun onEpisodeLongClick(episode: Episode)
        fun onPageClick(malId: Int, page: Int)
        fun onPageSelectorClick(view: android.view.View, malId: Int, currentPage: Int, totalPages: Int)
    }

    val mediaInfoListener: MediaInfoListener
    val mediaGenreListener: MediaGenreListener
    val mediaCharacterListener: MediaCharacterListener
    val mediaStudioListener: MediaStudioListener
    val mediaTagsListener: MediaTagsListener
    val mediaThemesListener: MediaThemesListener
    val mediaStaffListener: MediaStaffListener
    val mediaRelationsListener: MediaRelationsListener
    val mediaRecommendationsListener: MediaRecommendationsListener
    val mediaLinksListener: MediaLinksListener
    val mediaEpisodesListener: MediaEpisodesListener
}