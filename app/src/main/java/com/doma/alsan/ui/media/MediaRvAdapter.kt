package com.doma.alsan.ui.media

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.flexbox.FlexboxLayoutManager
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.AnimeTheme
import com.doma.alsan.data.response.AnimeThemeEntry
import com.doma.alsan.data.response.Genre
import com.doma.alsan.data.response.anilist.MediaTag
import com.doma.alsan.data.response.anilist.PageInfo
import com.doma.alsan.databinding.*
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.helper.extensions.*
import com.doma.alsan.helper.pojo.MediaItem
import com.doma.alsan.helper.utils.*
import com.doma.alsan.helper.utils.GridSpacingItemDecoration
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter
import com.doma.alsan.ui.common.GenreRvAdapter
import com.doma.alsan.ui.common.TextRvAdapter
import com.doma.alsan.type.StaffLanguage

class MediaRvAdapter(
    private val context: Context,
    list: List<MediaItem>,
    private val appSetting: AppSetting,
    private val width: Int,
    private val listener: MediaListener
) : BaseRecyclerViewAdapter<MediaItem, ViewBinding>(list) {

    private var characterAdapter: MediaCharacterRvAdapter? = null
    private var synonymsAdapter: TextRvAdapter? = null
    private var studiosAdapter: TextRvAdapter? = null
    private var producersAdapter: TextRvAdapter? = null
    private var serializationsAdapter: TextRvAdapter? = null
    private var staffAdapter: MediaStaffRvAdapter? = null
    private var relationsAdapter: MediaRelationsRvAdapter? = null
    private var recommendationsAdapter: MediaRecommendationsRvAdapter? = null
    private var linksAdapter: MediaLinksRvAdapter? = null
    
    private var selectedLanguage: StaffLanguage = StaffLanguage.JAPANESE
    private var characterViewHolderBinding: LayoutHorizontalListBinding? = null

    fun updateSelectedLanguage(language: StaffLanguage) {
        selectedLanguage = language
        characterViewHolderBinding?.horizontalListLanguageSelector?.text = language.getString()
        characterAdapter?.updateSelectedLanguage(language)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            MediaItem.VIEW_TYPE_INFO -> {
                val view = LayoutMediaInfoBinding.inflate(inflater, parent, false)
                synonymsAdapter = TextRvAdapter(context, listOf())
                view.mediaInfoSynonymsRecyclerView.adapter = synonymsAdapter
                studiosAdapter = TextRvAdapter(context, listOf(), getTextListener())
                view.mediaInfoStudiosRecyclerView.adapter = studiosAdapter
                producersAdapter = TextRvAdapter(context, listOf(), getTextListener())
                view.mediaInfoProducersRecyclerView.adapter = producersAdapter
                serializationsAdapter = TextRvAdapter(context, listOf())
                view.mediaInfoSerializationsRecyclerView.adapter = serializationsAdapter
                return InfoViewHolder(view)
            }
            MediaItem.VIEW_TYPE_TAGS -> {
                val view = LayoutTitleAndListBinding.inflate(inflater, parent, false)
                view.listRecyclerView.layoutManager = GridLayoutManager(context, 2)
                view.listRecyclerView.addItemDecoration(GridSpacingItemDecoration(2, context.resources.getDimensionPixelSize(R.dimen.marginNormal), false, context.resources.getDimensionPixelSize(R.dimen.marginClose)))
                return TagsViewHolder(view)
            }
            MediaItem.VIEW_TYPE_THEMES_OPENING, MediaItem.VIEW_TYPE_THEMES_ENDING -> {
                val view = LayoutTitleAndListBinding.inflate(inflater, parent, false)
                view.listRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                return ThemesViewHolder(view)
            }
            MediaItem.VIEW_TYPE_RELATIONS -> {
                val view = LayoutHorizontalListBinding.inflate(inflater, parent, false)
                relationsAdapter = MediaRelationsRvAdapter(context, listOf(), appSetting, width, listener.mediaRelationsListener)
                view.horizontalListRecyclerView.adapter = relationsAdapter
                view.horizontalListRecyclerView.addItemDecoration(SpaceItemDecoration(right = context.resources.getDimensionPixelSize(R.dimen.marginPageNormal)))
                return RelationsViewHolder(view)
            }
            MediaItem.VIEW_TYPE_RECOMMENDATIONS -> {
                val view = LayoutHorizontalListBinding.inflate(inflater, parent, false)
                val params = view.root.layoutParams as? ViewGroup.MarginLayoutParams
                params?.topMargin = context.resources.getDimensionPixelSize(R.dimen.marginBig)
                view.root.layoutParams = params
                recommendationsAdapter = MediaRecommendationsRvAdapter(context, listOf(), appSetting, width, listener.mediaRecommendationsListener)
                view.horizontalListRecyclerView.adapter = recommendationsAdapter
                view.horizontalListRecyclerView.addItemDecoration(SpaceItemDecoration(right = context.resources.getDimensionPixelSize(R.dimen.marginPageNormal)))
                return RecommendationsViewHolder(view)
            }
            MediaItem.VIEW_TYPE_LINKS -> {
                val view = LayoutTitleAndListBinding.inflate(inflater, parent, false)
                linksAdapter = MediaLinksRvAdapter(context, listOf(), listener.mediaLinksListener)
                view.listRecyclerView.adapter = linksAdapter
                view.listRecyclerView.layoutManager = FlexboxLayoutManager(context)
                return LinkViewHolder(view)
            }
            MediaItem.VIEW_TYPE_STATS -> {
                val view = LayoutMediaStatsBinding.inflate(inflater, parent, false)
                return StatsViewHolder(view)
            }
            MediaItem.VIEW_TYPE_STAFF_ITEM -> {
                val view = ListCardImageAndTextBinding.inflate(inflater, parent, false)
                return StaffItemViewHolder(view)
            }
            MediaItem.VIEW_TYPE_CHARACTER_ITEM -> {
                val view = ListCardImageAndTextBinding.inflate(inflater, parent, false)
                return CharacterItemViewHolder(view)
            }
            MediaItem.VIEW_TYPE_CHARACTER_LANGUAGE -> {
                val view = LayoutTitleAndListBinding.inflate(inflater, parent, false)
                return CharacterLanguageViewHolder(view)
            }
            MediaItem.VIEW_TYPE_EPISODE_ITEM -> {
                val view = ListEpisodeBinding.inflate(inflater, parent, false)
                return EpisodeItemViewHolder(view)
            }
            MediaItem.VIEW_TYPE_EPISODE_PAGINATION -> {
                val view = ListEpisodeMoreBinding.inflate(inflater, parent, false)
                return EpisodePaginationViewHolder(view)
            }
            MediaItem.VIEW_TYPE_GENRE -> {
                val view = LayoutTitleAndListBinding.inflate(inflater, parent, false)
                return GenreViewHolder(view)
            }
            else -> {
                val view = LayoutTitleAndTextBinding.inflate(inflater, parent, false)
                return SynopsisViewHolder(view)
            }
        }
    }

    private fun getTextListener(): TextRvAdapter.TextListener {
        return object : TextRvAdapter.TextListener {
            override fun getText(text: String) {
                val studio = list.find { it.viewType == MediaItem.VIEW_TYPE_INFO }?.media?.studios?.edges?.find { it.node.name == text }?.node
                studio?.let {
                    listener.mediaStudioListener.navigateToStudio(it)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    inner class CharacterLanguageViewHolder(private val binding: LayoutTitleAndListBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            binding.titleText.show(false)
            binding.seeMoreText.text = context.getString(R.string.language_x, selectedLanguage.name.convertFromSnakeCase(true))
            binding.seeMoreText.show(true)
            binding.seeMoreText.clicks {
                listener.mediaCharacterListener.openLanguageDialog()
            }
            binding.listRecyclerView.show(false)
            binding.footnoteText.show(false)
        }
    }

    inner class CharacterItemViewHolder(private val binding: ListCardImageAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            val edge = item.characterEdge ?: return
            binding.apply {
                ImageUtil.loadImage(context, edge.node.getImage(appSetting), cardImage)
                cardText.text = edge.node.name.userPreferred
                
                // Find voice actor for the selected language
                val voiceActorRole = edge.voiceActorRoles.find { 
                    it.voiceActor.language.replace(" ", "_").equals(selectedLanguage.name, ignoreCase = true)
                }
                val voiceActor = voiceActorRole?.voiceActor
                cardSubtitle.text = voiceActor?.name?.userPreferred ?: edge.role?.name?.convertFromSnakeCase(true) ?: ""
                cardSubtitle.show(true)

                root.clicks { listener.mediaCharacterListener.navigateToCharacter(edge.node) }
                
                voiceActor?.let { va ->
                    cardSubtitle.clicks { listener.mediaCharacterListener.navigateToStaff(va) }
                }
            }
        }
    }

    inner class StaffItemViewHolder(private val binding: ListCardImageAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            val edge = item.staffEdge ?: return
            binding.apply {
                ImageUtil.loadImage(context, edge.node.getImage(appSetting), cardImage)
                cardText.text = edge.node.name.userPreferred
                cardSubtitle.text = edge.role
                cardSubtitle.show(true)

                root.clicks { listener.mediaStaffListener.navigateToStaff(edge.node) }
            }
        }
    }

    inner class InfoViewHolder(private val binding: LayoutMediaInfoBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            binding.apply {
                val fallbackTitle = item.media.title.romaji
                mediaInfoRomajiText.text = item.media.title.romaji
                mediaInfoRomajiText.clicks {
                    listener.mediaInfoListener.copyTitle(item.media.title.romaji)
                }

                val englishTitle = item.media.title.english.ifBlank { fallbackTitle }
                mediaInfoEnglishText.text = englishTitle
                mediaInfoEnglishText.clicks {
                    listener.mediaInfoListener.copyTitle(englishTitle)
                }

                val nativeTitle = item.media.title.native.ifBlank { fallbackTitle }
                mediaInfoNativeText.text = nativeTitle
                mediaInfoNativeText.clicks {
                    listener.mediaInfoListener.copyTitle(nativeTitle)
                }

                synonymsAdapter?.updateData(item.media.synonyms.ifEmpty { listOf(fallbackTitle) })

                mediaInfoFormatText.text = item.media.getFormattedMediaFormat(true)

                mediaInfoLengthLabel.text = when (item.media.type?.getMediaType()) {
                    MediaType.ANIME -> context.getString(R.string.episodes)
                    MediaType.MANGA -> context.getString(R.string.chapters)
                    else -> context.getString(R.string.episodes)
                }
                mediaInfoLengthText.text = item.media.getLength()?.getNumberFormatting() ?: "?"

                mediaInfoDurationLabel.text = when (item.media.type?.getMediaType()) {
                    MediaType.ANIME -> context.getString(R.string.episode_duration)
                    MediaType.MANGA -> context.getString(R.string.volumes)
                    else -> context.getString(R.string.episode_duration)
                }
                mediaInfoDurationText.text = when (item.media.type?.getMediaType()) {
                    MediaType.ANIME -> item.media.duration?.showUnit(context, R.plurals.minute) ?: "?"
                    MediaType.MANGA -> item.media.volumes?.getNumberFormatting() ?: "?"
                    else -> item.media.duration?.showUnit(context, R.plurals.minute) ?: "?"
                }
                mediaInfoDurationLayout.show(mediaInfoDurationText.text != "?")

                mediaInfoSourceText.text = item.media.source?.getString()

                mediaInfoStatusText.text = item.media.status?.getString()

                val startDate = TimeUtil.getReadableDateFromFuzzyDate(item.media.startDate)
                mediaInfoStartDateText.text = if (startDate == "-") "?" else startDate

                val endDate = TimeUtil.getReadableDateFromFuzzyDate(item.media.endDate)
                mediaInfoEndDateText.text = if (endDate == "-") "?" else endDate

                mediaInfoSeasonLayout.show(item.media.type == com.doma.alsan.type.MediaType.ANIME && item.media.season != null && item.media.seasonYear != null)
                mediaInfoSeasonText.text = "${item.media.season?.getString()} ${item.media.seasonYear}"
                mediaInfoSeasonText.clicks {
                    if (item.media.season != null && item.media.seasonYear != null) {
                        listener.mediaInfoListener.navigateToExplore(item.media.type ?: com.doma.alsan.type.MediaType.ANIME, item.media.season, item.media.seasonYear)
                    }
                }
                
                // Remove redundant Info title if requested (hiding it)
                mediaInfoTitle.show(false)

                val studios = item.media.studios.edges.filter { it.isMain }.map { it.node.name }
                val producers = item.media.studios.edges.filter { !it.isMain }.map { it.node.name }
                val serializations = item.media.mangaSerialization?.map { it.name } ?: listOf()
                studiosAdapter?.updateData(studios)
                producersAdapter?.updateData(producers)
                serializationsAdapter?.updateData(serializations)
                mediaInfoStudiosLayout.show(studios.isNotEmpty())
                mediaInfoProducersLayout.show(producers.isNotEmpty())
                mediaInfoSerializationsLayout.show(serializations.isNotEmpty())
                mediaInfoDividerThree.root.show(studios.isNotEmpty() || producers.isNotEmpty() || serializations.isNotEmpty())

                mediaInfoStatsLayout.mediaStatsAverageScore.text = item.media.averageScore.getNumberFormatting() + "%"
                mediaInfoStatsLayout.mediaStatsMeanScore.text = item.media.meanScore.getNumberFormatting() + "%"
                mediaInfoStatsLayout.mediaStatsPopularity.text = item.media.popularity.getNumberFormatting()
                mediaInfoStatsLayout.mediaStatsFavorites.text = item.media.favourites.getNumberFormatting()
            }
        }
    }

    inner class TagsViewHolder(private val binding: LayoutTitleAndListBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            binding.titleText.text = context.getString(R.string.tags)
            binding.seeMoreText.show(item.media.tags.any { it.isGeneralSpoiler || it.isMediaSpoiler })
            binding.seeMoreText.text = if (item.showSpoilerTags) context.getString(R.string.hide_spoilers) else context.getString(R.string.show_spoilers)
            binding.seeMoreText.setTextColor(context.getAttrValue(R.attr.themeSecondaryColor))
            binding.seeMoreText.clicks {
                listener.mediaTagsListener.shouldShowSpoilers(!item.showSpoilerTags)
            }
            binding.footnoteText.text = context.getString(R.string.long_press_to_see_tag_description)
            binding.footnoteText.show(true)
            binding.listRecyclerView.adapter = MediaTagsRvAdapter(
                context,
                if (item.showSpoilerTags)
                    item.media.tags
                else
                    item.media.tags.filter { !!it.isGeneralSpoiler && !it.isMediaSpoiler },
                item.media.type ?: com.doma.alsan.type.MediaType.ANIME,
                listener.mediaTagsListener
            )
        }
    }

    inner class ThemesViewHolder(private val binding: LayoutTitleAndListBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            with(binding) {
                val themeGroups = when (item.viewType) {
                    MediaItem.VIEW_TYPE_THEMES_ENDING -> {
                        titleText.text = context.getString(R.string.ending_themes)
                        item.media.endings?.groupBy { it.group } ?: mapOf()
                    }
                    else -> {
                        titleText.text = context.getString(R.string.opening_themes)
                        item.media.openings?.groupBy { it.group } ?: mapOf()
                    }
                }

                val hasMultipleGroups = themeGroups.keys.size > 1
                seeMoreText.show(hasMultipleGroups)
                seeMoreText.text = item.themeGroup
                seeMoreText.setTextColor(context.getAttrValue(R.attr.themeSecondaryColor))
                seeMoreText.clicks {
                    listener.mediaThemesListener.openGroupDialog(item.viewType, themeGroups.keys.toList())
                }
                footnoteText.show(false)
                listRecyclerView.adapter = MediaThemesRvAdapter(context, themeGroups[item.themeGroup] ?: listOf(), object : MediaThemesRvAdapter.MediaThemesListener {
                    override fun openThemeDialog(animeTheme: AnimeTheme, animeThemeEntry: AnimeThemeEntry?) {
                        listener.mediaThemesListener.openThemeDialog(item.media, animeTheme, animeThemeEntry)
                    }
                })
            }
        }
    }


    inner class RelationsViewHolder(private val binding: LayoutHorizontalListBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            binding.horizontalListTitle.text = context.getString(R.string.relations)
            binding.horizontalListSeeMore.show(false)
            relationsAdapter?.updateData(item.media.relations.edges)
        }
    }

    inner class RecommendationsViewHolder(private val binding: LayoutHorizontalListBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            binding.horizontalListTitle.text = context.getString(R.string.recommendations)
            binding.horizontalListSeeMore.show(false)
            recommendationsAdapter?.updateData(item.media.recommendations.nodes)
        }
    }

    inner class LinkViewHolder(private val binding: LayoutTitleAndListBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            binding.titleText.text = context.getString(R.string.links)
            binding.seeMoreText.show(false)
            binding.footnoteText.text = context.getString(R.string.long_press_to_copy_link)
            binding.footnoteText.show(true)
            linksAdapter?.updateData(item.media.externalLinks)
        }
    }

    inner class EpisodeItemViewHolder(private val binding: ListEpisodeBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            val episode = item.episode ?: return
            binding.apply {
                episodeNumberText.text = episode.number.toString()
                episodeTitleText.text = episode.title
                episodeBadgeText.show(episode.filler)
                episodeBadgeText.text = context.getString(R.string.filler)
                
                if (item.isCurrent) {
                    root.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    episodeNumberText.setTextColor(context.getAttrValue(R.attr.themePrimaryColor))
                    episodeTitleText.setTextColor(context.getAttrValue(R.attr.themePrimaryColor))
                    androidx.core.widget.TextViewCompat.setTextAppearance(episodeTitleText, R.style.FontSmallBold)
                } else {
                    root.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    episodeNumberText.setTextColor(context.getAttrValue(R.attr.themeSecondaryColor))
                    episodeTitleText.setTextColor(context.getAttrValue(R.attr.themeContentColor))
                    androidx.core.widget.TextViewCompat.setTextAppearance(episodeTitleText, R.style.FontSmall)
                }
                
                root.clicks { listener.mediaEpisodesListener.onEpisodeClick(episode) }
            }
        }
    }

    inner class EpisodePaginationViewHolder(private val binding: ListEpisodeMoreBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            val pagination = item.pagination ?: return
            val malId = item.media.idMal ?: return
            binding.apply {
                episodeMoreText.text = "${pagination.currentPage} / ${pagination.lastPage}"
                
                episodeMoreText.clicks {
                    listener.mediaEpisodesListener.onPageSelectorClick(episodeMoreText, malId, pagination.currentPage, pagination.lastPage)
                }
                
                episodePrevButton.show(pagination.currentPage > 1)
                episodePrevButton.clicks { 
                    listener.mediaEpisodesListener.onPageClick(malId, pagination.currentPage - 1)
                }
                
                episodeNextButton.show(pagination.currentPage < pagination.lastPage)
                episodeNextButton.clicks {
                    listener.mediaEpisodesListener.onPageClick(malId, pagination.currentPage + 1)
                }
            }
        }
    }

    inner class SynopsisViewHolder(private val binding: LayoutTitleAndTextBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            binding.apply {
                itemTitle.show(false) 
                itemText.text = item.media.description
            }
        }
    }

    inner class GenreViewHolder(private val binding: LayoutTitleAndListBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            binding.apply {
                titleText.show(false)
                seeMoreText.show(false)
                listRecyclerView.layoutManager = com.google.android.flexbox.FlexboxLayoutManager(context).apply {
                    flexDirection = com.google.android.flexbox.FlexDirection.ROW
                    justifyContent = com.google.android.flexbox.JustifyContent.FLEX_START
                    flexWrap = com.google.android.flexbox.FlexWrap.WRAP
                }
                listRecyclerView.adapter = GenreRvAdapter(context, item.media.genres, object : GenreRvAdapter.GenreListener {
                    override fun getGenre(genre: Genre) {
                        listener.mediaGenreListener.navigateToExplore(item.media.type ?: com.doma.alsan.type.MediaType.ANIME, genre)
                    }
                })
            }
        }
    }

    inner class StatsViewHolder(private val binding: LayoutMediaStatsBinding) : ViewHolder(binding) {
        override fun bind(item: MediaItem, index: Int) {
            binding.apply {
                mediaStatsAverageScore.text = item.media.averageScore.getNumberFormatting()
                mediaStatsMeanScore.text = item.media.meanScore.getNumberFormatting()
                mediaStatsPopularity.text = item.media.popularity.getNumberFormatting()
                mediaStatsFavorites.text = item.media.favourites.getNumberFormatting()
            }
        }
    }
}
