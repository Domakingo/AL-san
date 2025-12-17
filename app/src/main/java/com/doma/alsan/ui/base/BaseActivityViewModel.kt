package com.doma.alsan.ui.base

import com.doma.alsan.R
import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.helper.enums.AppTheme

class BaseActivityViewModel(private val userRepository: UserRepository) : BaseViewModel<Unit>() {

    override fun loadData(param: Unit) = Unit

    fun isLightMode(): Boolean {
        return userRepository.getAppTheme().name.contains("LIGHT")
    }

    fun getAppThemeResource(): Int {
        return when (userRepository.getAppTheme()) {
            // AniList Themes (Default)
            AppTheme.ANILIST_DARK_BLUE -> R.style.AppTheme_ThemeAniListDarkBlue
            AppTheme.ANILIST_LIGHT_BLUE -> R.style.AppTheme_ThemeAniListLightBlue
            
            // Purple Theme
            AppTheme.DARK_PURPLE -> R.style.AppTheme_ThemeAniListDarkPurple
            AppTheme.LIGHT_PURPLE -> R.style.AppTheme_ThemeAniListLightPurple
            
            // Green Theme
            AppTheme.DARK_GREEN -> R.style.AppTheme_ThemeAniListDarkGreen
            AppTheme.LIGHT_GREEN -> R.style.AppTheme_ThemeAniListLightGreen
            
            // Pink Theme
            AppTheme.DARK_PINK -> R.style.AppTheme_ThemeAniListDarkPink
            AppTheme.LIGHT_PINK -> R.style.AppTheme_ThemeAniListLightPink
            
            // Yellow Theme
            AppTheme.DARK_YELLOW -> R.style.AppTheme_ThemeAniListDarkYellow
            AppTheme.LIGHT_YELLOW -> R.style.AppTheme_ThemeAniListLightYellow
        }
    }
}