package com.doma.alsan.ui.settings.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doma.alsan.BuildConfig
import com.doma.alsan.R
import com.doma.alsan.databinding.FragmentAboutBinding
import com.doma.alsan.helper.extensions.applyBottomSidePaddingInsets
import com.doma.alsan.helper.extensions.applyTopPaddingInsets
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.ui.base.BaseFragment
import com.doma.alsan.ui.base.NavigationManager
import org.koin.androidx.viewmodel.ext.android.viewModel


class AboutFragment : BaseFragment<FragmentAboutBinding, AboutViewModel>() {

    override val viewModel: AboutViewModel by viewModel()

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAboutBinding {
        return FragmentAboutBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        binding.apply {
            setUpToolbar(defaultToolbar.defaultToolbar, getString(R.string.about_al_chan))

            aboutSettingsAppVersionText.text = getString(R.string.version, BuildConfig.VERSION_NAME)

            aboutSettingsAniListLink.clicks {
                navigation.openWebView(NavigationManager.Url.ALSAN_FORUM_THREAD)
            }

            aboutSettingsGitHubLink.clicks {
                navigation.openWebView(NavigationManager.Url.ALSAN_GITHUB)
            }

            aboutSettingsGmailLink.clicks {
                navigation.openEmailClient()
            }

            aboutSettingsPlayStoreLink.clicks {
                navigation.openWebView(NavigationManager.Url.ALSAN_PLAY_STORE)
            }

            aboutSettingsTwitterLink.clicks {
                navigation.openWebView(NavigationManager.Url.ALSAN_TWITTER)
            }

            aboutSettingsPrivacyPolicyText.clicks {
                navigation.openWebView(NavigationManager.Url.ALSAN_PRIVACY_POLICY)
            }
        }
    }

    override fun setUpInsets() {
        binding.defaultToolbar.defaultToolbar.applyTopPaddingInsets()
        binding.aboutSettingsLayout.applyBottomSidePaddingInsets()
    }

    override fun setUpObserver() {
        // do nothing
    }

    companion object {
        @JvmStatic
        fun newInstance() = AboutFragment()
    }
}