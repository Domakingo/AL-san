package com.doma.alsan.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.databinding.ListRectangleBinding
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class FavoriteMediaRvAdapter(
    private val context: Context,
    list: List<Media>,
    private val mediaType: MediaType,
    private val appSetting: AppSetting,
    private val listener: ProfileListener.FavoriteMediaListener
) : BaseRecyclerViewAdapter<Media, ListRectangleBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListRectangleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListRectangleBinding) : ViewHolder(binding) {
        override fun bind(item: Media, index: Int) {
            binding.apply {
                val image = item.getCoverImage(appSetting)
                ImageUtil.loadImage(context, image, rectangleItemImage)
                rectangleItemText.show(false)
                
                // Show title overlay
                val title = item.getTitle(appSetting)
                rectangleItemNameOverlay.show(title.isNotBlank())
                rectangleItemNameText.text = title
                
                root.clicks { listener.navigateToMedia(item, mediaType) }
            }
        }
    }
}