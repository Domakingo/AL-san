package com.doma.alsan.data.converter

import com.doma.alsan.FollowingQuery
import com.doma.alsan.data.response.anilist.Page
import com.doma.alsan.data.response.anilist.PageInfo
import com.doma.alsan.data.response.anilist.User
import com.doma.alsan.data.response.anilist.UserAvatar

fun FollowingQuery.Data.convert(): Page<User> {
    return Page(
        PageInfo(
            total = Page?.pageInfo?.total ?: 0,
            perPage = Page?.pageInfo?.perPage ?: 0,
            currentPage = Page?.pageInfo?.currentPage ?: 0,
            lastPage = Page?.pageInfo?.lastPage ?: 0,
            hasNextPage = Page?.pageInfo?.hasNextPage ?: false
        ),
        Page?.following?.filterNotNull()?.map {
            User(
                id = it.id,
                name = it.name,
                avatar = UserAvatar(
                    large = it.avatar?.large ?: "",
                    medium = it.avatar?.medium ?: ""
                ),
                bannerImage = it.bannerImage ?: "",
                isFollowing = it.isFollowing ?: false,
                isFollower = it.isFollower ?: false,
                siteUrl = it.siteUrl ?: ""
            )
        } ?: listOf()
    )
}