package com.doma.alsan.ui.staff.character

import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.Media

interface StaffCharacterListListener {
    fun navigateToCharacter(character: Character)
    fun navigateToMedia(media: Media)
}