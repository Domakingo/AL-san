package com.doma.alsan.ui.media.mediastats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.doma.alsan.R
import com.doma.alsan.data.response.anilist.Media
import com.doma.alsan.databinding.LayoutInfiniteScrollingBinding
import com.doma.alsan.helper.extensions.applyBottomSidePaddingInsets
import com.doma.alsan.helper.extensions.applyTopPaddingInsets
import com.doma.alsan.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class MediaStatsFragment : BaseFragment<LayoutInfiniteScrollingBinding, MediaStatsViewModel>() {

    private lateinit var media: Media

    override val viewModel: MediaStatsViewModel by viewModel()

    private var adapter: MediaStatsRvAdapter? = null

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutInfiniteScrollingBinding {
        return LayoutInfiniteScrollingBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        with(binding) {
            setUpToolbar(defaultToolbar.defaultToolbar, getString(R.string.media_stats))

            adapter = MediaStatsRvAdapter(requireContext(), listOf())
            infiniteScrollingRecyclerView.layoutManager = LinearLayoutManager(context)
            infiniteScrollingRecyclerView.adapter = adapter

            infiniteScrollingSwipeRefresh.isEnabled = false
        }
    }

    override fun setUpInsets() {
        with(binding) {
            defaultToolbar.defaultToolbar.applyTopPaddingInsets()
            infiniteScrollingRecyclerView.applyBottomSidePaddingInsets()
        }
    }

    override fun setUpObserver() {
        disposables.addAll(
            viewModel.appSetting.subscribe {
                binding.defaultToolbar.defaultToolbar.subtitle = media.getTitle(it)
            },
            viewModel.mediaStatsItems.subscribe {
                adapter?.updateData(it, true)
            }
        )

        viewModel.loadData(MediaStatsParam(media))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    companion object {
        @JvmStatic
        fun newInstance(media: Media) =
            MediaStatsFragment().apply {
                this.media = media
            }
    }
}