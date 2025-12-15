package com.doma.alsan.data.manager

import com.doma.alsan.data.localstorage.JsonStorageHandler
import com.doma.alsan.data.response.HomeData
import com.doma.alsan.data.response.anilist.MediaTag
import com.doma.alsan.data.response.Genre
import com.doma.alsan.helper.pojo.SaveItem

class DefaultContentManager(private val jsonStorageHandler: JsonStorageHandler) : ContentManager {

    override var homeData: SaveItem<HomeData>?
        get() = jsonStorageHandler.homeData
        set(value) { jsonStorageHandler.homeData = value }

    override var genres: SaveItem<List<Genre>>?
        get() = jsonStorageHandler.genres
        set(value) { jsonStorageHandler.genres = value }

    override var tags: SaveItem<List<MediaTag>>?
        get() = jsonStorageHandler.tags
        set(value) { jsonStorageHandler.tags = value }
}