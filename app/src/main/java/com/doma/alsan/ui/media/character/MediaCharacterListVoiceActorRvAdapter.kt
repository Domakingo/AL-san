package com.doma.alsan.ui.media.character

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.StaffRoleType
import com.doma.alsan.databinding.ListMediaCharacterVoiceActorBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class MediaCharacterListVoiceActorRvAdapter(
    private val context: Context,
    list: List<StaffRoleType>,
    private val appSetting: AppSetting,
    private val listener: MediaCharacterListRvAdapter.MediaCharacterListListener
) : BaseRecyclerViewAdapter<StaffRoleType, ListMediaCharacterVoiceActorBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListMediaCharacterVoiceActorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListMediaCharacterVoiceActorBinding) : ViewHolder(binding) {
        override fun bind(item: StaffRoleType, index: Int) {
            binding.apply {
                voiceActorName.text = item.voiceActor.name.userPreferred
                voiceActorRoleNote.text = "(${item.roleNote})"
                voiceActorRoleNote.show(item.roleNote.isNotBlank())
                voiceActorDubGroup.text = item.dubGroup
                voiceActorDubGroup.show(item.dubGroup.isNotBlank())
                ImageUtil.loadCircleImage(context, item.voiceActor.getImage(appSetting), voiceActorImage)

                root.clicks { listener.navigateToStaff(item.voiceActor) }
            }
        }
    }
}