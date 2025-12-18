package com.doma.alsan.ui.common

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.doma.alsan.databinding.DialogBottomSheetListBinding
import com.doma.alsan.ui.base.BaseDialogFragment
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class BottomSheetListDialog : BaseDialogFragment<DialogBottomSheetListBinding>() {

    private var adapter: BaseRecyclerViewAdapter<*, *>? = null
    private var onDismissListener: (() -> Unit)? = null

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogBottomSheetListBinding {
        return DialogBottomSheetListBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        binding.dialogRecyclerView.adapter = adapter
    }

    override fun setUpObserver() {
        // do nothing
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
        onDismissListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    companion object {
        fun newInstance(adapter: BaseRecyclerViewAdapter<*, *>, onDismiss: (() -> Unit)? = null) =
            BottomSheetListDialog().apply {
                this.adapter = adapter
                this.onDismissListener = onDismiss
            }
    }
}