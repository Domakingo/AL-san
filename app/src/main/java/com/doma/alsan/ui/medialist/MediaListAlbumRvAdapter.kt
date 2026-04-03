package com.doma.alsan.ui.medialist

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.entity.ListStyle
import com.doma.alsan.data.response.anilist.MediaListOptions
import com.doma.alsan.databinding.ListMediaListAlbumBinding
import com.doma.alsan.databinding.ListTitleBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.pojo.MediaListItem
import com.doma.alsan.helper.utils.ImageUtil

class MediaListAlbumRvAdapter(
    context: Context,
    list: List<MediaListItem>,
    isViewer: Boolean,
    appSetting: AppSetting,
    listStyle: ListStyle,
    mediaListOptions: MediaListOptions,
    private val listener: MediaListListener
) : BaseMediaListRvAdapter(context, list, isViewer, appSetting, listStyle, mediaListOptions) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MediaListItem.VIEW_TYPE_TITLE -> {
                val view = ListTitleBinding.inflate(inflater, parent, false)
                TitleViewHolder(view)
            }
            else -> {
                val view = ListMediaListAlbumBinding.inflate(inflater, parent, false)
                ListItemViewHolder(view)
            }
        }
    }

    inner class ListItemViewHolder(private val binding: ListMediaListAlbumBinding) : ItemViewHolder(binding) {
        override fun bind(item: MediaListItem, index: Int) {
            val mediaList = item.mediaList
            val media = mediaList.media
            binding.apply {
                // cover
                ImageUtil.loadImage(context, getCoverImage(media), mediaListCoverImage)

                // title
                mediaListTitleText.text = getTitle(media)
                mediaListTitleText.clicks {
                    listener.navigateToMedia(media)
                }
                mediaListTitleText.setOnLongClickListener {
                    listener.copyMediaTitle(getTitle(media))
                    true
                }

                // airing indicator
                mediaListAiringIndicator.show(shouldShowAiringIndicator(media))
                mediaListAiringIndicator.setImageResource(getAiringIndicatorIcon(mediaList))
                mediaListAiringIndicator.clicks {
                    listener.showAiringText(getAiringText(mediaList))
                }

                // priority
                mediaListPriority.show(shouldShowPriority(mediaList))
                mediaListPriority.setBackgroundColor(getPriorityColor(mediaList))

                // score
                handleScoring(mediaListScoreLayout, mediaListScoreIcon, mediaListScoreText, mediaListScoreSmiley, mediaList)
                mediaListScoreLayout.clicks {
                    if (!isViewer) {
                        listener.showQuickDetail(mediaList)
                        return@clicks
                    }

                    listener.showScoreDialog(mediaList)
                }

                // progress
                mediaListProgressText.text = getProgressText(mediaList)
                mediaListProgressText.show(shouldShowProgress(media))
                mediaListProgressText.clicks {
                    if (!isViewer) {
                        listener.showQuickDetail(mediaList)
                        return@clicks
                    }

                    listener.showProgressDialog(mediaList, false)
                }

                mediaListProgressVolumeText.text = getProgressVolumeText(mediaList)
                mediaListProgressVolumeText.show(shouldShowProgressVolume(media))
                mediaListProgressVolumeText.clicks {
                    if (!isViewer) {
                        listener.showQuickDetail(mediaList)
                        return@clicks
                    }

                    listener.showProgressDialog(mediaList, true)
                }

                // root
                root.setOnLongClickListener {
                    if (shouldShowQuickDetail()) {
                        it.isPressed = false
                        listener.showQuickDetail(mediaList)
                    }

                    true
                }

                root.clicks {
                    if (!isViewer) {
                        listener.navigateToMedia(media)
                        return@clicks
                    }

                    listener.navigateToListEditor(mediaList)
                }

                // theme
                val primaryColor = listStyle.getPrimaryColor(context)
                mediaListTitleText.setTextColor(primaryColor)
                mediaListScoreText.setTextColor(primaryColor)
                mediaListScoreSmiley.imageTintList = ColorStateList.valueOf(primaryColor)
                mediaListProgressText.setTextColor(primaryColor)
                mediaListProgressVolumeText.setTextColor(primaryColor)

                val secondaryColor = listStyle.getSecondaryColor(context)
                mediaListAiringIndicator.imageTintList = ColorStateList.valueOf(secondaryColor)

                val cardColor = listStyle.getCardColor(context)
                mediaListCardBackground.setCardBackgroundColor(cardColor)
            }
        }
    }
}