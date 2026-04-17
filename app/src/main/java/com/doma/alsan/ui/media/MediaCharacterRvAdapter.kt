package com.doma.alsan.ui.media

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.CharacterEdge
import com.doma.alsan.databinding.ListCircularBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.type.StaffLanguage
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class MediaCharacterRvAdapter(
    private val context: Context,
    list: List<CharacterEdge>,
    private val appSetting: AppSetting,
    private val width: Int,
    private val listener: MediaListener.MediaCharacterListener
) : BaseRecyclerViewAdapter<CharacterEdge, ListCircularBinding>(list) {

    private var selectedLanguage: StaffLanguage = StaffLanguage.JAPANESE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListCircularBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    fun updateSelectedLanguage(language: StaffLanguage) {
        selectedLanguage = language
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(private val binding: ListCircularBinding) : ViewHolder(binding) {
        override fun bind(item: CharacterEdge, index: Int) {
            binding.apply {
                ImageUtil.loadCircleImage(context, item.node.getImage(appSetting), circularItemImage)
                circularItemText.text = item.node.name.userPreferred

                // Find voice actor for the selected language
                val voiceActorRole = item.voiceActorRoles.find { 
                    it.voiceActor.language.replace(" ", "_").equals(selectedLanguage.name, ignoreCase = true)
                }
                val voiceActor = voiceActorRole?.voiceActor
                
                if (voiceActor != null && voiceActor.name.userPreferred.isNotBlank()) {
                    circularItemDescriptionText.visibility = View.VISIBLE
                    circularItemDescriptionText.text = voiceActor.name.userPreferred
                    circularItemDescriptionText.clicks {
                        listener.navigateToStaff(voiceActor)
                    }
                } else {
                    circularItemDescriptionText.visibility = View.GONE
                }

                root.clicks { listener.navigateToCharacter(item.node) }
            }
        }
    }
}