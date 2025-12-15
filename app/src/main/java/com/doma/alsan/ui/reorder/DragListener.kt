package com.doma.alsan.ui.reorder

import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

interface DragListener {
    fun onStartDrag(viewHolder: BaseRecyclerViewAdapter<*, *>.ViewHolder)
}