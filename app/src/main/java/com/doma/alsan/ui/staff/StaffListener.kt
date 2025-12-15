package com.doma.alsan.ui.staff

import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.Media

interface StaffListener {
    fun toggleShowMore(shouldShowMore: Boolean)
    fun navigateToStaffCharacter()
    fun navigateToStaffMedia()

    val staffCharacterListener: StaffCharacterListener
    val staffMediaListener: StaffMediaListener

    interface StaffCharacterListener {
        fun navigateToCharacter(character: Character)
    }

    interface StaffMediaListener {
        fun navigateToMedia(media: Media)
    }
}