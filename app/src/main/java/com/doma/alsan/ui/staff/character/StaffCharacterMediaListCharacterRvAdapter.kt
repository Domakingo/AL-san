package com.doma.alsan.ui.staff.character

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.databinding.ListMediaCharacterVoiceActorBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class StaffCharacterMediaListCharacterRvAdapter(
    private val context: Context,
    list: List<Character>,
    private val appSetting: AppSetting,
    private val listener: StaffCharacterListListener
) : BaseRecyclerViewAdapter<Character, ListMediaCharacterVoiceActorBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListMediaCharacterVoiceActorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListMediaCharacterVoiceActorBinding) : ViewHolder(binding) {
        override fun bind(item: Character, index: Int) {
            binding.apply {
                voiceActorName.text = item.name.userPreferred
                voiceActorRoleNote.show(false)
                voiceActorDubGroup.show(false)
                ImageUtil.loadCircleImage(context, item.getImage(appSetting), voiceActorImage)
                root.clicks { listener.navigateToCharacter(item) }
            }
        }
    }
}