package com.doma.alsan.ui.staff.character

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.databinding.ListStaffCharacterMediaBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter
import com.doma.alsan.ui.staff.StaffListener

class StaffCharacterListMediaRvAdapter(
    private val context: Context,
    list: List<Media>,
    private val appSetting: AppSetting,
    private val listener: StaffCharacterListListener
) : BaseRecyclerViewAdapter<Media, ListStaffCharacterMediaBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListStaffCharacterMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListStaffCharacterMediaBinding) : ViewHolder(binding) {
        override fun bind(item: Media, index: Int) {
            binding.apply {
                mediaName.text = item.getTitle(appSetting)
                ImageUtil.loadRectangleImage(context, item.getCoverImage(appSetting), mediaCoverImage)
                root.clicks { listener.navigateToMedia(item) }
            }
        }
    }
}