package com.doma.alsan.helper.pojo

import com.doma.alsan.helper.enums.AppTheme

/**
 * Represents a theme group with both light and dark variants
 */
data class AppThemeItem(
    val header: String? = null,
    val appTheme: AppTheme? = null,
    val lightTheme: AppTheme? = null,
    val darkTheme: AppTheme? = null,
    val themeName: String? = null
)