package com.doma.alsan.ui.character

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.appbar.AppBarLayout
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.databinding.FragmentCharacterBinding
import com.doma.alsan.helper.extensions.*
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.helper.utils.SpaceItemDecoration
import com.doma.alsan.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs
import com.doma.alsan.helper.pojo.CharacterItem
import com.doma.alsan.ui.character.CharacterViewModel.CharacterTab

class CharacterFragment : BaseFragment<FragmentCharacterBinding, CharacterViewModel>() {

    override val viewModel: CharacterViewModel by viewModel()

    private var scaleUpAnimation: Animation? = null
    private var scaleDownAnimation: Animation? = null
    private var isToolbarExpanded = true

    private var characterAdapter: CharacterRvAdapter? = null

    private var menuViewOnAniList: MenuItem? = null
    private var menuCopyLink: MenuItem? = null
    private var appSetting = AppSetting()
    private var isUpdatingTabs = false
    private var isSynopsisExpanded = false

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCharacterBinding {
        return FragmentCharacterBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        binding.apply {
            scaleUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
            scaleDownAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down)
            characterAppBarLayout.setExpanded(isToolbarExpanded)

            setUpToolbar(characterToolbar, "", R.drawable.ic_delete) {
                navigation.closeBrowseScreen()
            }
            characterToolbar.inflateMenu(R.menu.menu_view_on_anilist)

            menuViewOnAniList = characterToolbar.menu.findItem(R.id.itemViewOnAniList)
            menuCopyLink = characterToolbar.menu.findItem(R.id.itemCopyLink)

            menuViewOnAniList?.setOnMenuItemClickListener {
                viewModel.loadCharacterLink()
                true
            }

            menuCopyLink?.setOnMenuItemClickListener {
                viewModel.copyCharacterLink()
                true
            }

            characterRecyclerView.addItemDecoration(SpaceItemDecoration(top = resources.getDimensionPixelSize(R.dimen.marginFar)))
            assignAdapter(appSetting)

            characterAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                isToolbarExpanded = verticalOffset == 0
                characterSwipeRefresh.isEnabled = isToolbarExpanded
                // Banner content always stays visible - no scale animation
            })

            characterImage.clicks {
                viewModel.loadCharacterImage()
            }

            characterSetAsFavoriteButton.clicks {
                viewModel.toggleFavorite()
            }

            characterSwipeRefresh.setOnRefreshListener {
                viewModel.reloadData()
            }

            characterTabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                    if (isUpdatingTabs) return
                    val tabType = tab.tag as? CharacterViewModel.CharacterTab ?: return
                    viewModel.setTab(tabType)
                    updateRecyclerViewLayout(tabType)
                }
                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            })

            characterHeaderSynopsisLayout.clicks {
                isSynopsisExpanded = !isSynopsisExpanded
                updateSynopsisState()
            }
        }
    }

    private fun updateSynopsisState() {
        binding.apply {
            characterHeaderSynopsisText.maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 4
            characterHeaderSynopsisArrow.rotation = if (isSynopsisExpanded) 180f else 0f
        }
    }

    private fun updateRecyclerViewLayout(tab: CharacterViewModel.CharacterTab) {
        binding.characterRecyclerView.apply {
            for (i in 0 until itemDecorationCount) {
                removeItemDecorationAt(0)
            }

            val spanCount = 3
            val spacing = resources.getDimensionPixelSize(R.dimen.marginSmall)
            val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), spanCount)
            gridLayoutManager.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (characterAdapter?.getItemViewType(position)) {
                        CharacterItem.VIEW_TYPE_MEDIA_GROUP -> spanCount
                        else -> 1
                    }
                }
            }
            layoutManager = gridLayoutManager
            addItemDecoration(com.doma.alsan.helper.utils.GridSpacingItemDecoration(spanCount, spacing, true))
        }
    }

    override fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.characterCollapsingToolbar, null)
        binding.characterRecyclerView.applyBottomPaddingInsets()
    }

    override fun setUpObserver() {
        disposables.addAll(
            viewModel.loading.subscribe {
                binding.characterSwipeRefresh.isRefreshing = it
            },
            viewModel.isAuthenticated.subscribe {
                binding.characterSetAsFavoriteButton.isEnabled = it

                if (!it) {
                    binding.characterSetAsFavoriteButton.apply {
                        text = getString(R.string.please_login)
                        strokeWidth = 0
                        strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                        backgroundTintList = ColorStateList.valueOf(context.getAttrValue(R.attr.themeContentTransparentColor))
                        setTextColor(context.getAttrValue(R.attr.themeContentColor))
                    }
                }
            },
            viewModel.success.subscribe {
                dialog.showToast(it)
            },
            viewModel.error.subscribe {
                dialog.showToast(it)
            },
            viewModel.characterAdapterComponent.subscribe {
                appSetting = it
                assignAdapter(it)
            },
            viewModel.characterImage.subscribe {
                ImageUtil.loadImage(requireContext(), it, binding.characterImage)
            },
            viewModel.characterName.subscribe {
                binding.characterNameText.text = it
                // Show character name in toolbar when scrolled
                binding.characterCollapsingToolbar.title = it
            },
            viewModel.characterNativeName.subscribe {
                binding.characterNativeNameText.text = it
            },
            viewModel.mediaCount.subscribe {
                binding.characterMediaText.text = it.getNumberFormatting()
            },
            viewModel.mediaCountVisibility.subscribe {
                binding.characterMediaLayout.show(it)
                binding.characterBarDivider1.show(it)
            },
            viewModel.favoritesCount.subscribe {
                binding.characterFavoritesText.text = it.getNumberFormatting()
            },
            viewModel.isFavorite.subscribe {
                if (it) {
                    binding.characterSetAsFavoriteButton.apply {
                        text = context.getString(R.string.your_favorite)
                        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.lineWidth)
                        strokeColor = ColorStateList.valueOf(context.getAttrValue(R.attr.themePrimaryColor))
                        backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                        setTextColor(context.getAttrValue(R.attr.themePrimaryColor))
                    }
                } else {
                    binding.characterSetAsFavoriteButton.apply {
                        text = context.getString(R.string.set_as_favorite)
                        strokeWidth = 0
                        strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                        backgroundTintList = ColorStateList.valueOf(context.getAttrValue(R.attr.themePrimaryColor))
                        setTextColor(context.getAttrValue(R.attr.themeBackgroundColor))
                    }
                }
            },
            viewModel.characterItemList.subscribe {
                characterAdapter?.updateData(it, true)
                updateRecyclerViewLayout(viewModel.currentTab.blockingFirst())
            },
            viewModel.staffMedia.subscribe {
                dialog.showListDialog(it) { data, _ ->
                    navigation.navigateToMedia(data.getId())
                }
            },
            viewModel.characterLink.subscribe {
                navigation.openWebView(it)
            },
            viewModel.characterImageForPreview.subscribe {
                ImageUtil.showFullScreenImage(requireContext(), it, binding.characterImage)
            },
            viewModel.characterMetadata.subscribe { character ->
                updateHeaderContent(character)
            }
        )

        arguments?.getInt(CHARACTER_ID)?.let {
            viewModel.loadData(CharacterParam(it))
        }
    }

    private fun updateHeaderContent(character: com.doma.alsan.data.response.anilist.Character) {
        binding.apply {
            // Synopsis
            com.doma.alsan.helper.utils.MarkdownUtil.applyMarkdown(requireContext(), screenWidth, characterHeaderSynopsisText, character.description)
             characterBloodTypeText.text = character.bloodType.ifBlank { "-" }
             characterBloodTypeLabel.text = "Blood Type"
             characterBloodTypeLayout.show(character.bloodType.isNotBlank())
             
             characterHeaderSynopsisLayout.show(character.description.isNotBlank())

            // Tabs
            val tabs = ArrayList<CharacterTab>()
            tabs.add(CharacterTab.APPEARANCES)
            
            // Check if there are voice actors to show the tab
            val hasVoiceActors = character.media.edges.any { it.voiceActorRoles.isNotEmpty() }
            if (hasVoiceActors) tabs.add(CharacterTab.VOICE_ACTORS)

            if (characterTabLayout.tabCount != tabs.size) {
                isUpdatingTabs = true
                characterTabLayout.removeAllTabs()
                tabs.forEach { tabType ->
                    val tab = characterTabLayout.newTab()
                    tab.text = getString(tabType.stringRes)
                    tab.tag = tabType
                    characterTabLayout.addTab(tab)
                }
                isUpdatingTabs = false
            }
        }
    }

    private fun assignAdapter(appSetting: AppSetting) {
        characterAdapter = CharacterRvAdapter(requireContext(), listOf(), appSetting, screenWidth, getCharacterListener())
        binding.characterRecyclerView.adapter = characterAdapter
    }

    private fun getCharacterListener(): CharacterListener {
        return object : CharacterListener {
            override fun toggleShowMore(shouldShowMore: Boolean) {
                // Now handled in header
            }

            override fun navigateToStaff(staff: Staff) {
                navigation.navigateToStaff(staff.id)
            }

            override fun showStaffMedia(staff: Staff) {
                viewModel.loadStaffMedia(staff)
            }

            override fun navigateToCharacterMedia() {
                arguments?.let {
                    navigation.navigateToCharacterMedia(it.getInt(CHARACTER_ID))
                }
            }

            override val characterMediaListener: CharacterListener.CharacterMediaListener = object : CharacterListener.CharacterMediaListener {
                override fun navigateToMedia(media: Media) {
                    navigation.navigateToMedia(media.getId())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        characterAdapter = null
        menuCopyLink = null
        menuViewOnAniList = null
    }

    companion object {
        private const val CHARACTER_ID = "characterId"

        @JvmStatic
        fun newInstance(characterId: Int) =
            CharacterFragment().apply {
                arguments = Bundle().apply {
                    putInt(CHARACTER_ID, characterId)
                }
            }
    }
}