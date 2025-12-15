package com.doma.alsan.ui.reorder

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

abstract class BaseReorderRecyclerViewAdapter<T, VB: ViewBinding>(
    list: List<T>
) : BaseRecyclerViewAdapter<T, VB>(list), ItemMoveListener