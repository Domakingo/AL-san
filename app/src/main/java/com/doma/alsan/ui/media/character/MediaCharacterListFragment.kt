package com.doma.alsan.ui.media.character

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Character
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.databinding.LayoutInfiniteScrollingBinding
import com.doma.alsan.helper.extensions.applyBottomPaddingInsets
import com.doma.alsan.helper.extensions.applyTopPaddingInsets
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.utils.GridSpacingItemDecoration
import com.doma.alsan.ui.base.BaseFragment
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.type.StaffLanguage
import org.koin.androidx.viewmodel.ext.android.viewModel


class MediaCharacterListFragment : BaseFragment<LayoutInfiniteScrollingBinding, MediaCharacterListViewModel>() {

    override val viewModel: MediaCharacterListViewModel by viewModel()

    private var adapter: MediaCharacterListRvAdapter? = null

    private var menuItemChangeVaLanguage: MenuItem? = null
    private var menuItemSearch: MenuItem? = null
    private var searchView: SearchView? = null
    private var appSetting = AppSetting()

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutInfiniteScrollingBinding {
        return LayoutInfiniteScrollingBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        val mediaType = getMediaTypeFromArgs()

        binding.apply {
            setUpToolbar(defaultToolbar.defaultToolbar, getString(R.string.character_list))
            defaultToolbar.defaultToolbar.inflateMenu(R.menu.menu_character_list)

            // Search menu item
            menuItemSearch = defaultToolbar.defaultToolbar.menu.findItem(R.id.itemSearch)
            searchView = menuItemSearch?.actionView as? SearchView
            
            if (mediaType == MediaType.MANGA) {
                searchView?.queryHint = getString(R.string.search_characters)
            } else {
                searchView?.queryHint = getString(R.string.search_character_or_voice_actor)
            }

            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.searchCharacters(newText ?: "")
                    return true
                }
            })

            // Change language menu item
            menuItemChangeVaLanguage = defaultToolbar.defaultToolbar.menu.findItem(R.id.itemChangeLanguage)
            
            // Apply theme color to icon programmatically
            menuItemChangeVaLanguage?.icon?.let { icon ->
                val typedValue = TypedValue()
                requireContext().theme.resolveAttribute(R.attr.themeContentColor, typedValue, true)
                val wrappedIcon = DrawableCompat.wrap(icon.mutate())
                DrawableCompat.setTint(wrappedIcon, typedValue.data)
                menuItemChangeVaLanguage?.icon = wrappedIcon
            }

            if (mediaType == MediaType.MANGA) {
                menuItemChangeVaLanguage?.isVisible = false
            }
            
            menuItemChangeVaLanguage?.setOnMenuItemClickListener {
                viewModel.loadVoiceActorLanguages()
                true
            }

            // Hide language button when search is expanded
            menuItemSearch?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    if (mediaType != MediaType.MANGA) {
                        menuItemChangeVaLanguage?.isVisible = false
                    }
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    if (mediaType != MediaType.MANGA) {
                        menuItemChangeVaLanguage?.isVisible = true
                    }
                    // Clear the search when collapsing
                    viewModel.searchCharacters("")
                    return true
                }
            })

            adapter = MediaCharacterListRvAdapter(requireContext(), listOf(), appSetting, getMediaCharacterListListener())
            infiniteScrollingRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.gridSpan))
            infiniteScrollingRecyclerView.addItemDecoration(GridSpacingItemDecoration(resources.getInteger(R.integer.gridSpan), resources.getDimensionPixelSize(R.dimen.marginNormal), false))
            infiniteScrollingRecyclerView.adapter = adapter

            infiniteScrollingSwipeRefresh.setOnRefreshListener {
                searchView?.setQuery("", false)
                searchView?.isIconified = true
                viewModel.reloadData()
            }

            infiniteScrollingRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1)) {
                        viewModel.loadNextPage()
                    }
                }
            })
        }
    }

    override fun setUpInsets() {
        binding.defaultToolbar.defaultToolbar.applyTopPaddingInsets()
        binding.infiniteScrollingRecyclerView.applyBottomPaddingInsets()
    }

    override fun setUpObserver() {
        val mediaType = getMediaTypeFromArgs()

        disposables.addAll(
            viewModel.loading.subscribe {
                binding.infiniteScrollingSwipeRefresh.isRefreshing = it
            },
            viewModel.error.subscribe {
                dialog.showToast(it)
            },
            viewModel.appSetting.subscribe {
                appSetting = it
                adapter = MediaCharacterListRvAdapter(requireContext(), listOf(), it, getMediaCharacterListListener())
                binding.infiniteScrollingRecyclerView.adapter = adapter
            },
            viewModel.characters.subscribe {
                adapter?.updateData(it, true)
            },
            viewModel.emptyLayoutVisibility.subscribe {
                binding.emptyLayout.emptyLayout.show(it)
            },
            viewModel.voiceActorLanguages.subscribe {
                dialog.showListDialog(it) { data, _ ->
                    viewModel.updateVoiceActorLanguage(data)
                }
            }
        )

        arguments?.getInt(MEDIA_ID)?.let {
            val selectedLanguage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                arguments?.getSerializable(SELECTED_LANGUAGE, StaffLanguage::class.java)
            } else {
                @Suppress("DEPRECATION")
                arguments?.getSerializable(SELECTED_LANGUAGE) as? StaffLanguage
            }

            val availableLanguages = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                arguments?.getSerializable(AVAILABLE_LANGUAGES, ArrayList::class.java) as? ArrayList<StaffLanguage>
            } else {
                @Suppress("DEPRECATION")
                arguments?.getSerializable(AVAILABLE_LANGUAGES) as? ArrayList<StaffLanguage>
            }

            viewModel.loadData(MediaCharacterListParam(it, mediaType, selectedLanguage, availableLanguages))
        }
    }

    private fun getMediaCharacterListListener(): MediaCharacterListRvAdapter.MediaCharacterListListener {
        return object : MediaCharacterListRvAdapter.MediaCharacterListListener {
            override fun navigateToCharacter(character: Character) {
                navigation.navigateToCharacter(character.id)
            }

            override fun navigateToStaff(staff: Staff) {
                navigation.navigateToStaff(staff.id)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        menuItemChangeVaLanguage = null
        menuItemSearch = null
        searchView = null
    }

    private fun getMediaTypeFromArgs(): MediaType {
        val args = arguments ?: return MediaType.ANIME
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            args.getSerializable(MEDIA_TYPE, MediaType::class.java) ?: MediaType.ANIME
        } else {
            @Suppress("DEPRECATION")
            args.getSerializable(MEDIA_TYPE) as? MediaType ?: MediaType.ANIME
        }
    }

    companion object {
        private const val MEDIA_ID = "mediaId"
        private const val MEDIA_TYPE = "mediaType"
        private const val SELECTED_LANGUAGE = "selectedLanguage"
        private const val AVAILABLE_LANGUAGES = "availableLanguages"

        @JvmStatic
        fun newInstance(
            mediaId: Int,
            mediaType: MediaType = MediaType.ANIME,
            selectedLanguage: StaffLanguage? = null,
            availableLanguages: List<StaffLanguage>? = null
        ) =
            MediaCharacterListFragment().apply {
                arguments = Bundle().apply {
                    putInt(MEDIA_ID, mediaId)
                    putSerializable(MEDIA_TYPE, mediaType)
                    selectedLanguage?.let { putSerializable(SELECTED_LANGUAGE, it) }
                    availableLanguages?.let { putSerializable(AVAILABLE_LANGUAGES, ArrayList(it)) }
                }
            }
    }
}