package com.doma.alsan.ui.profile

import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.data.response.anilist.Studio
import com.doma.alsan.helper.enums.MediaType

interface ProfileListener {

    interface StatsListener {
        fun navigateToStatsDetail()
        fun navigateToForceUpdate()
    }

    interface FavoriteMediaListener {
        fun navigateToFavoriteMedia(mediaType: MediaType)
        fun navigateToMedia(media: Media, mediaType: MediaType)
    }

    interface FavoriteCharacterListener {
        fun navigateToFavoriteCharacter()
        fun navigateToCharacter(character: Character)
    }

    interface FavoriteStaffListener {
        fun navigateToFavoriteStaff()
        fun navigateToStaff(staff: Staff)
    }

    interface FavoriteStudioListener {
        fun navigateToFavoriteStudio()
        fun navigateToStudio(studio: Studio)
    }

    val statsListener: StatsListener
    val favoriteMediaListener: FavoriteMediaListener
    val favoriteCharacterListener: FavoriteCharacterListener
    val favoriteStaffListener: FavoriteStaffListener
    val favoriteStudioListener: FavoriteStudioListener
}