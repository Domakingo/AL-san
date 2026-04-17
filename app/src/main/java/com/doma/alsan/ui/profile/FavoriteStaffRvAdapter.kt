package com.doma.alsan.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.databinding.ListCircularBinding
import com.doma.alsan.databinding.ListRectangleBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class FavoriteStaffRvAdapter(
    private val context: Context,
    list: List<Staff>,
    private val appSetting: AppSetting,
    private val listener: ProfileListener.FavoriteStaffListener
) : BaseRecyclerViewAdapter<Staff, ListRectangleBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListRectangleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListRectangleBinding) : ViewHolder(binding) {
        override fun bind(item: Staff, index: Int) {
            binding.apply {
                val image = item.getImage(appSetting)
                ImageUtil.loadImage(context, image, rectangleItemImage)
                
                // Show name below image instead of overlay
                rectangleItemText.show(true)
                rectangleItemText.text = item.name.userPreferred
                
                rectangleItemNameOverlay.show(false)
                
                root.clicks { listener.navigateToStaff(item) }
            }
        }
    }
}