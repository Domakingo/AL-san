package com.doma.alsan.helper.enums

import com.doma.alsan.R
import com.doma.alsan.helper.extensions.convertFromSnakeCase
import java.util.*

/**
 * ANILIST_DARK_BLUE is the default theme using official AniList brand colors.
 */
enum class AppTheme(val colors: Triple<Int, Int, Int>) {
    // Blue Theme - Pastel for Dark, Deep for Light
    ANILIST_DARK_BLUE(Triple(R.color.pastelBlue, R.color.pastelBlueSecondary, R.color.dangerRed)),
    ANILIST_LIGHT_BLUE(Triple(R.color.deepBlue, R.color.deepBlueSecondary, R.color.dangerRed)),
    
    // Purple Theme - Pastel for Dark, Deep for Light
    DARK_PURPLE(Triple(R.color.pastelPurple, R.color.pastelPurpleSecondary, R.color.dangerRed)),
    LIGHT_PURPLE(Triple(R.color.deepPurple, R.color.deepPurpleSecondary, R.color.dangerRed)),
    
    // Green Theme - Pastel for Dark, Deep for Light 
    DARK_GREEN(Triple(R.color.pastelGreen, R.color.pastelGreenSecondary, R.color.dangerRed)),
    LIGHT_GREEN(Triple(R.color.deepGreen, R.color.deepGreenSecondary, R.color.dangerRed)),
    
    // Pink Theme - Pastel for Dark, Deep for Light
    DARK_PINK(Triple(R.color.pastelPink, R.color.pastelPinkSecondary, R.color.dangerRed)),
    LIGHT_PINK(Triple(R.color.deepPink, R.color.deepPinkSecondary, R.color.dangerRed)),
    
    // Yellow Theme - Pastel for Dark, Deep for Light
    DARK_YELLOW(Triple(R.color.pastelYellow, R.color.pastelYellowSecondary, R.color.dangerRed)),
    LIGHT_YELLOW(Triple(R.color.deepYellow, R.color.deepYellowSecondary, R.color.dangerRed))
}

fun AppTheme.getColorName(): String {
    return this.name.split("_").last().lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun AppTheme.getString(): String {
    return name.convertFromSnakeCase()
}

fun AppTheme.getThemeMode(): String {
    return if (name.contains("DARK")) "Dark" else "Light"
}