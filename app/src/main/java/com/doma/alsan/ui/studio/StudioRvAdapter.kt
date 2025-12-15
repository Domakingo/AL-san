package com.doma.alsan.ui.studio

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.databinding.LayoutTitleAndListBinding
import com.doma.alsan.databinding.LayoutTitleAndTextBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.pojo.StudioItem
import com.doma.alsan.helper.utils.GridSpacingItemDecoration
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class StudioRvAdapter(
    private val context: Context,
    list: List<StudioItem>,
    private val appSetting: AppSetting,
    private val listener: StudioListener
) : BaseRecyclerViewAdapter<StudioItem, ViewBinding>(list) {

    companion object {
        private const val MEDIA_LIMIT = 9
    }

    private var studioMediaAdapter: StudioMediaRvAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = LayoutTitleAndListBinding.inflate(inflater, parent, false)
        studioMediaAdapter = StudioMediaRvAdapter(context, listOf(), appSetting, listener)
        view.listRecyclerView.layoutManager = GridLayoutManager(context, context.resources.getInteger(R.integer.gridSpan))
        view.listRecyclerView.adapter = studioMediaAdapter
        view.listRecyclerView.addItemDecoration(GridSpacingItemDecoration(context.resources.getInteger(R.integer.gridSpan), context.resources.getDimensionPixelSize(R.dimen.marginNormal), false))
        view.listRecyclerView.isNestedScrollingEnabled = false
        return MediaViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    inner class MediaViewHolder(private val binding: LayoutTitleAndListBinding) : ViewHolder(binding) {
        override fun bind(item: StudioItem, index: Int) {
            binding.apply {
                titleText.text = context.getString(R.string.media)
                seeMoreText.show(item.studio.media.edges.size > MEDIA_LIMIT)
                seeMoreText.clicks { listener.navigateToStudioMedia() }
                studioMediaAdapter?.updateData(item.studio.media.edges.take(MEDIA_LIMIT))
            }
        }
    }
}