package com.doma.alsan.ui.character

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.databinding.*
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.pojo.CharacterItem
import com.doma.alsan.helper.utils.GridSpacingItemDecoration
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.helper.utils.MarkdownUtil
import com.doma.alsan.helper.utils.SpaceItemDecoration
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class CharacterRvAdapter(
    private val context: Context,
    list: List<CharacterItem>,
    private val appSetting: AppSetting,
    private val width: Int,
    private val listener: CharacterListener
) : BaseRecyclerViewAdapter<CharacterItem, ViewBinding>(list) {

    private var voiceActorAdapter: CharacterVoiceActorRvAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CharacterItem.VIEW_TYPE_STATS -> {
                val view = LayoutMediaStatsBinding.inflate(inflater, parent, false)
                StatsViewHolder(view)
            }
            CharacterItem.VIEW_TYPE_MEDIA_GROUP -> {
                val view = LayoutTitleAndListBinding.inflate(inflater, parent, false)
                MediaGroupViewHolder(view)
            }
            CharacterItem.VIEW_TYPE_MEDIA_ITEM -> {
                val view = ListCardImageAndTextBinding.inflate(inflater, parent, false)
                MediaItemViewHolder(view)
            }
            CharacterItem.VIEW_TYPE_VOICE_ACTOR_ITEM -> {
                val view = ListCardImageAndTextBinding.inflate(inflater, parent, false)
                VoiceActorItemViewHolder(view)
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
        override fun bind(item: CharacterItem, index: Int) {
            binding.apply {
                mediaStatsAverageScore.text = item.character.favourites.toString()
                mediaStatsMeanScore.show(false)
                mediaStatsPopularity.text = item.character.media.pageInfo.total.toString()
                mediaStatsFavorites.text = item.character.bloodType.ifBlank { "-" }
            }
        }
    }

    inner class BioViewHolder(private val binding: LayoutTitleAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: CharacterItem, index: Int) {
            // Deprecated
        }
    }

    inner class VoiceActorItemViewHolder(private val binding: ListCardImageAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: CharacterItem, index: Int) {
            val voiceActor = item.voiceActor ?: return
            binding.apply {
                ImageUtil.loadImage(context, voiceActor.voiceActor.image.large, cardImage)
                cardText.text = voiceActor.voiceActor.name.userPreferred
                cardSubtitle.text = voiceActor.voiceActor.language
                cardInfoLayout.show(false)
                root.clicks {
                    listener.navigateToStaff(voiceActor.voiceActor)
                }
            }
        }
    }

    inner class MediaGroupViewHolder(private val binding: LayoutTitleAndListBinding) : ViewHolder(binding) {
        override fun bind(item: CharacterItem, index: Int) {
            binding.apply {
                titleText.text = item.title
                seeMoreText.show(false)
                listRecyclerView.show(false)
            }
        }
    }

    inner class MediaItemViewHolder(private val binding: ListCardImageAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: CharacterItem, index: Int) {
            val mediaEdge = item.mediaEdge ?: return
            binding.apply {
                ImageUtil.loadImage(context, mediaEdge.node.getCoverImage(appSetting), cardImage)
                cardText.text = mediaEdge.node.getTitle(appSetting)
                cardSubtitle.text = mediaEdge.getCharacterRoleString()
                cardInfoLayout.show(false)
                root.clicks {
                    listener.characterMediaListener.navigateToMedia(mediaEdge.node)
                }
            }
        }
    }
}