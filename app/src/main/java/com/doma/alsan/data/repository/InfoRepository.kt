package com.doma.alsan.data.repository

import com.doma.alsan.data.response.Announcement
import io.reactivex.rxjava3.core.Observable

interface InfoRepository {
    fun getAnnouncement(): Observable<Announcement>
    fun getLastAnnouncementId(): Observable<String>
    fun setLastAnnouncementId(announcementId: String)
}