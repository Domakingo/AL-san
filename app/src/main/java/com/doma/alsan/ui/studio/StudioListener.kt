package com.doma.alsan.ui.studio

import com.doma.alsan.data.response.anilist.Media

interface StudioListener {
    fun navigateToMedia(media: Media)
    fun navigateToStudioMedia()
}