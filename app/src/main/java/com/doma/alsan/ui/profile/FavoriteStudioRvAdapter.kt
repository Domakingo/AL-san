package com.doma.alsan.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.data.response.anilist.Studio
import com.doma.alsan.databinding.ListCardTextBinding
import com.doma.alsan.databinding.ListCircularBinding
import com.doma.alsan.databinding.ListRectangleBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class FavoriteStudioRvAdapter(
    private val context: Context,
    list: List<Studio>,
    private val listener: ProfileListener.FavoriteStudioListener
) : BaseRecyclerViewAdapter<Studio, ListCardTextBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListCardTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListCardTextBinding) : ViewHolder(binding) {
        override fun bind(item: Studio, index: Int) {
            binding.apply {
                cardIcon.show(false)
                cardText.text = item.name
                root.clicks { listener.navigateToStudio(item) }
            }
        }
    }
}