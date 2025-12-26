package com.doma.alsan

import android.app.Application
import com.google.gson.GsonBuilder
import com.doma.alsan.data.datasource.*
import com.doma.alsan.data.localstorage.*
import com.doma.alsan.data.manager.*
import com.doma.alsan.data.network.apollo.AniListApolloHandler
import com.doma.alsan.data.network.apollo.ApolloHandler
import com.doma.alsan.data.network.interceptor.AniListHeaderInterceptor
import com.doma.alsan.data.network.interceptor.HeaderInterceptor
import com.doma.alsan.data.network.interceptor.SpotifyAuthHeaderInterceptor
import com.doma.alsan.data.network.interceptor.SpotifyHeaderInterceptor
import com.doma.alsan.data.network.retrofit.DefaultRetrofitHandler
import com.doma.alsan.data.network.retrofit.RetrofitHandler
import com.doma.alsan.data.repository.*
import com.doma.alsan.helper.Constant
import com.doma.alsan.helper.service.clipboard.ClipboardService
import com.doma.alsan.helper.service.clipboard.DefaultClipboardService
import com.doma.alsan.helper.service.pushnotification.DefaultPushNotificationService
import com.doma.alsan.helper.service.pushnotification.PushNotificationService
import com.doma.alsan.ui.activity.ActivityDetailViewModel
import com.doma.alsan.ui.activity.ActivityListViewModel
import com.doma.alsan.ui.base.BaseActivityViewModel
import com.doma.alsan.ui.calendar.CalendarViewModel
import com.doma.alsan.ui.character.CharacterViewModel
import com.doma.alsan.ui.character.media.CharacterMediaListViewModel
import com.doma.alsan.ui.common.BottomSheetMediaQuickDetailViewModel
import com.doma.alsan.ui.customise.CustomiseViewModel
import com.doma.alsan.ui.editor.EditorViewModel
import com.doma.alsan.ui.explore.ExploreViewModel
import com.doma.alsan.ui.favorite.FavoriteViewModel
import com.doma.alsan.ui.follow.FollowViewModel
import com.doma.alsan.ui.home.HomeViewModel
import com.doma.alsan.ui.landing.LandingViewModel
import com.doma.alsan.ui.login.LoginViewModel
import com.doma.alsan.ui.main.MainViewModel
import com.doma.alsan.ui.main.SharedMainViewModel
import com.doma.alsan.ui.media.character.MediaCharacterListViewModel
import com.doma.alsan.ui.media.MediaViewModel
import com.doma.alsan.ui.media.mediasocial.MediaSocialViewModel
import com.doma.alsan.ui.media.mediastats.MediaStatsViewModel
import com.doma.alsan.ui.media.staff.MediaStaffListViewModel
import com.doma.alsan.ui.media.themes.BottomSheetMediaThemesViewModel
import com.doma.alsan.ui.medialist.BottomSheetMediaListQuickDetailViewModel
import com.doma.alsan.ui.medialist.BottomSheetMediaFilterViewModel
import com.doma.alsan.ui.medialist.MediaListViewModel
import com.doma.alsan.ui.notifications.NotificationsViewModel
import com.doma.alsan.ui.profile.ProfileViewModel
import com.doma.alsan.ui.reorder.ReorderViewModel
import com.doma.alsan.ui.review.ReviewViewModel
import com.doma.alsan.ui.review.reader.ReaderViewModel
import com.doma.alsan.ui.search.SearchViewModel
import com.doma.alsan.ui.seasonal.SeasonalViewModel
import com.doma.alsan.ui.settings.SettingsViewModel
import com.doma.alsan.ui.settings.account.AccountSettingsViewModel
import com.doma.alsan.ui.settings.anilist.AniListSettingsViewModel
import com.doma.alsan.ui.settings.app.AppSettingsViewModel
import com.doma.alsan.ui.settings.list.ListSettingsViewModel
import com.doma.alsan.ui.settings.notifications.NotificationsSettingsViewModel
import com.doma.alsan.ui.social.SocialViewModel
import com.doma.alsan.ui.splash.SplashViewModel
import com.doma.alsan.ui.staff.StaffViewModel
import com.doma.alsan.ui.staff.character.StaffCharacterListViewModel
import com.doma.alsan.ui.staff.media.StaffMediaListViewModel
import com.doma.alsan.ui.studio.StudioViewModel
import com.doma.alsan.ui.studio.media.StudioMediaListViewModel
import com.doma.alsan.ui.texteditor.TextEditorViewModel
import com.doma.alsan.ui.userstats.UserStatsViewModel

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module

class AlsanApplication : Application() {

    @Suppress("DEPRECATION")
    private val appModules = module {
        val gson = GsonBuilder()
            .setLenient()
            .serializeSpecialFloatingPointValues()
            .create()

        // local storage
        single<SharedPreferencesHandler> {
            DefaultSharedPreferencesHandler(
                this@AlsanApplication.applicationContext,
                Constant.SHARED_PREFERENCES_NAME,
                gson
            )
        }

        single<JsonStorageHandler> {
            DefaultJsonStorageHandler(
                this@AlsanApplication,
                gson
            )
        }

        single<FileStorageHandler> {
            DefaultFileStorageHandler(this@AlsanApplication)
        }

        // local storage manager
        single<UserManager> { DefaultUserManager(get(), get(), get()) }
        single<ContentManager> { DefaultContentManager(get()) }
        single<BrowseManager> { DefaultBrowseManager(get()) }

        // network
        val aniListHeaderInterceptor = "aniListHeaderInterceptor"
        val spotifyAuthHeaderInterceptor = "spotifyAuthHeaderInterceptor"
        val spotifyHeaderInterceptor = "spotifyHeaderInterceptor"

        single<HeaderInterceptor>(named(aniListHeaderInterceptor)) { AniListHeaderInterceptor(get()) }
        single<HeaderInterceptor>(named(spotifyAuthHeaderInterceptor)) { SpotifyAuthHeaderInterceptor(get()) }
        single<HeaderInterceptor>(named(spotifyHeaderInterceptor)) { SpotifyHeaderInterceptor(get()) }
        single<ApolloHandler> { AniListApolloHandler(get(named(aniListHeaderInterceptor)), Constant.ANILIST_API_BASE_URL) }
        single<RetrofitHandler> {
            DefaultRetrofitHandler(
                Constant.ALCHAN_RAW_GITHUB_URL,
                Constant.JIKAN_API_URL,
                Constant.ANIME_THEMES_API_URL,
                Constant.YOUTUBE_SEARCH_API_URL,
                Constant.SPOTIFY_AUTH_API_URL,
                get(named(spotifyAuthHeaderInterceptor)),
                Constant.SPOTIFY_API_URL,
                get(named(spotifyHeaderInterceptor))
            )
        }

        // data source
        single<ContentDataSource> { DefaultContentDataSource(get(), Constant.ANILIST_API_STATUS_VERSION, Constant.ANILIST_API_SOURCE_VERSION) }
        single<UserDataSource> { DefaultUserDataSource(get()) }
        single<MediaListDataSource> { DefaultMediaListDataSource(get(), Constant.ANILIST_API_STATUS_VERSION, Constant.ANILIST_API_SOURCE_VERSION) }
        single<BrowseDataSource> { DefaultBrowseDataSource(get(), get(), Constant.ANILIST_API_STATUS_VERSION, Constant.ANILIST_API_SOURCE_VERSION, Constant.ANILIST_API_RELATION_TYPE_VERSION) }
        single<SocialDataSource> { DefaultSocialDataSource(get()) }
        single<InfoDataSource> { DefaultInfoDataSource(get()) }

        // repository
        single<ContentRepository> { DefaultContentRepository(get(), get()) }
        single<UserRepository> { DefaultUserRepository(get(), get()) }
        single<MediaListRepository> { DefaultMediaListRepository(get(), get()) }
        single<BrowseRepository> { DefaultBrowseRepository(get(), get()) }
        single<SocialRepository> { DefaultSocialRepository(get()) }
        single<InfoRepository> { DefaultInfoRepository(get(), get()) }

        // service
        single<ClipboardService> { DefaultClipboardService(this.androidContext()) }
        single<PushNotificationService> { DefaultPushNotificationService(this.androidContext(), get()) }

        // view model
        viewModel { BaseActivityViewModel(get()) }

        viewModel { SplashViewModel(get(), get()) }
        viewModel { LandingViewModel() }
        viewModel { LoginViewModel(get()) }

        viewModel { SharedMainViewModel() }
        viewModel { MainViewModel(get(), get(), get()) }

        viewModel { BottomSheetMediaQuickDetailViewModel(get()) }
        viewModel { BottomSheetMediaListQuickDetailViewModel(get(), get()) }
        viewModel { BottomSheetMediaThemesViewModel(get()) }

        viewModel { HomeViewModel(get(), get(), get()) }
        viewModel { SearchViewModel(get(), get()) }
        viewModel { SeasonalViewModel(get(), get(), get()) }
        viewModel { ExploreViewModel(get(), get()) }
        viewModel { CalendarViewModel(get(), get()) }
        viewModel { ReviewViewModel(get(), get()) }
        viewModel { ReaderViewModel(get(), get(), get()) }

        viewModel { MediaListViewModel(get(), get(), get(), get()) }
        viewModel { BottomSheetMediaFilterViewModel(get()) }

        viewModel { NotificationsViewModel(get()) }

        viewModel { ProfileViewModel(get(), get(), get(), get()) }
        viewModel { FollowViewModel(get()) }
        viewModel { UserStatsViewModel(get(), get()) }
        viewModel { FavoriteViewModel(get()) }

        viewModel { SettingsViewModel() }
        viewModel { AppSettingsViewModel(get(), get()) }
        viewModel { AniListSettingsViewModel(get()) }
        viewModel { ListSettingsViewModel(get()) }
        viewModel { NotificationsSettingsViewModel(get()) }
        viewModel { AccountSettingsViewModel(get()) }

        viewModel { ReorderViewModel() }

        viewModel { CustomiseViewModel(get(), get()) }

        viewModel { EditorViewModel(get(), get()) }

        viewModel { MediaViewModel(get(), get(), get(), get()) }
        viewModel { MediaStatsViewModel(get()) }
        viewModel { MediaSocialViewModel(get(), get()) }
        viewModel { MediaCharacterListViewModel(get(), get()) }
        viewModel { MediaStaffListViewModel(get(), get()) }
        viewModel { CharacterViewModel(get(), get(), get()) }
        viewModel { CharacterMediaListViewModel(get(), get()) }
        viewModel { StaffViewModel(get(), get(), get()) }
        viewModel { StaffCharacterListViewModel(get(), get()) }
        viewModel { StaffMediaListViewModel(get(), get()) }
        viewModel { StudioViewModel(get(), get(), get()) }
        viewModel { StudioMediaListViewModel(get(), get()) }

        viewModel { SocialViewModel(get(), get(), get()) }
        viewModel { ActivityDetailViewModel(get(), get(), get()) }
        viewModel { ActivityListViewModel(get(), get(), get()) }
        viewModel { TextEditorViewModel(get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AlsanApplication)
            modules(appModules)
        }
    }
}