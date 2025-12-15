package com.doma.alsan.ui.media.character

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.CharacterEdge
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.databinding.ListCardImageAndTextBinding
import com.doma.alsan.databinding.ListLoadingBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.convertFromSnakeCase
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.helper.utils.SpaceItemDecoration
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class MediaCharacterListRvAdapter(
    private val context: Context,
    list: List<CharacterEdge?>,
    private val appSetting: AppSetting,
    private val listener: MediaCharacterListListener
): BaseRecyclerViewAdapter<CharacterEdge?, ViewBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_LOADING -> {
                val view = ListLoadingBinding.inflate(inflater, parent, false)
                LoadingViewHolder(view)
            }
            else -> {
                val view = ListCardImageAndTextBinding.inflate(inflater, parent, false)
                view.cardRecyclerView.addItemDecoration(SpaceItemDecoration(bottom = context.resources.getDimensionPixelSize(R.dimen.marginSmall)))
                ItemViewHolder(view)
            }
        }
    }

    inner class ItemViewHolder(private val binding: ListCardImageAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: CharacterEdge?, index: Int) {
            if (item == null)
                return

            binding.apply {
                ImageUtil.loadImage(context, item.node.getImage(appSetting), cardImage)
                cardText.text = item.node.name.userPreferred
                cardText.setLines(2)
                cardText.maxLines = 2
                cardSubtitle.text = item.role?.name?.convertFromSnakeCase(false)
                cardSubtitle.setLines(1)
                cardSubtitle.maxLines = 1

                cardRecyclerView.adapter = MediaCharacterListVoiceActorRvAdapter(context, item.voiceActorRoles, appSetting, listener)
                cardRecyclerView.show(item.voiceActorRoles.isNotEmpty())

                root.clicks { listener.navigateToCharacter(item.node) }
            }
        }
    }

    inner class LoadingViewHolder(private val binding: ListLoadingBinding) : ViewHolder(binding) {
        override fun bind(item: CharacterEdge?, index: Int) {
            // do nothing
        }
    }

    interface MediaCharacterListListener {
        fun navigateToCharacter(character: Character)
        fun navigateToStaff(staff: Staff)
    }
}