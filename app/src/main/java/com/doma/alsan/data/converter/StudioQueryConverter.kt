package com.doma.alsan.data.converter

import com.doma.alsan.StudioQuery
import com.doma.alsan.data.response.anilist.*

fun StudioQuery.Data.convert(): Studio {
    return Studio?.let {
        Studio(
            id = it.id,
            name = it.name,
            isAnimationStudio = it.isAnimationStudio,
            media = MediaConnection(
                edges = it.media?.edges?.mapNotNull { edge ->
                    val node = edge?.node ?: return@mapNotNull null
                    MediaEdge(
                        node = Media(
                            idAniList = node.id,
                            title = MediaTitle(
                                romaji = node.title?.romaji ?: "",
                                english = node.title?.english ?: "",
                                native = node.title?.native ?: "",
                                userPreferred = node.title?.userPreferred ?: ""
                            ),
                            type = node.type,
                            format = node.format,
                            coverImage = MediaCoverImage(
                                extraLarge = node.coverImage?.extraLarge ?: "",
                                large = node.coverImage?.large ?: "",
                                medium = node.coverImage?.medium ?: ""
                            ),
                            countryOfOrigin = node.countryOfOrigin,
                            averageScore = node.averageScore ?: 0,
                            meanScore = node.meanScore ?: 0,
                            popularity = node.popularity ?: 0,
                            favourites = node.favourites ?: 0,
                            startDate = node.startDate?.let { startDate ->
                                FuzzyDate(year = startDate.year, month = startDate.month, day = startDate.day)
                            }
                        ),
                        isMainStudio = edge.isMainStudio
                    )
                } ?: listOf(),
                pageInfo = PageInfo(
                    total = it.media?.pageInfo?.total ?: 0,
                    perPage = it.media?.pageInfo?.perPage ?: 0,
                    currentPage = it.media?.pageInfo?.currentPage ?: 0,
                    lastPage = it.media?.pageInfo?.lastPage ?: 0,
                    hasNextPage = it.media?.pageInfo?.hasNextPage ?: false
                )
            ),
            siteUrl = it.siteUrl ?: "",
            isFavourite = it.isFavourite,
            favourites = it.favourites ?: 0
        )
    } ?: Studio()
}