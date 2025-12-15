package com.doma.alsan.data.entity

data class CalendarSetting(
    var showOnlyWatchingAndPlanning: Boolean = true,
    var showOnlyCurrentSeason: Boolean = false,
    var showAdult: Boolean = false
)