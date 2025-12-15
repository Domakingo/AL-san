package com.doma.alsan.ui.seasonal

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.databinding.ListMediaListLinearBinding
import com.doma.alsan.databinding.ListSeasonalGridBinding
import com.doma.alsan.databinding.ListSeasonalLinearBinding
import com.doma.alsan.databinding.ListTitleBinding
import com.doma.alsan.helper.extensions.*
import com.doma.alsan.helper.pojo.SeasonalItem
import com.doma.alsan.helper.utils.ImageUtil

class SeasonalGridRvAdapter(
    context: Context,
    list: List<SeasonalItem>,
    appSetting: AppSetting,
    listener: SeasonalListener
) : BaseSeasonalRvAdapter(context, list, appSetting, listener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SeasonalItem.VIEW_TYPE_TITLE -> {
                val view = ListTitleBinding.inflate(inflater, parent, false)
                TitleViewHolder(view)
            }
            else -> {
                val view = ListSeasonalGridBinding.inflate(inflater, parent, false)
                ListItemViewHolder(view)
            }
        }
    }

    inner class ListItemViewHolder(private val binding: ListSeasonalGridBinding) : ItemViewHolder(binding) {
        override fun bind(item: SeasonalItem, index: Int) {
            val media = item.media
            with(binding) {
                ImageUtil.loadImage(context, getCoverImage(media), seasonalCoverImage)
                seasonalTitle.text = getTitle(media)

                seasonalMediaListStatusLayout.isEnabled = media.mediaListEntry == null
                ImageUtil.loadImage(context, getMediaListStatusIcon(media), seasonalMediaListStatusIcon)

                val statusColor = getMediaListStatusColor(media)
                seasonalMediaListStatusIcon.imageTintList = ColorStateList.valueOf(statusColor)

                root.clicks {
                    listener.navigateToMedia(media)
                }

                root.setOnLongClickListener {
                    listener.showQuickDetail(media)
                    true
                }

                seasonalMediaListStatusLayout.clicks {
                    listener.addToPlanning(media)
                }
            }
        }
    }
}