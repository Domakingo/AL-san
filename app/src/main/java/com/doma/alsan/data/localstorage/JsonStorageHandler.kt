package com.doma.alsan.data.localstorage

import com.doma.alsan.data.response.HomeData
import com.doma.alsan.data.response.anilist.MediaTag
import com.doma.alsan.data.response.anilist.User
import com.doma.alsan.data.response.Genre
import com.doma.alsan.data.response.anilist.MediaListCollection
import com.doma.alsan.helper.pojo.SaveItem

interface JsonStorageHandler {
    var homeData: SaveItem<HomeData>?
    var viewerData: SaveItem<User>?
    var genres: SaveItem<List<Genre>>?
    var tags: SaveItem<List<MediaTag>>?
    var animeList: SaveItem<MediaListCollection>?
    var mangaList: SaveItem<MediaListCollection>?
}