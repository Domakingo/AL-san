package com.doma.alsan.data.datasource

import com.doma.alsan.data.network.retrofit.RetrofitHandler
import com.doma.alsan.data.response.github.AnnouncementResponse
import io.reactivex.rxjava3.core.Observable

class DefaultInfoDataSource(private val retrofitHandler: RetrofitHandler) : InfoDataSource {

    override fun getAnnouncement(): Observable<AnnouncementResponse> {
        return retrofitHandler.gitHubRetrofitClient().getAnnouncement()
    }
}