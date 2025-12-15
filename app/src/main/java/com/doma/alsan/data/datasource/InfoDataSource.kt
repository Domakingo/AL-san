package com.doma.alsan.data.datasource

import com.doma.alsan.data.response.github.AnnouncementResponse
import io.reactivex.rxjava3.core.Observable

interface InfoDataSource {
    fun getAnnouncement(): Observable<AnnouncementResponse>
}