package com.doma.alsan.ui.root

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.doma.alsan.databinding.ActivityRootBinding
import com.doma.alsan.helper.utils.DeepLink
import com.doma.alsan.helper.utils.ImageUtil
import com.doma.alsan.helper.utils.PushNotificationUtil
import com.doma.alsan.type.NotificationUnion
import com.doma.alsan.ui.base.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class RootActivity : BaseActivity<ActivityRootBinding>() {

    override lateinit var dialogManager: DialogManager

    override lateinit var navigationManager: NavigationManager

    private val _incomingDeepLink = PublishSubject.create<DeepLink>()
    override val incomingDeepLink: Observable<DeepLink>
        get() = _incomingDeepLink

    private var newIntent: Intent? = null

    override fun generateViewBinding(): ActivityRootBinding {
        return ActivityRootBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navigationManager.shouldPopFromBrowseScreen()) {
                    navigationManager.popBrowseScreenPage()
                } else if (navigationManager.hasBackStack()) {
                    navigationManager.popBackStack()
                } else {
                    finish()
                }
            }
        })
    }

    override fun setUpLayout() {
        navigationManager = DefaultNavigationManager(this, supportFragmentManager, binding.rootLayout)
        dialogManager = DefaultDialogManager(this)

        ImageUtil.init(this)

        handleDeepLink(intent)
        requestNotificationPermission()
    }

    override fun setUpObserver() {
        // do nothing
    }

    override fun onResume() {
        super.onResume()
        newIntent?.let {
            handleDeepLink(it)
            newIntent = null
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.data != null) {
            newIntent = intent
        }
    }

    private fun handleDeepLink(intent: Intent) {
        val deepLink = DeepLink(intent.data)

        when {
            supportFragmentManager.fragments.isEmpty() -> {
                navigationManager.navigateToSplash(deepLink, intent.getBooleanExtra("RESTART", false))
            }
            navigationManager.isAtPreLoginScreen() -> {
                if (deepLink.isLogin()) {
                    // Safely parse OAuth access token with proper validation
                    val accessToken = deepLink.uri?.encodedFragment?.let { fragment ->
                        val tokenPrefix = "access_token="
                        val startIndex = fragment.indexOf(tokenPrefix)
                        if (startIndex == -1) return@let null
                        
                        val tokenStart = startIndex + tokenPrefix.length
                        val endIndex = fragment.indexOf("&", tokenStart).takeIf { it != -1 } ?: fragment.length
                        
                        fragment.substring(tokenStart, endIndex).takeIf { it.isNotBlank() }
                    }
                    navigationManager.navigateToLogin(accessToken, true)
                } else {
                    navigationManager.navigateToMain(deepLink)
                }
                intent.data = null
            }
            deepLink.uri != null -> {
                _incomingDeepLink.onNext(deepLink)
                intent.data = null
            }
        }
    }

    private fun requestNotificationPermission() {
        PushNotificationUtil.createNotificationChannel(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
            val isPermissionDenied = checkSelfPermission(notificationPermission) != PackageManager.PERMISSION_GRANTED
            val isNeverDeniedOrCurrentlyGranted = !shouldShowRequestPermissionRationale(notificationPermission)
            if (isPermissionDenied && isNeverDeniedOrCurrentlyGranted) {
                requestPermissions(arrayOf(notificationPermission), 0)
            }
        }
    }
}