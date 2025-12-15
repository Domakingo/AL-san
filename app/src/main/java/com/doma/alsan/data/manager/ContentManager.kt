package com.doma.alsan.data.manager

import com.doma.alsan.data.response.HomeData
import com.doma.alsan.data.response.anilist.MediaTag
import com.doma.alsan.data.response.Genre
import com.doma.alsan.helper.pojo.SaveItem

interface ContentManager {
    var homeData: SaveItem<HomeData>?
    var genres: SaveItem<List<Genre>>?
    var tags: SaveItem<List<MediaTag>>?
}