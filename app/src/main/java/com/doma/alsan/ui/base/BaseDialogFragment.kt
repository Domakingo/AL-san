package com.doma.alsan.ui.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class BaseDialogFragment<VB: ViewBinding> : BottomSheetDialogFragment(), ViewContract {

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding!!

    abstract fun generateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    protected val disposables = CompositeDisposable()
    private var insetsController: WindowInsetsControllerCompat? = null

    protected var screenWidth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.window?.let {
            insetsController = WindowInsetsControllerCompat(it, it.decorView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = generateViewBinding(activity?.layoutInflater ?: inflater, container)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            
            // Fix for floating menu: allow drawing behind navigation bar
            bottomSheetDialog.window?.let { window ->
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.setFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                
                // Nuclear window level clean
                window.decorView.setPadding(0, 0, 0, 0)
                window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
            }

            // Target the internal Material dialog structure
            val container = bottomSheetDialog.findViewById<android.view.View>(com.google.android.material.R.id.container)
            val coordinator = bottomSheetDialog.findViewById<android.view.View>(com.google.android.material.R.id.coordinator)
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            
            // Remove any hidden padding that the Material library adds to the container
            container?.setPadding(0, 0, 0, 0)
            coordinator?.setPadding(0, 0, 0, 0)
            
            bottomSheet?.let {
                it.fitsSystemWindows = false
                it.setPadding(0, 0, 0, 0)
                (it.layoutParams as? android.view.ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
                
                // Force the FrameLayout to physically occupy the bottom
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets -> insets }
                
                val behavior = BottomSheetBehavior.from(it)
                behavior.isFitToContents = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isGestureInsetBottomIgnored = true
            }
        }
        return dialog
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels

        setUpLayout()
    }

    override fun onStart() {
        super.onStart()
        setUpObserver()
    }

    override fun onResume() {
        super.onResume()
        if (disposables.isDisposed) {
            setUpObserver()
        }
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        closeKeyboard()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        closeKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        insetsController = null
    }

    protected fun openKeyboard() {
        insetsController?.show(WindowInsetsCompat.Type.ime())
    }

    protected fun closeKeyboard() {
        insetsController?.hide(WindowInsetsCompat.Type.ime())
    }
}