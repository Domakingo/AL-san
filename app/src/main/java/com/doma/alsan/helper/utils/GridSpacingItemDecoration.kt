package com.doma.alsan.helper.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean,
    private val vSpacing: Int = spacing
) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val layoutManager = parent.layoutManager as? androidx.recyclerview.widget.GridLayoutManager
        val spanSizeLookup = layoutManager?.spanSizeLookup
        val column = spanSizeLookup?.getSpanIndex(position, spanCount) ?: (position % spanCount)
        val spanSize = spanSizeLookup?.getSpanSize(position) ?: 1

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + spanSize) * spacing / spanCount

            val rowIndex = spanSizeLookup?.getSpanGroupIndex(position, spanCount) ?: 0
            val isFullWidth = spanSize == spanCount

            if (rowIndex == 0) {
                outRect.top = vSpacing
            }
            outRect.bottom = if (isFullWidth) 0 else vSpacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + spanSize) * spacing / spanCount

            val rowIndex = spanSizeLookup?.getSpanGroupIndex(position, spanCount) ?: 0
            val isFullWidth = spanSize == spanCount

            if (rowIndex > 0) {
                outRect.top = if (isFullWidth) 0 else vSpacing
            }
        }
    }
}