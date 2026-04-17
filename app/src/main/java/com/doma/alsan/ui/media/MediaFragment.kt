package com.doma.alsan.ui.media

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.appbar.AppBarLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.response.AnimeTheme
import com.doma.alsan.data.response.AnimeThemeEntry
import com.doma.alsan.data.response.Genre
import com.doma.alsan.data.response.Episode
import com.doma.alsan.databinding.ListEpisodeBinding
import com.doma.alsan.databinding.ListEpisodeMoreBinding
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter
import com.doma.alsan.data.response.anilist.*
import com.doma.alsan.databinding.FragmentMediaBinding
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.helper.enums.SearchCategory
import com.doma.alsan.helper.extensions.*
import com.doma.alsan.helper.pojo.ListItem
import com.doma.alsan.helper.pojo.MediaItem
import com.doma.alsan.helper.utils.GridSpacingItemDecoration
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.helper.utils.SpaceItemDecoration
import com.doma.alsan.helper.utils.TimeUtil
import com.doma.alsan.type.MediaSeason
import com.doma.alsan.ui.base.BaseFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.recyclerview.widget.RecyclerView
import com.doma.alsan.ui.common.GenreRvAdapter
import com.doma.alsan.helper.utils.MarkdownUtil
import kotlin.math.abs

class MediaFragment : BaseFragment<FragmentMediaBinding, MediaViewModel>() {

    override val viewModel: MediaViewModel by viewModel()

    private var scaleUpAnimation: Animation? = null
    private var scaleDownAnimation: Animation? = null
    private var isToolbarExpanded = true

    private var mediaAdapter: MediaRvAdapter? = null
    private var episodesAdapter: MediaEpisodesRvAdapter? = null
    private var episodesRecyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var menuItemMediaStats: MenuItem? = null
    private var menuItemSocial: MenuItem? = null
    private var menuItemReview: MenuItem? = null
    private var menuItemDownloadCover: MenuItem? = null
    private var menuItemDownloadBanner: MenuItem? = null
    private var currentMedia: Media? = null
    private var appSetting = AppSetting()
    private var pendingScrollToEpisode: Int? = null
    private var isSynopsisExpanded = false

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMediaBinding {
        return FragmentMediaBinding.inflate(inflater, container, false)
    }

    private var isUpdatingTabs = false

    override fun setUpLayout() {
        binding.apply {
            scaleUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
            scaleDownAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down)
            mediaAppBarLayout.setExpanded(isToolbarExpanded)

            setUpToolbar(mediaToolbar, "", R.drawable.ic_custom_close) {
                navigation.closeBrowseScreen()
            }
            mediaToolbar.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_custom_more)

            menuItemMediaStats = mediaToolbar.menu.findItem(R.id.itemMediaStats)
            menuItemMediaStats?.setOnMenuItemClickListener {
                currentMedia?.let { navigation.navigateToMediaStats(it) }
                true
            }
            menuItemSocial = mediaToolbar.menu.findItem(R.id.itemMediaSocial)
            menuItemSocial?.setOnMenuItemClickListener {
                currentMedia?.let { navigation.navigateToMediaSocial(it) }
                true
            }
            menuItemReview = mediaToolbar.menu.findItem(R.id.itemMediaReview)
            menuItemReview?.setOnMenuItemClickListener {
                currentMedia?.let { navigation.navigateToMediaReview(it) }
                true
            }
            menuItemDownloadCover = mediaToolbar.menu.findItem(R.id.itemDownloadCover)
            menuItemDownloadCover?.setOnMenuItemClickListener {
                currentMedia?.let {
                    ImageUtil.downloadImage(requireContext(), it.coverImage.extraLarge, "${it.title.userPreferred}_cover.jpg")
                }
                true
            }

            menuItemDownloadBanner = mediaToolbar.menu.findItem(R.id.itemDownloadBanner)
            menuItemDownloadBanner?.setOnMenuItemClickListener {
                currentMedia?.let {
                    if (it.bannerImage.isNotBlank()) {
                        ImageUtil.downloadImage(requireContext(), it.bannerImage, "${it.title.userPreferred}_banner.jpg")
                    } else {
                        dialog.showToast(R.string.there_s_nothing_here)
                    }
                }
                true
            }

            val gridLayoutManager = GridLayoutManager(requireContext(), 3)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val viewType = mediaAdapter?.getItemViewType(position)
                    return when (viewType) {
                        MediaItem.VIEW_TYPE_CHARACTER_ITEM,
                        MediaItem.VIEW_TYPE_STAFF_ITEM -> 1
                        else -> 3
                    }
                }
            }
            mediaRecyclerView.layoutManager = gridLayoutManager
            
            mediaAdapter = MediaRvAdapter(requireContext(), listOf(), appSetting, screenWidth, getMediaListener())
            mediaRecyclerView.adapter = mediaAdapter
            
            val spacing = resources.getDimensionPixelSize(R.dimen.marginNormal)
            mediaRecyclerView.addItemDecoration(GridSpacingItemDecoration(3, spacing, true))
            assignAdapter(appSetting)

            mediaAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                isToolbarExpanded = verticalOffset == 0
                mediaSwipeRefresh.isEnabled = isToolbarExpanded
                // Banner content always stays visible - no scale animation
            })

            mediaCoverImage.clicks {
                viewModel.loadCoverImage()
            }

            mediaBannerImage.clicks {
                viewModel.loadBannerImage()
            }

            mediaAddToListButton.clicks {
                arguments?.getInt(MEDIA_ID)?.let { mediaId ->
                    navigation.navigateToEditor(mediaId, false) {
                        // do nothing
                    }
                }
            }

            mediaSwipeRefresh.setOnRefreshListener {
                viewModel.reloadData()
            }

            // TabLayout setup
            mediaTabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                    if (isUpdatingTabs) return
                    val mediaTab = tab.tag as? MediaViewModel.MediaTab ?: return
                    viewModel.setTab(mediaTab)
                }
                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                    if (isUpdatingTabs) return
                    val mediaTab = tab.tag as? MediaViewModel.MediaTab ?: return
                    viewModel.setTab(mediaTab)
                }
            })

            mediaHeaderSynopsisArrow.clicks {
                isSynopsisExpanded = !isSynopsisExpanded
                mediaHeaderSynopsisText.maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 4
                mediaHeaderSynopsisGradient.show(!isSynopsisExpanded)
                ImageUtil.loadImage(requireContext(), if (isSynopsisExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down, mediaHeaderSynopsisArrow)
            }
        }
    }

    override fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mediaCollapsingToolbar, null)
        binding.mediaRecyclerView.applyBottomPaddingInsets()
    }

    override fun setUpObserver() {
        disposables.addAll(
            viewModel.isAuthenticated.subscribe {
                binding.mediaAddToListButton.isEnabled = it

                if (!it) {
                    binding.mediaAddToListButton.apply {
                        text = getString(R.string.please_login)
                        strokeWidth = 0
                        strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                        backgroundTintList = ColorStateList.valueOf(context.getAttrValue(R.attr.themeContentTransparentColor))
                        setTextColor(context.getAttrValue(R.attr.themeContentColor))
                    }
                }
            },
            viewModel.loading.subscribe {
                binding.mediaSwipeRefresh.isRefreshing = it
            },
            viewModel.success.subscribe {
                dialog.showToast(it)
            },
            viewModel.error.subscribe {
                dialog.showToast(it)
            },
            viewModel.mediaAdapterComponent.subscribe {
                appSetting = it
                assignAdapter(it)
            },
            viewModel.bannerImage.subscribe {
                ImageUtil.loadImage(requireContext(), it, binding.mediaBannerImage)
            },
            viewModel.coverImage.subscribe {
                ImageUtil.loadImage(requireContext(), it, binding.mediaCoverImage)
            },
            viewModel.mediaTitle.subscribe {
                binding.mediaTitleText.text = it
                // Show media title in toolbar when scrolled
                binding.mediaCollapsingToolbar.title = it
            },
            viewModel.mediaYear.subscribe {
                binding.mediaYearText.text = it
            },
            viewModel.mediaYearVisibility.subscribe {
                binding.mediaYearText.show(it)
            },
            viewModel.mediaFormat.subscribe {
                binding.mediaFormatText.text = it.data?.getString()
            },
            viewModel.mediaLength.subscribe { (length, mediaType) ->
                binding.mediaLengthText.text = when (mediaType) {
                    MediaType.ANIME -> length.showUnit(requireContext(), R.plurals.episode)
                    MediaType.MANGA -> length.showUnit(requireContext(), R.plurals.chapter)
                }
            },
            viewModel.mediaLengthVisibility.subscribe {
                binding.mediaLengthDividerIcon.show(it)
                binding.mediaLengthText.show(it)
            },
            viewModel.airingSchedule.subscribe {
                binding.mediaAiringLayout.show(it.data != null)

                it.data?.let { airingSchedule ->
                    binding.mediaAiringText.text = getString(R.string.ep_x_on_y, airingSchedule.episode, TimeUtil.displayInDayDateTimeFormat(airingSchedule.airingAt))
                }
            },
            viewModel.averageScore.subscribe {
                binding.mediaScoreText.text = it.getNumberFormatting()
            },
            viewModel.favorites.subscribe {
                binding.mediaFavoritesText.text = it.getNumberFormatting()
            },
            viewModel.addToListButtonText.subscribe {
                if (it.isNotBlank()) {
                    binding.mediaAddToListButton.apply {
                        text = it
                        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.lineWidth)
                        strokeColor = ColorStateList.valueOf(context.getAttrValue(R.attr.themePrimaryColor))
                        backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                        setTextColor(context.getAttrValue(R.attr.themePrimaryColor))
                    }
                } else {
                    binding.mediaAddToListButton.apply {
                        text = getString(R.string.add_to_list)
                        strokeWidth = 0
                        strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                        backgroundTintList = ColorStateList.valueOf(context.getAttrValue(R.attr.themePrimaryColor))
                        setTextColor(context.getAttrValue(R.attr.themeBackgroundColor))
                    }
                }
            },
            viewModel.mediaItemList.subscribe { list ->
                mediaAdapter?.updateData(list, true)
            },
            viewModel.mediaMetadata.subscribe { media ->
                currentMedia = media
                updateHeaderContent(media)
            },
            viewModel.coverImageUrlForPreview.subscribe {
                ImageUtil.showFullScreenImage(requireContext(), it, binding.mediaCoverImage)
            },
            viewModel.bannerImageUrlForPreview.subscribe {
                ImageUtil.showFullScreenImage(requireContext(), it, binding.mediaBannerImage)
            },
            viewModel.voiceActorLanguages.subscribe { languages ->
                dialog.showListDialog(languages) { data, _ ->
                    viewModel.updateVoiceActorLanguage(data)
                }
            },
            viewModel.selectedLanguage.subscribe { language ->
                mediaAdapter?.updateSelectedLanguage(language)
            },
            viewModel.pagedEpisodes.observeOn(AndroidSchedulers.mainThread()).subscribe { result: Triple<List<Episode>, Int, Int> ->
                val episodes: List<Episode> = result.first
                val currentPage: Int = result.second
                val totalPages: Int = result.third
                episodesAdapter?.updateEpisodes(episodes, currentPage, totalPages)
                val epNum: Int? = pendingScrollToEpisode
                if (epNum != null) {
                    binding.root.postDelayed({
                        episodesAdapter?.scrollToEpisode(epNum)
                        pendingScrollToEpisode = null
                    }, 300L)
                }
            },
            viewModel.currentProgress.observeOn(AndroidSchedulers.mainThread()).subscribe { progress ->
                episodesAdapter?.setCurrentProgress(progress)
            },
            viewModel.currentTab.subscribe { tab ->
                val tabLayout = binding.mediaTabLayout
                for (i in 0 until tabLayout.tabCount) {
                    val t = tabLayout.getTabAt(i)
                    if (t?.tag == tab) {
                        t.select()
                        break
                    }
                }
            }
        )

        arguments?.getInt(MEDIA_ID)?.let {
            viewModel.loadData(MediaParam(it))
        }
    }

    private fun assignAdapter(appSetting: AppSetting) {
        mediaAdapter = MediaRvAdapter(requireContext(), listOf(), appSetting, screenWidth, getMediaListener())
        binding.mediaRecyclerView.adapter = mediaAdapter
    }

    private fun getMediaListener(): MediaListener {
        return object : MediaListener {
            override val mediaInfoListener: MediaListener.MediaInfoListener = getMediaInfoListener()
            override val mediaGenreListener: MediaListener.MediaGenreListener = getMediaGenreListener()
            override val mediaCharacterListener: MediaListener.MediaCharacterListener = getMediaCharacterListener()
            override val mediaStudioListener: MediaListener.MediaStudioListener = getMediaStudioListener()
            override val mediaTagsListener: MediaListener.MediaTagsListener = getMediaTagsListener()
            override val mediaThemesListener: MediaListener.MediaThemesListener = getMediaThemesListener()
            override val mediaStaffListener: MediaListener.MediaStaffListener = getMediaStaffListener()
            override val mediaRelationsListener: MediaListener.MediaRelationsListener = getMediaRelationsListener()
            override val mediaRecommendationsListener: MediaListener.MediaRecommendationsListener = getMediaRecommendationsListener()
            override val mediaLinksListener: MediaListener.MediaLinksListener = getMediaLinksListener()
            override val mediaEpisodesListener: MediaListener.MediaEpisodesListener = getMediaEpisodesListener()
        }
    }


    private fun getMediaInfoListener(): MediaListener.MediaInfoListener {
        return object : MediaListener.MediaInfoListener {
            override fun copyTitle(title: String) {
                viewModel.copyText(title)
            }

            override fun navigateToExplore(type: com.doma.alsan.type.MediaType, season: MediaSeason, seasonYear: Int) {
                navigation.navigateToExplore(
                    SearchCategory.ANIME,
                    MediaFilter(mediaSeasons = listOf(season), minYear = seasonYear, maxYear = seasonYear)
                ) {
                    it()
                }
            }
        }
    }

    private fun getMediaGenreListener(): MediaListener.MediaGenreListener {
        return object : MediaListener.MediaGenreListener {
            override fun navigateToExplore(type: com.doma.alsan.type.MediaType, genre: Genre) {
                navigation.navigateToExplore(
                    if (type == com.doma.alsan.type.MediaType.MANGA) SearchCategory.MANGA else SearchCategory.ANIME,
                    MediaFilter(includedGenres = listOf(genre.name))
                ) {
                    it()
                }
            }
        }
    }

    private fun getMediaCharacterListener(): MediaListener.MediaCharacterListener {
        return object : MediaListener.MediaCharacterListener {
            fun navigateToMediaCharacters(media: Media) {
                // No longer needed, characters are in a tab
            }

            override fun navigateToCharacter(character: Character) {
                navigation.navigateToCharacter(character.id)
            }

            override fun navigateToStaff(staff: Staff) {
                navigation.navigateToStaff(staff.id)
            }

            override fun openLanguageDialog() {
                viewModel.loadVoiceActorLanguages()
            }
        }
    }

    private fun getMediaStudioListener(): MediaListener.MediaStudioListener {
        return object : MediaListener.MediaStudioListener {
            override fun navigateToStudio(studio: Studio) {
                navigation.navigateToStudio(studio.id)
            }
        }
    }

    private fun getMediaTagsListener(): MediaListener.MediaTagsListener {
        return object : MediaListener.MediaTagsListener {
            override fun shouldShowSpoilers(show: Boolean) {
                viewModel.updateShouldShowSpoilerTags(show)
            }

            override fun navigateToExplore(type: com.doma.alsan.type.MediaType, tag: MediaTag) {
                navigation.navigateToExplore(
                    if (type == com.doma.alsan.type.MediaType.MANGA) SearchCategory.MANGA else SearchCategory.ANIME,
                    MediaFilter(includedTags = listOf(tag))
                ) {
                    it()
                }
            }

            override fun showDescription(tag: MediaTag) {
                dialog.showToast(tag.description)
            }
        }
    }

    private fun getMediaThemesListener(): MediaListener.MediaThemesListener {
        return object : MediaListener.MediaThemesListener {
            override fun openThemeDialog(
                media: Media,
                animeTheme: AnimeTheme,
                animeThemeEntry: AnimeThemeEntry?
            ) {
                dialog.showAnimeThemesDialog(media, animeTheme, animeThemeEntry) { url, videoId, usePlayer ->
                    if (usePlayer && url != null)
                        navigation.openWebView(url)
                    else if (!usePlayer && videoId != null)
                        navigation.openOnYouTube(videoId)
                    else if (!usePlayer && url != null)
                        navigation.openOnSpotify(url)
                }
            }

            override fun openGroupDialog(viewType: Int, groups: List<String>) {
                dialog.showListDialog(groups.map { ListItem(it, it) }) { data, index ->
                    viewModel.changeThemeGroup(viewType, data)
                }
            }
        }
    }

    private fun getMediaStaffListener(): MediaListener.MediaStaffListener {
        return object : MediaListener.MediaStaffListener {
            fun navigateToMediaStaff(media: Media) {
                // No longer needed, staff is in a tab
            }

            override fun navigateToStaff(staff: Staff) {
                navigation.navigateToStaff(staff.id)
            }
        }
    }

    private fun getMediaRelationsListener() : MediaListener.MediaRelationsListener {
        return object : MediaListener.MediaRelationsListener {
            override fun navigateToMedia(media: Media) {
                navigation.navigateToMedia(media.getId())
            }
        }
    }

    private fun getMediaRecommendationsListener(): MediaListener.MediaRecommendationsListener {
        return object : MediaListener.MediaRecommendationsListener {
            override fun navigateToMedia(media: Media) {
                navigation.navigateToMedia(media.getId())
            }
        }
    }

    private fun getMediaLinksListener(): MediaListener.MediaLinksListener {
        return object : MediaListener.MediaLinksListener {
            override fun navigateToUrl(mediaExternalLink: MediaExternalLink) {
                navigation.openWebView(mediaExternalLink.url)
            }

            override fun copyExternalLink(mediaExternalLink: MediaExternalLink) {
                viewModel.copyExternalLink(mediaExternalLink)
            }
        }
    }

    private fun getMediaEpisodesListener(): MediaListener.MediaEpisodesListener {
        return object : MediaListener.MediaEpisodesListener {
            override fun onEpisodeClick(episode: Episode) {
                this@MediaFragment.onEpisodeClick(episode)
            }

            override fun onEpisodeLongClick(episode: Episode) {
                this@MediaFragment.onEpisodeLongClick(episode)
            }

            fun showAllEpisodes() {
                // No longer needed, episodes are in a tab
            }

            override fun onPageClick(malId: Int, page: Int) {
                viewModel.fetchEpisodes(malId, page)
            }

            override fun onPageSelectorClick(view: android.view.View, malId: Int, currentPage: Int, totalPages: Int) {
                val popup = androidx.appcompat.widget.PopupMenu(requireContext(), view)
                for (i in 1..totalPages) {
                    val item = popup.menu.add(0, i, i, "Pagina $i")
                    if (i == currentPage) item.isEnabled = false
                }
                popup.setOnMenuItemClickListener { item ->
                    viewModel.fetchEpisodes(malId, item.itemId)
                    true
                }
                popup.show()
            }
        }
    }

    private fun updateHeaderContent(media: Media) {
        binding.apply {
            // Genres
            if (mediaHeaderGenreRecyclerView.layoutManager == null) {
                mediaHeaderGenreRecyclerView.layoutManager = com.google.android.flexbox.FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = com.google.android.flexbox.FlexDirection.ROW
                    justifyContent = com.google.android.flexbox.JustifyContent.FLEX_START
                    flexWrap = com.google.android.flexbox.FlexWrap.WRAP
                }
            }
            mediaHeaderGenreRecyclerView.adapter = GenreRvAdapter(requireContext(), media.genres, object : GenreRvAdapter.GenreListener {
                override fun getGenre(genre: Genre) {
                    getMediaGenreListener().navigateToExplore(media.type ?: com.doma.alsan.type.MediaType.ANIME, genre)
                }
            })

            // Synopsis
            MarkdownUtil.applyMarkdown(requireContext(), screenWidth, mediaHeaderSynopsisText, media.description)
            mediaHeaderSynopsisLayout.show(media.description.isNotBlank())

            // Tabs
            val tabs = ArrayList<MediaViewModel.MediaTab>()
            tabs.add(MediaViewModel.MediaTab.DETAILS)
            tabs.add(MediaViewModel.MediaTab.CHARACTERS)
            if (media.type == com.doma.alsan.type.MediaType.ANIME) tabs.add(MediaViewModel.MediaTab.EPISODES)
            tabs.add(MediaViewModel.MediaTab.STAFF)
            tabs.add(MediaViewModel.MediaTab.RECOMMENDATIONS)

            if (mediaTabLayout.tabCount != tabs.size) {
                isUpdatingTabs = true
                mediaTabLayout.removeAllTabs()
                tabs.forEach { tabType ->
                    val tab = mediaTabLayout.newTab()
                    tab.text = getString(tabType.stringRes)
                    tab.tag = tabType
                    mediaTabLayout.addTab(tab)
                }
                isUpdatingTabs = false
            }
        }
    }

    private fun onEpisodeClick(episode: Episode) {
        if (!episode.url.isNullOrBlank()) {
            navigation.openWebView(episode.url)
        }
    }

    private fun onEpisodeLongClick(episode: Episode) {
        if (!episode.title.isNullOrBlank()) {
            viewModel.copyText(episode.title, R.string.episode_title_copied)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaAdapter = null
        episodesAdapter = null
        menuItemMediaStats = null
        menuItemSocial = null
        menuItemReview = null
        menuItemDownloadCover = null
        menuItemDownloadBanner = null
    }

    companion object {
        private const val MEDIA_ID = "mediaId"

        @JvmStatic
        fun newInstance(mediaId: Int) =
            MediaFragment().apply {
                arguments = Bundle().apply {
                    putInt(MEDIA_ID, mediaId)
                }
            }
    }
}