package com.doma.alsan.data.response

import com.doma.alsan.data.response.anilist.Activity

data class SocialData(
    val friendsActivities: List<Activity> = listOf(),
    val globalActivities: List<Activity> = listOf()
)