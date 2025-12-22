package com.doma.alsan.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import coil.load
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.data.response.anilist.Studio
import com.doma.alsan.databinding.FragmentProfileBinding
import com.doma.alsan.helper.enums.ActivityListPage
import com.doma.alsan.helper.enums.Favorite
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.helper.extensions.applyBottomPaddingInsets
import com.doma.alsan.helper.extensions.applyTopPaddingInsets
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.utils.SpaceItemDecoration
import com.doma.alsan.ui.base.BaseFragment
import com.doma.alsan.ui.main.SharedMainViewModel
import com.stfalcon.imageviewer.StfalconImageViewer
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>() {

    override val viewModel: ProfileViewModel by viewModel()
    private val sharedViewModel by activityViewModel<SharedMainViewModel>()

    private var menuItemReviews: MenuItem? = null
    private var menuItemActivities: MenuItem? = null
    private var menuItemAddAsBestFriend: MenuItem? = null
    private var menuItemSettings: MenuItem? = null
    private var menuItemViewOnAniList: MenuItem? = null
    private var menuItemShareProfile: MenuItem? = null
    private var menuItemCopyLink: MenuItem? = null
    private var menuItemReport: MenuItem? = null

    private var profileAdapter: ProfileRvAdapter? = null
    private var currentUserId = 0
    private var appSetting = AppSetting()

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        binding.apply {
            if (!isViewer()) {
                setUpToolbar(profileToolbar, "", R.drawable.ic_custom_close) {
                    navigation.closeBrowseScreen()
                }
            }

            profileToolbar.menu.apply {
                menuItemReviews = findItem(R.id.itemReviews)
                menuItemActivities = findItem(R.id.itemActivities)
                menuItemAddAsBestFriend = findItem(R.id.itemAddAsBestFriend)
                menuItemSettings = findItem(R.id.itemSettings)
                menuItemViewOnAniList = findItem(R.id.itemViewOnAniList)
                menuItemShareProfile = findItem(R.id.itemShareProfile)
                menuItemCopyLink = findItem(R.id.itemCopyLink)
                menuItemReport = findItem(R.id.itemReport)
            }

            profileToolbar.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_custom_more)

            // Disable ViewPager2 swipe while scrolling to prevent scroll/swipe conflicts
            profileRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (isViewer()) {
                        when (newState) {
                            androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING -> {
                                sharedViewModel.setViewPagerSwipeEnabled(false)
                            }
                            androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE -> {
                                sharedViewModel.setViewPagerSwipeEnabled(true)
                            }
                        }
                    }
                }
            })
            
            assignAdapter(appSetting)

            notLoggedInLayout.goToLoginButton.setOnClickListener {
                viewModel.logout()
                navigation.navigateToLanding()
            }

            menuItemReviews?.setOnMenuItemClickListener {
                doIfUserIdIsLoaded {
                    navigation.navigateToUserReview(currentUserId)
                }
                true
            }

            menuItemActivities?.setOnMenuItemClickListener {
                doIfUserIdIsLoaded {
                    navigation.navigateToActivityList(ActivityListPage.SPECIFIC_USER, currentUserId)
                }
                true
            }

            menuItemSettings?.setOnMenuItemClickListener {
                navigation.navigateToSettings()
                true
            }

            menuItemViewOnAniList?.setOnMenuItemClickListener {
                viewModel.loadProfileUrlForWebView()
                true
            }

            menuItemShareProfile?.setOnMenuItemClickListener {
                viewModel.loadProfileUrlForShareSheet()
                true
            }

            menuItemCopyLink?.setOnMenuItemClickListener {
                viewModel.copyProfileUrl()
                true
            }

            menuItemReport?.setOnMenuItemClickListener {
                dialog.showToast(R.string.please_click_on_the_arrow_icon_on_the_top_left_and_click_report_block)
                viewModel.loadProfileUrlForWebView()
                true
            }

            profileSwipeRefresh.setOnRefreshListener {
                viewModel.reloadData()
            }
        }
    }

    override fun setUpInsets() {
        binding.root.applyTopPaddingInsets()
        if (!isViewer()) {
            binding.profileRecyclerView.applyBottomPaddingInsets()
        }
    }

    override fun setUpObserver() {
        disposables.addAll(
            viewModel.success.subscribe {
                dialog.showToast(it)
            },
            viewModel.loading.subscribe {
                binding.profileSwipeRefresh.isRefreshing = it
            },
            viewModel.error.subscribe {
                dialog.showToast(it)
            },
            viewModel.profileAdapterComponent.subscribe {
                appSetting = it
                assignAdapter(it)
            },
            viewModel.notLoggedInLayoutVisibility.subscribe {
                binding.notLoggedInLayout.notLoggedInLayout.show(it)
                binding.profileSwipeRefresh.show(!it)
            },
            viewModel.viewerMenuItemVisibility.subscribe {
                menuItemSettings?.isVisible = it
            },
            viewModel.bestFriendVisibility.subscribe {
                menuItemAddAsBestFriend?.isVisible = it
            },
            viewModel.reportMenuItemVisibility.subscribe {
                menuItemReport?.isVisible = it
            },
            viewModel.profileItemList.subscribe {
                profileAdapter?.updateData(it, true)
            },
            viewModel.profileUrlForWebView.subscribe {
                navigation.openWebView(it)
            },
            viewModel.profileUrlForShareSheet.subscribe {
                dialog.showShareSheet(it)
            },
            viewModel.avatarUrlForPreview.subscribe { (url, _) ->
                // Show full screen image without transition from source view
                StfalconImageViewer.Builder<String>(requireContext(), arrayOf(url)) { view, image ->
                    view.load(image)
                }.withHiddenStatusBar(false).show(true)
            },
            viewModel.bannerUrlForPreview.subscribe {
                StfalconImageViewer.Builder<String>(requireContext(), arrayOf(it)) { view, image ->
                    view.load(image)
                }.withHiddenStatusBar(false).show(true)
            },
            viewModel.currentUserId.subscribe {
                currentUserId = it
            }
        )

        viewModel.loadData(
            ProfileParam(
                arguments?.getInt(USER_ID)?.let { if (it == 0) null else it },
                arguments?.getString(USERNAME)
            )
        )
    }

    private fun assignAdapter(appSetting: AppSetting) {
        profileAdapter = ProfileRvAdapter(requireContext(), listOf(), appSetting, screenWidth, getProfileListener())
        binding.profileRecyclerView.adapter = profileAdapter
    }

    private fun getProfileListener(): ProfileListener {
        return object : ProfileListener {
            override val headerListener: ProfileListener.HeaderListener = getHeaderListener()
            override val statsListener: ProfileListener.StatsListener = getStatsListener()
            override val favoriteMediaListener: ProfileListener.FavoriteMediaListener = getFavoriteMediaListener()
            override val favoriteCharacterListener: ProfileListener.FavoriteCharacterListener = getFavoriteCharacterListener()
            override val favoriteStaffListener: ProfileListener.FavoriteStaffListener = getFavoriteStaffListener()
            override val favoriteStudioListener: ProfileListener.FavoriteStudioListener = getFavoriteStudioListener()
        }
    }

    private fun getHeaderListener(): ProfileListener.HeaderListener {
        return object : ProfileListener.HeaderListener {
            override fun onAvatarClick(isCircle: Boolean) {
                viewModel.loadAvatarUrl(isCircle)
            }

            override fun onBannerClick() {
                viewModel.loadBannerUrl()
            }

            override fun onFollowClick() {
                viewModel.toggleFollow()
            }

            override fun onAnimeCountClick() {
                if (isViewer())
                    sharedViewModel.navigateTo(SharedMainViewModel.Page.ANIME)
                else {
                    doIfUserIdIsLoaded {
                        navigation.navigateToAnimeMediaList(currentUserId)
                    }
                }
            }

            override fun onMangaCountClick() {
                if (isViewer())
                    sharedViewModel.navigateTo(SharedMainViewModel.Page.MANGA)
                else {
                    doIfUserIdIsLoaded {
                        navigation.navigateToMangaMediaList(currentUserId)
                    }
                }
            }

            override fun onFollowingCountClick() {
                doIfUserIdIsLoaded {
                    navigation.navigateToFollowing(currentUserId)
                }
            }

            override fun onFollowersCountClick() {
                doIfUserIdIsLoaded {
                    navigation.navigateToFollowers(currentUserId)
                }
            }
        }
    }

    private fun getStatsListener(): ProfileListener.StatsListener {
        return object : ProfileListener.StatsListener {
            override fun navigateToStatsDetail() {
                doIfUserIdIsLoaded {
                    navigation.navigateToUserStats(currentUserId)
                }
            }

            override fun navigateToForceUpdate() {
                 navigation.navigateToAccountSettings()
            }
        }
    }

    private fun getFavoriteMediaListener(): ProfileListener.FavoriteMediaListener {
        return object : ProfileListener.FavoriteMediaListener {
            override fun navigateToFavoriteMedia(mediaType: MediaType) {
                val favorite = when (mediaType) {
                    MediaType.ANIME -> Favorite.ANIME
                    MediaType.MANGA -> Favorite.MANGA
                }
                doIfUserIdIsLoaded {
                    navigation.navigateToFavorite(currentUserId, favorite)
                }
            }

            override fun navigateToMedia(media: Media, mediaType: MediaType) {
                navigation.navigateToMedia(media.getId())
            }
        }
    }

    private fun getFavoriteCharacterListener(): ProfileListener.FavoriteCharacterListener {
        return object : ProfileListener.FavoriteCharacterListener {
            override fun navigateToFavoriteCharacter() {
                doIfUserIdIsLoaded {
                    navigation.navigateToFavorite(currentUserId, Favorite.CHARACTERS)
                }
            }

            override fun navigateToCharacter(character: Character) {
                navigation.navigateToCharacter(character.id)
            }
        }
    }

    private fun getFavoriteStaffListener(): ProfileListener.FavoriteStaffListener {
        return object : ProfileListener.FavoriteStaffListener {
            override fun navigateToFavoriteStaff() {
                doIfUserIdIsLoaded {
                    navigation.navigateToFavorite(currentUserId, Favorite.STAFF)
                }
            }

            override fun navigateToStaff(staff: Staff) {
                navigation.navigateToStaff(staff.id)
            }
        }
    }

    private fun getFavoriteStudioListener(): ProfileListener.FavoriteStudioListener {
        return object : ProfileListener.FavoriteStudioListener {
            override fun navigateToFavoriteStudio() {
                doIfUserIdIsLoaded {
                    navigation.navigateToFavorite(currentUserId, Favorite.STUDIOS)
                }
            }

            override fun navigateToStudio(studio: Studio) {
                navigation.navigateToStudio(studio.id)
            }
        }
    }

    private fun isViewer(): Boolean {
        return arguments?.getInt(USER_ID) == 0 && arguments?.getString(USERNAME) == null
    }

    private fun doIfUserIdIsLoaded(action: () -> Unit) {
        if (currentUserId != 0)
            action()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        menuItemReviews = null
        menuItemActivities = null
        menuItemAddAsBestFriend = null
        menuItemSettings = null
        menuItemViewOnAniList = null
        menuItemShareProfile = null
        menuItemCopyLink = null
        menuItemReport = null
        profileAdapter = null
    }

    companion object {
        private const val USER_ID = "userId"
        private const val USERNAME = "username"

        @JvmStatic
        fun newInstance(userId: Int? = null, username: String? = null) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    if (userId != null) putInt(USER_ID, userId)
                    if (username != null) putString(USERNAME, username)
                }
            }
    }
}