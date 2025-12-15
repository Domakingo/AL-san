package com.doma.alsan.ui.character

import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.Staff

interface CharacterListener {
    fun toggleShowMore(shouldShowMore: Boolean)
    fun navigateToStaff(staff: Staff)
    fun showStaffMedia(staff: Staff)
    fun navigateToCharacterMedia()

    val characterMediaListener: CharacterMediaListener

    interface CharacterMediaListener {
        fun navigateToMedia(media: Media)
    }
}