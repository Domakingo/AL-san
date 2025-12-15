package com.doma.alsan.helper.pojo

import android.net.Uri
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.entity.ListStyle
import com.doma.alsan.data.response.anilist.MediaListOptions

data class MediaListAdapterComponent(
    var isViewer: Boolean = false,
    var listStyle: ListStyle = ListStyle(),
    var appSetting: AppSetting = AppSetting(),
    var mediaListOptions: MediaListOptions = MediaListOptions(),
    var backgroundUri: Uri? = null
)