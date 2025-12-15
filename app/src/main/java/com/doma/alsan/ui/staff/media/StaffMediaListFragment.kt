package com.doma.alsan.ui.staff.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.databinding.LayoutInfiniteScrollingBinding
import com.doma.alsan.helper.extensions.applyBottomPaddingInsets
import com.doma.alsan.helper.extensions.applyTopPaddingInsets
import com.doma.alsan.helper.extensions.getStringResource
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.pojo.StaffMediaListAdapterComponent
import com.doma.alsan.helper.utils.GridSpacingItemDecoration
import com.doma.alsan.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.doma.alsan.type.MediaSort


class StaffMediaListFragment : BaseFragment<LayoutInfiniteScrollingBinding, StaffMediaListViewModel>() {

    override val viewModel: StaffMediaListViewModel by viewModel()

    private var adapter: StaffMediaListRvAdapter? = null
    private var adapterComponent = StaffMediaListAdapterComponent()

    private var menuSortBy: MenuItem? = null
    private var menuShowHideOnList: MenuItem? = null

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutInfiniteScrollingBinding {
        return LayoutInfiniteScrollingBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        binding.apply {
            setUpToolbar(defaultToolbar.defaultToolbar, getString(R.string.media_list))
            defaultToolbar.defaultToolbar.inflateMenu(R.menu.menu_staff_media_list)
            defaultToolbar.defaultToolbar.subtitle = getString(R.string.sorted_by_x, getString(adapterComponent.mediaSort.getStringResource()))

            menuSortBy = defaultToolbar.defaultToolbar.menu.findItem(R.id.itemSortBy)
            menuShowHideOnList = defaultToolbar.defaultToolbar.menu.findItem(R.id.itemShowHideOnList)

            menuSortBy?.setOnMenuItemClickListener {
                viewModel.loadMediaSorts()
                true
            }

            menuShowHideOnList?.setOnMenuItemClickListener {
                viewModel.loadShowHideOnLists()
                true
            }

            adapter = StaffMediaListRvAdapter(requireContext(), listOf(), adapterComponent.appSetting, adapterComponent.mediaSort, getStaffMediaListListener())
            infiniteScrollingRecyclerView.layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.gridSpan))
            infiniteScrollingRecyclerView.addItemDecoration(GridSpacingItemDecoration(resources.getInteger(R.integer.gridSpan), resources.getDimensionPixelSize(R.dimen.marginNormal), false))
            infiniteScrollingRecyclerView.adapter = adapter

            infiniteScrollingSwipeRefresh.setOnRefreshListener {
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
        disposables.addAll(
            viewModel.loading.subscribe {
                binding.infiniteScrollingSwipeRefresh.isRefreshing = it
            },
            viewModel.error.subscribe {
                dialog.showToast(it)
            },
            viewModel.adapterComponent.subscribe {
                adapterComponent = it
                adapter = StaffMediaListRvAdapter(requireContext(), listOf(), it.appSetting, it.mediaSort, getStaffMediaListListener())
                binding.infiniteScrollingRecyclerView.adapter = adapter
                binding.defaultToolbar.defaultToolbar.subtitle = getString(R.string.sorted_by_x, getString(it.mediaSort.getStringResource()))
            },
            viewModel.media.subscribe {
                adapter?.updateData(it, true)
            },
            viewModel.emptyLayoutVisibility.subscribe {
                binding.emptyLayout.emptyLayout.show(it)
            },
            viewModel.mediaSortList.subscribe {
                dialog.showListDialog(it) { data, _ ->
                    viewModel.updateMediaSort(data)
                }
            },
            viewModel.showHideOnListList.subscribe {
                dialog.showListDialog(it) { data, _ ->
                    viewModel.updateShowHideOnList(data)
                }
            }
        )

        arguments?.getInt(STAFF_ID)?.let {
            viewModel.loadData(StaffMediaListParam(it))
        }
    }

    private fun getStaffMediaListListener(): StaffMediaListRvAdapter.StaffMediaListListener {
        return object : StaffMediaListRvAdapter.StaffMediaListListener {
            override fun navigateToMedia(media: Media) {
                navigation.navigateToMedia(media.getId())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        menuSortBy = null
        menuShowHideOnList = null
    }

    companion object {
        const val STAFF_ID = "staffId"
        @JvmStatic
        fun newInstance(staffId: Int) =
            StaffMediaListFragment().apply {
                arguments = Bundle().apply {
                    putInt(STAFF_ID, staffId)
                }
            }
    }
}