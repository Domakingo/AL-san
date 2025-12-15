package com.doma.alsan.ui.landing

import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.R
import com.doma.alsan.databinding.FragmentLandingBinding
import com.doma.alsan.helper.extensions.applyTopBottomPaddingInsets
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class LandingFragment : BaseFragment<FragmentLandingBinding, LandingViewModel>() {

    override val viewModel: LandingViewModel by viewModel()

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLandingBinding {
        return FragmentLandingBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        binding.apply {
            ImageUtil.loadImage(requireContext(), R.drawable.landing_wallpaper, landingBackgroundImage)

            landingGetStartedButton.clicks {
                navigation.navigateToLogin()
            }
        }
    }

    override fun setUpInsets() {
        binding.landingContentRoot.applyTopBottomPaddingInsets()
    }

    override fun setUpObserver() {
        // do nothing
    }

    companion object {
        @JvmStatic
        fun newInstance() = LandingFragment()
    }
}