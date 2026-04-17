package com.doma.alsan.ui.media

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.Recommendation
import com.doma.alsan.databinding.ListMediaRecommendationBinding
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.helper.extensions.*
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter
import com.doma.alsan.type.MediaStatus

class MediaRecommendationsRvAdapter(
    private val context: Context,
    list: List<Recommendation>,
    private val appSetting: AppSetting,
    private val width: Int,
    private val listener: MediaListener.MediaRecommendationsListener
) : BaseRecyclerViewAdapter<Recommendation, ListMediaRecommendationBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListMediaRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        view.root.layoutParams.width = (width.toDouble() / context.resources.getInteger(R.integer.horizontalListRelationDivider)).toInt()
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListMediaRecommendationBinding) : ViewHolder(binding) {
        override fun bind(item: Recommendation, index: Int) {
            binding.apply {
                item.mediaRecommendation?.let {
                    ImageUtil.loadImage(context, it.getCoverImage(appSetting), recommendationCoverImage)
                    recommendationTitleText.text = it.getTitle(appSetting)
                    recommendationFormatText.text = it.getFormattedMediaFormat(true)
                    recommendationRatingText.text = item.rating.getNumberFormatting()
                    
                    root.clicks {
                        listener.navigateToMedia(it)
                    }
                }
            }
        }
    }
}