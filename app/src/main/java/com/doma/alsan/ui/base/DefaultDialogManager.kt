package com.doma.alsan.ui.base

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.doma.alsan.data.response.AnimeTheme
import com.doma.alsan.data.response.AnimeThemeEntry
import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.MediaList
import com.doma.alsan.data.response.anilist.MediaListGroup
import com.doma.alsan.data.response.anilist.MediaTag
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.helper.pojo.ListItem
import com.doma.alsan.helper.pojo.SliderItem
import com.doma.alsan.helper.pojo.TextInputSetting
import com.doma.alsan.type.ScoreFormat
import com.doma.alsan.ui.common.*
import com.doma.alsan.ui.common.BottomSheetTagDialog
import com.doma.alsan.ui.editor.BottomSheetProgressDialog
import com.doma.alsan.ui.editor.BottomSheetScoreDialog
import com.doma.alsan.ui.media.MediaListener
import com.doma.alsan.ui.media.themes.BottomSheetMediaThemesDialog
import com.doma.alsan.ui.medialist.BottomSheetMediaFilterDialog
import com.doma.alsan.ui.medialist.BottomSheetMediaListQuickDetailDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class DefaultDialogManager(private val context: Context) : DialogManager {

    private var bottomSheetListDialog: BottomSheetListDialog? = null
    // removed unused dialog references to prevent leaks

    private var datePickerDialog: DatePickerDialog? = null

    private var isToastShowing = false

    override fun showToast(message: Int) {
        if (!isToastShowing) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            startToastTimer()
        }
    }

    override fun showToast(message: String) {
        if (!isToastShowing) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            startToastTimer()
        }
    }

    private fun startToastTimer() {
        isToastShowing = true

        Single.timer(2, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    isToastShowing = false
                },
                {

                }
            )
    }

    override fun showMessageDialog(title: Int, message: Int, positiveButton: Int) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton, null)
            .setCancelable(false)
            .show()
    }

    override fun showMessageDialog(title: String, message: String, positiveButton: Int) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton, null)
            .setCancelable(false)
            .show()
    }

    override fun showActionDialog(
        title: String,
        message: String,
        positiveButton: Int,
        positiveAction: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun showConfirmationDialog(
        title: Int,
        message: Int,
        positiveButton: Int,
        positiveAction: () -> Unit,
        negativeButton: Int,
        negativeAction: () -> Unit,
        thirdButton: Int?,
        thirdAction: (() -> Unit)?
    ) {
        val builder = AlertDialog.Builder(context)

        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButton) { _, _ -> positiveAction() }
            setNegativeButton(negativeButton) { _, _ -> negativeAction() }
            if (thirdButton != null) setNeutralButton(thirdButton) { _, _ -> thirdAction?.invoke() }
            setCancelable(false)
            show()
        }
    }

    override fun showConfirmationDialog(
        title: String,
        message: String,
        positiveButton: Int,
        positiveAction: () -> Unit,
        negativeButton: Int,
        negativeAction: () -> Unit,
        thirdButton: Int?,
        thirdAction: (() -> Unit)?
    ) {
        val builder = AlertDialog.Builder(context)

        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButton) { _, _ -> positiveAction() }
            setNegativeButton(negativeButton) { _, _ -> negativeAction() }
            if (thirdButton != null) setNeutralButton(thirdButton) { _, _ -> thirdAction?.invoke() }
            setCancelable(false)
            show()
        }
    }

    override fun <T> showListDialog(
        list: List<ListItem<T>>,
        action: (data: T, index: Int) -> Unit
    ) {
        val adapter = BottomSheetListRvAdapter(context, list, object : BottomSheetListRvAdapter.BottomSheetListListener<T> {
            override fun getSelectedItem(data: T, index: Int) {
                dismissListDialog()
                action(data, index)
            }
        })
        bottomSheetListDialog = BottomSheetListDialog.newInstance(adapter) {
            bottomSheetListDialog = null
        }
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            bottomSheetListDialog?.show(it, null)
        }
    }

    override fun showListDialog(adapter: BaseRecyclerViewAdapter<*, *>) {
        bottomSheetListDialog = BottomSheetListDialog.newInstance(adapter) {
            bottomSheetListDialog = null
        }
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            bottomSheetListDialog?.show(it, null)
        }
    }

    override fun dismissListDialog() {
        bottomSheetListDialog?.dismiss()
        bottomSheetListDialog = null
    }

    override fun showTextInputDialog(
        currentText: String,
        textInputSetting: TextInputSetting,
        action: (newText: String) -> Unit
    ) {
        val dialog = BottomSheetTextInputDialog.newInstance(currentText, textInputSetting, object : BottomSheetTextInputDialog.BottomSheetTextInputListener {
            override fun getNewText(newText: String) {
                action(newText)
            }
        })
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }

    override fun showSliderDialog(
        sliderItem: SliderItem,
        useSingleSlider: Boolean,
        action: (minValue: Int?, maxValue: Int?) -> Unit
    ) {
        val dialog = BottomSheetSliderDialog.newInstance(sliderItem, useSingleSlider, object : BottomSheetSliderDialog.BottomSheetSliderListener {
            override fun getNewValues(minValue: Int?, maxValue: Int?) {
                action(minValue, maxValue)
            }
        })
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }

    override fun <T> showMultiSelectDialog(
        list: List<ListItem<T>>,
        selectedIndex: ArrayList<Int>,
        action: (data: List<T>) -> Unit
    ) {
        val adapter = BottomSheetMultiSelectRvAdapter(context, list, selectedIndex, object : BottomSheetMultiSelectRvAdapter.BottomSheetMultiSelectListener<T> {
            override fun getSelectedItems(data: List<T>, index: List<Int>) {
                action(data)
            }
        })
        bottomSheetListDialog = BottomSheetListDialog.newInstance(adapter) {
            bottomSheetListDialog = null
        }
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            bottomSheetListDialog?.show(it, null)
        }
    }

    override fun showTagDialog(
        list: List<ListItem<MediaTag?>>,
        selectedIndex: ArrayList<Int>,
        action: (data: List<MediaTag>) -> Unit
    ) {
        val dialog = BottomSheetTagDialog.newInstance(list, selectedIndex, object : BottomSheetTagDialog.TagDialogListener {
            override fun getSelectedTags(list: List<MediaTag>) {
                action(list)
            }
        })
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }

    override fun showProgressDialog(
        mediaType: MediaType,
        currentProgress: Int,
        maxProgress: Int?,
        isProgressVolume: Boolean,
        action: (newProgress: Int) -> Unit
    ) {
        val dialog = BottomSheetProgressDialog.newInstance(mediaType, currentProgress, maxProgress, isProgressVolume, object : BottomSheetProgressDialog.BottomSheetProgressListener {
            override fun getNewProgress(newProgress: Int) {
                action(newProgress)
            }
        })
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }

    override fun showScoreDialog(
        scoreFormat: ScoreFormat,
        currentScore: Double,
        advancedScores: LinkedHashMap<String, Double>?,
        action: (newScore: Double, newAdvancedScores: LinkedHashMap<String, Double>?) -> Unit
    ) {
        val dialog = BottomSheetScoreDialog.newInstance(scoreFormat, currentScore, advancedScores, object : BottomSheetScoreDialog.BottomSheetScoreListener {
            override fun getNewScore(newScore: Double, newAdvancedScores: LinkedHashMap<String, Double>?) {
                action(newScore, newAdvancedScores)
            }
        })
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }

    override fun showDatePicker(calendar: Calendar, action: (year: Int, month: Int, dayOfMonth: Int) -> Unit) {
        datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                action(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog?.show()
    }


    override fun showSpoilerDialog(
        spoilerText: String,
        onLinkClickAction: ((link: String) -> Unit)?
    ) {
        val dialog = BottomSheetSpoilerDialog.newInstance(spoilerText, object : BottomSheetSpoilerDialog.SpoilerListener {
            override fun onLinkClick(link: String) {
                onLinkClickAction?.invoke(link)
            }
        })
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }

    override fun showShareSheet(text: String) {
        val sendIntent: Intent = Intent().apply {
            this.action = Intent.ACTION_SEND
            this.putExtra(Intent.EXTRA_TEXT, text)
            this.type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    override fun showMediaQuickDetailDialog(media: Media) {
        val dialog = BottomSheetMediaQuickDetailDialog.newInstance(media)
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }

    override fun showMediaListQuickDetailDialog(userId: Int, mediaList: MediaList) {
        val dialog = BottomSheetMediaListQuickDetailDialog.newInstance(userId, mediaList)
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }

    override fun showAnimeThemesDialog(
        media: Media,
        animeTheme: AnimeTheme,
        animeThemeEntry: AnimeThemeEntry?,
        action: (url: String?, videoId: String?, usePlayer: Boolean) -> Unit
    ) {
        val dialog = BottomSheetMediaThemesDialog.newInstance(media, animeTheme, animeThemeEntry, object : BottomSheetMediaThemesDialog.BottomSheetMediaThemeListener {
            override fun playWithPlayer(url: String) {
                action(url, null, true)
            }

            override fun playWithYouTube(videoId: String) {
                action(null, videoId, false)
            }

            override fun playWithSpotify(url: String) {
                action(url, null, false)
            }
        })
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }

    override fun showMediaFilterDialog(
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
    ) {
        val dialog = BottomSheetMediaFilterDialog.newInstance(
            mediaFilter,
            mediaType,
            scoreFormat,
            isUserList,
            hasBigList,
            isViewer,
            listSections,
            selectedSectionIndex,
            isAllListPositionAtTop,
            object : BottomSheetMediaFilterDialog.MediaFilterListener {
                override fun onFilterApplied(mediaFilter: MediaFilter, sectionIndex: Int) {
                    action(mediaFilter, sectionIndex)
                }
            }
        )
        (context as? AppCompatActivity?)?.supportFragmentManager?.let {
            dialog.show(it, null)
        }
    }
}