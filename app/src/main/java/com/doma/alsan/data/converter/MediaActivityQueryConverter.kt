package com.doma.alsan.data.converter

import com.doma.alsan.MediaActivityQuery
import com.doma.alsan.data.response.anilist.Activity
import com.doma.alsan.data.response.anilist.ListActivity
import com.doma.alsan.data.response.anilist.Page
import com.doma.alsan.data.response.anilist.PageInfo

fun MediaActivityQuery.Data.convert(): Page<ListActivity> {
    return Page(
        pageInfo = PageInfo(
            total = Page?.pageInfo?.total ?: 0,
            perPage = Page?.pageInfo?.perPage ?: 0,
            currentPage = Page?.pageInfo?.currentPage ?: 0,
            lastPage = Page?.pageInfo?.lastPage ?: 0,
            hasNextPage = Page?.pageInfo?.hasNextPage ?: false
        ),
        data = Page?.activities?.filterNotNull()?.map { activity ->
            activity.onListActivity?.convert() ?: ListActivity()
        } ?: listOf()
    )
}