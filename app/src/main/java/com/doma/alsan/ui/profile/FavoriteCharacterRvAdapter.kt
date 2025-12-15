package com.doma.alsan.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.databinding.ListCircularBinding
import com.doma.alsan.databinding.ListRectangleBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class FavoriteCharacterRvAdapter(
    private val context: Context,
    list: List<Character>,
    private val appSetting: AppSetting,
    private val listener: ProfileListener.FavoriteCharacterListener
) : BaseRecyclerViewAdapter<Character, ListRectangleBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListRectangleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListRectangleBinding) : ViewHolder(binding) {
        override fun bind(item: Character, index: Int) {
            binding.apply {
                val image = item.getImage(appSetting)
                ImageUtil.loadImage(context, image, rectangleItemImage)
                rectangleItemText.show(false)
                root.clicks { listener.navigateToCharacter(item) }
            }
        }
    }
}