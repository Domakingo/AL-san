package com.doma.alsan.ui.staff

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.databinding.*
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.pojo.StaffItem
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.helper.utils.MarkdownUtil
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class StaffRvAdapter(
    private val context: Context,
    list: List<StaffItem>,
    private val appSetting: AppSetting,
    private val width: Int,
    private val listener: StaffListener
) : BaseRecyclerViewAdapter<StaffItem, ViewBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            StaffItem.VIEW_TYPE_DETAILS -> {
                // Not used in list yet? Or same as bio?
                val view = LayoutTitleAndTextBinding.inflate(inflater, parent, false)
                BioViewHolder(view)
            }
            StaffItem.VIEW_TYPE_STATS -> {
                val view = LayoutMediaStatsBinding.inflate(inflater, parent, false)
                StatsViewHolder(view)
            }
            StaffItem.VIEW_TYPE_CHARACTER_GROUP, StaffItem.VIEW_TYPE_MEDIA_GROUP -> {
                val view = LayoutTitleAndListBinding.inflate(inflater, parent, false)
                GroupViewHolder(view)
            }
            StaffItem.VIEW_TYPE_CHARACTER_ITEM -> {
                val view = ListCardImageAndTextBinding.inflate(inflater, parent, false)
                CharacterItemViewHolder(view)
            }
            StaffItem.VIEW_TYPE_MEDIA_ITEM -> {
                val view = ListCardImageAndTextBinding.inflate(inflater, parent, false)
                MediaItemViewHolder(view)
            }
            else -> {
                val view = LayoutTitleAndTextBinding.inflate(inflater, parent, false)
                BioViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    inner class StatsViewHolder(private val binding: LayoutMediaStatsBinding) : ViewHolder(binding) {
        override fun bind(item: StaffItem, index: Int) {
            binding.apply {
                mediaStatsAverageScore.text = item.staff.favourites.toString()
                mediaStatsMeanScore.show(false)
                mediaStatsPopularity.text = (item.staff.staffMedia.pageInfo.total + item.staff.characterMedia.pageInfo.total + item.staff.characters.pageInfo.total).toString()
                mediaStatsFavorites.text = item.staff.language
            }
        }
    }

    inner class BioViewHolder(private val binding: LayoutTitleAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: StaffItem, index: Int) {
            // Deprecated
        }
    }

    inner class GroupViewHolder(private val binding: LayoutTitleAndListBinding) : ViewHolder(binding) {
        override fun bind(item: StaffItem, index: Int) {
            binding.apply {
                titleText.text = item.title
                seeMoreText.show(false)
                listRecyclerView.show(false)
            }
        }
    }

    inner class CharacterItemViewHolder(private val binding: ListCardImageAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: StaffItem, index: Int) {
            val edge = item.characterEdge ?: return
            binding.apply {
                ImageUtil.loadImage(context, edge.node.image.large, cardImage)
                cardText.text = edge.node.name.userPreferred
                cardSubtitle.text = edge.role?.name ?: ""
                cardInfoLayout.show(false)
                root.clicks {
                    listener.staffCharacterListener.navigateToCharacter(edge.node)
                }
            }
        }
    }

    inner class MediaItemViewHolder(private val binding: ListCardImageAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: StaffItem, index: Int) {
            val edge = item.mediaEdge ?: return
            binding.apply {
                ImageUtil.loadImage(context, edge.node.getCoverImage(appSetting), cardImage)
                cardText.text = edge.node.getTitle(appSetting)
                cardSubtitle.text = edge.staffRole
                cardInfoLayout.show(false)
                root.clicks {
                    listener.staffMediaListener.navigateToMedia(edge.node)
                }
            }
        }
    }
}