package com.doma.alsan.ui.base

import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.response.AnimeTheme
import com.doma.alsan.data.response.AnimeThemeEntry
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.MediaList
import com.doma.alsan.data.response.anilist.MediaListGroup
import com.doma.alsan.data.response.anilist.MediaTag
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.helper.pojo.ListItem
import com.doma.alsan.helper.pojo.SliderItem
import com.doma.alsan.helper.pojo.TextInputSetting
import com.doma.alsan.type.ScoreFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

interface DialogManager {
    fun showToast(message: Int)
    fun showToast(message: String)
    fun showMessageDialog(title: Int, message: Int, positiveButton: Int)
    fun showMessageDialog(title: String, message: String, positiveButton: Int)
    fun showActionDialog(title: String, message: String, positiveButton: Int, positiveAction: () -> Unit)
    fun showConfirmationDialog(
        title: Int,
        message: Int, 
        positiveButton: Int, 
        positiveAction: () -> Unit, 
        negativeButton: Int, 
        negativeAction: () -> Unit,
        thirdButton: Int? = null,
        thirdAction: (() -> Unit)? = null
    )
    fun showConfirmationDialog(
        title: String,
        message: String,
        positiveButton: Int,
        positiveAction: () -> Unit,
        negativeButton: Int,
        negativeAction: () -> Unit,
        thirdButton: Int? = null,
        thirdAction: (() -> Unit)? = null
    )

    fun <T> showListDialog(list: List<ListItem<T>>, action: (data: T, index: Int) -> Unit)
    fun showListDialog(adapter: BaseRecyclerViewAdapter<*, *>)
    fun dismissListDialog()

    fun showTextInputDialog(currentText: String, textInputSetting: TextInputSetting, action: (newText: String) -> Unit)
    fun showSliderDialog(sliderItem: SliderItem, useSingleSlider: Boolean = false, action: (minValue: Int?, maxValue: Int?) -> Unit)
    fun <T> showMultiSelectDialog(list: List<ListItem<T>>, selectedIndex: ArrayList<Int>, action: (data: List<T>) -> Unit)

    fun showTagDialog(list: List<ListItem<MediaTag?>>, selectedIndex: ArrayList<Int>, action: (data: List<MediaTag>) -> Unit)

    fun showProgressDialog(mediaType: MediaType, currentProgress: Int, maxProgress: Int?, isProgressVolume: Boolean, action: (newProgress: Int) -> Unit)
    fun showScoreDialog(scoreFormat: ScoreFormat, currentScore: Double, advancedScores: LinkedHashMap<String, Double>?, action: (newScore: Double, newAdvancedScores: LinkedHashMap<String, Double>?) -> Unit)

    fun showDatePicker(calendar: Calendar, action: (year: Int, month: Int, dayOfMonth: Int) -> Unit)

    fun showSpoilerDialog(spoilerText: String, onLinkClickAction: ((link: String) -> Unit)?)

    fun showShareSheet(text: String)

    fun showMediaQuickDetailDialog(media: Media)
    fun showMediaListQuickDetailDialog(userId: Int, mediaList: MediaList)

    fun showAnimeThemesDialog(media: Media, animeTheme: AnimeTheme, animeThemeEntry: AnimeThemeEntry?, action: (url: String?, videoId: String?, usePlayer: Boolean) -> Unit)

    fun showMediaFilterDialog(
        mediaFilter: MediaFilter,
        mediaType: MediaType,
        scoreFormat: ScoreFormat,
        isUserList: Boolean,
        hasBigList: Boolean,
        isViewer: Boolean,
        listSections: List<MediaListGroup>,
        selectedSectionIndex: Int,
        isAllListPositionAtTop: Boolean,
        action: (mediaFilter: MediaFilter, sectionIndex: Int) -> Unit
    )
}