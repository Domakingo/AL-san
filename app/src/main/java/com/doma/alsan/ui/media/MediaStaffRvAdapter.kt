package com.doma.alsan.ui.media

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.data.response.anilist.StaffEdge
import com.doma.alsan.databinding.ListCircularBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class MediaStaffRvAdapter(
    private val context: Context,
    list: List<StaffEdge>,
    private val appSetting: AppSetting,
    private val width: Int,
    private val listener: MediaListener.MediaStaffListener
) : BaseRecyclerViewAdapter<StaffEdge, ListCircularBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListCircularBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListCircularBinding) : ViewHolder(binding) {
        override fun bind(item: StaffEdge, index: Int) {
            binding.apply {
                ImageUtil.loadCircleImage(context, item.node.getImage(appSetting), circularItemImage)
                circularItemText.text = item.node.name.userPreferred
                circularItemText.setLines(1)
                circularItemText.maxLines = 1
                circularItemDescriptionText.text = item.role
                circularItemDescriptionText.show(true)

                root.clicks { listener.navigateToStaff(item.node) }
            }
        }
    }
}