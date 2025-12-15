package com.doma.alsan.data.repository

import com.doma.alsan.data.converter.convert
import com.doma.alsan.data.datasource.InfoDataSource
import com.doma.alsan.data.manager.UserManager
import com.doma.alsan.data.response.Announcement
import io.reactivex.rxjava3.core.Observable

class DefaultInfoRepository(private val infoDataSource: InfoDataSource, private val userManager: UserManager) : InfoRepository {

    override fun getAnnouncement(): Observable<Announcement> {
        return infoDataSource.getAnnouncement().map {
            it.convert()
        }
    }

    override fun getLastAnnouncementId(): Observable<String> {
        return Observable.just(userManager.lastAnnouncementId ?: "")
    }

    override fun setLastAnnouncementId(announcementId: String) {
        userManager.lastAnnouncementId = announcementId
    }
}