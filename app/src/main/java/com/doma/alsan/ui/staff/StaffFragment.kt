package com.doma.alsan.ui.staff

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.appbar.AppBarLayout
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.databinding.FragmentStaffBinding
import com.doma.alsan.helper.extensions.*
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.helper.utils.SpaceItemDecoration
import com.doma.alsan.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

class StaffFragment : BaseFragment<FragmentStaffBinding, StaffViewModel>() {

    override val viewModel: StaffViewModel by viewModel()

    private var scaleUpAnimation: Animation? = null
    private var scaleDownAnimation: Animation? = null
    private var isToolbarExpanded = true

    private var staffAdapter: StaffRvAdapter? = null

    private var menuViewOnAniList: MenuItem? = null
    private var menuCopyLink: MenuItem? = null
    private var appSetting = AppSetting()

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStaffBinding {
        return FragmentStaffBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        binding.apply {
            scaleUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
            scaleDownAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down)
            staffAppBarLayout.setExpanded(isToolbarExpanded)

            setUpToolbar(staffToolbar, "", R.drawable.ic_delete) {
                navigation.closeBrowseScreen()
            }
            staffToolbar.inflateMenu(R.menu.menu_view_on_anilist)

            menuViewOnAniList = staffToolbar.menu.findItem(R.id.itemViewOnAniList)
            menuCopyLink = staffToolbar.menu.findItem(R.id.itemCopyLink)

            menuViewOnAniList?.setOnMenuItemClickListener {
                viewModel.loadStaffLink()
                true
            }

            menuCopyLink?.setOnMenuItemClickListener {
                viewModel.copyStaffLink()
                true
            }

            staffRecyclerView.addItemDecoration(SpaceItemDecoration(top = resources.getDimensionPixelSize(R.dimen.marginFar)))
            assignAdapter(appSetting)

            staffAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                isToolbarExpanded = verticalOffset == 0
                staffSwipeRefresh.isEnabled = isToolbarExpanded
                // Banner content always stays visible - no scale animation
            })

            staffImage.clicks {
                viewModel.loadStaffImage()
            }

            staffSetAsFavoriteButton.clicks {
                viewModel.toggleFavorite()
            }

            staffSwipeRefresh.setOnRefreshListener {
                viewModel.reloadData()
            }
        }
    }

    override fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.staffCollapsingToolbar, null)
        binding.staffRecyclerView.applyBottomPaddingInsets()
    }

    override fun setUpObserver() {
        disposables.addAll(
            viewModel.loading.subscribe {
                binding.staffSwipeRefresh.isRefreshing = it
            },
            viewModel.isAuthenticated.subscribe {
                binding.staffSetAsFavoriteButton.isEnabled = it

                if (!it) {
                    binding.staffSetAsFavoriteButton.apply {
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
            viewModel.staffAdapterComponent.subscribe {
                appSetting = it
                assignAdapter(it)
            },
            viewModel.staffImage.subscribe {
                ImageUtil.loadImage(requireContext(), it, binding.staffImage)
            },
            viewModel.staffName.subscribe {
                binding.staffNameText.text = it
                // Show staff name in toolbar when scrolled
                binding.staffCollapsingToolbar.title = it
            },
            viewModel.mediaOrCharacterCount.subscribe {
                binding.staffMediaText.text = it.getNumberFormatting()
            },
            viewModel.mediaOrCharacterText.subscribe {
                binding.staffMediaLabel.text = getString(it)
            },
            viewModel.mediaOrCharacterCountVisibility.subscribe {
                binding.staffMediaLayout.show(it)
                binding.staffBarDivider1.show(it)
            },
            viewModel.favoritesCount.subscribe {
                binding.staffFavoritesText.text = it.getNumberFormatting()
            },
            viewModel.isFavorite.subscribe {
                if (it) {
                    binding.staffSetAsFavoriteButton.apply {
                        text = context.getString(R.string.your_favorite)
                        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.lineWidth)
                        strokeColor = ColorStateList.valueOf(context.getAttrValue(R.attr.themePrimaryColor))
                        backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                        setTextColor(context.getAttrValue(R.attr.themePrimaryColor))
                    }
                } else {
                    binding.staffSetAsFavoriteButton.apply {
                        text = context.getString(R.string.set_as_favorite)
                        strokeWidth = 0
                        strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                        backgroundTintList = ColorStateList.valueOf(context.getAttrValue(R.attr.themePrimaryColor))
                        setTextColor(context.getAttrValue(R.attr.themeBackgroundColor))
                    }
                }
            },
            viewModel.staffItemList.subscribe {
                staffAdapter?.updateData(it, true)
            },
            viewModel.staffLink.subscribe {
                navigation.openWebView(it)
            },
            viewModel.staffImageForPreview.subscribe {
                ImageUtil.showFullScreenImage(requireContext(), it, binding.staffImage)
            }
        )

        arguments?.getInt(STAFF_ID)?.let {
            viewModel.loadData(StaffParam(it))
        }
    }

    private fun assignAdapter(appSetting: AppSetting) {
        staffAdapter = StaffRvAdapter(requireContext(), listOf(), appSetting, screenWidth, getStaffListener())
        binding.staffRecyclerView.adapter = staffAdapter
    }

    private fun getStaffListener(): StaffListener {
        return object : StaffListener {
            override fun toggleShowMore(shouldShowMore: Boolean) {
                viewModel.updateShouldShowFullDescription(shouldShowMore)
            }

            override fun navigateToStaffCharacter() {
                arguments?.getInt(STAFF_ID)?.let {
                    navigation.navigateToStaffCharacter(it)
                }
            }

            override fun navigateToStaffMedia() {
                arguments?.getInt(STAFF_ID)?.let {
                    navigation.navigateToStaffMedia(it)
                }
            }

            override val staffCharacterListener: StaffListener.StaffCharacterListener = object : StaffListener.StaffCharacterListener {
                override fun navigateToCharacter(character: Character) {
                    navigation.navigateToCharacter(character.id)
                }
            }

            override val staffMediaListener: StaffListener.StaffMediaListener = object : StaffListener.StaffMediaListener {
                override fun navigateToMedia(media: Media) {
                    navigation.navigateToMedia(media.getId())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        staffAdapter = null
        menuCopyLink = null
        menuViewOnAniList = null
    }

    companion object {
        private const val STAFF_ID = "staffId"

        @JvmStatic
        fun newInstance(staffId: Int) =
            StaffFragment().apply {
                arguments = Bundle().apply {
                    putInt(STAFF_ID, staffId)
                }
            }
    }
}