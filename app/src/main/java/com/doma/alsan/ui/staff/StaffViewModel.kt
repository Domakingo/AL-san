package com.doma.alsan.ui.staff

import com.doma.alsan.R
import com.doma.alsan.data.entity.AppSetting
import com.doma.alsan.data.repository.BrowseRepository
import com.doma.alsan.data.repository.UserRepository
import com.doma.alsan.data.response.anilist.MediaEdge
import com.doma.alsan.data.response.anilist.Staff
import com.doma.alsan.helper.enums.Source
import com.doma.alsan.helper.extensions.applyScheduler
import com.doma.alsan.helper.extensions.getStringResource
import com.doma.alsan.helper.pojo.MediaItem
import com.doma.alsan.helper.pojo.StaffItem
import com.doma.alsan.helper.service.clipboard.ClipboardService
import com.doma.alsan.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import androidx.annotation.StringRes
import com.doma.alsan.type.MediaType

class StaffViewModel(
    private val browseRepository: BrowseRepository,
    private val userRepository: UserRepository,
    private val clipboardService: ClipboardService
) : BaseViewModel<StaffParam>() {

    private val _staffAdapterComponent = PublishSubject.create<AppSetting>()
    val staffAdapterComponent: Observable<AppSetting>
        get() = _staffAdapterComponent

    private val _staffImage = BehaviorSubject.createDefault("")
    val staffImage: Observable<String>
        get() = _staffImage

    private val _staffName = BehaviorSubject.createDefault("")
    val staffName: Observable<String>
        get() = _staffName

    private val _mediaOrCharacterCount = BehaviorSubject.createDefault(0)
    val mediaOrCharacterCount: Observable<Int>
        get() = _mediaOrCharacterCount

    private val _mediaOrCharacterText = BehaviorSubject.createDefault(R.string.media)
    val mediaOrCharacterText: Observable<Int>
        get() = _mediaOrCharacterText

    private val _mediaOrCharacterCountVisibility = BehaviorSubject.createDefault(false)
    val mediaOrCharacterCountVisibility: Observable<Boolean>
        get() = _mediaOrCharacterCountVisibility

    private val _favoritesCount = BehaviorSubject.createDefault(0)
    val favoritesCount: Observable<Int>
        get() = _favoritesCount

    private val _isFavorite = BehaviorSubject.createDefault(false)
    val isFavorite: Observable<Boolean>
        get() = _isFavorite

    private val _staffItemList = BehaviorSubject.createDefault(listOf<StaffItem>())
    val staffItemList: Observable<List<StaffItem>>
        get() = _staffItemList

    enum class StaffTab(@StringRes val stringRes: Int) {
        CHARACTERS(R.string.characters),
        MEDIA(R.string.media)
    }

    private val _currentTab = BehaviorSubject.createDefault(StaffTab.MEDIA)
    val currentTab: Observable<StaffTab>
        get() = _currentTab

    private val _staffMetadata = PublishSubject.create<Staff>()
    val staffMetadata: Observable<Staff>
        get() = _staffMetadata

    private val _staffLink = PublishSubject.create<String>()
    val staffLink: Observable<String>
        get() = _staffLink

    private val _staffImageForPreview = PublishSubject.create<String>()
    val staffImageForPreview: Observable<String>
        get() = _staffImageForPreview

    private var staffId = 0

    private var staff: Staff = Staff()
    private var appSetting: AppSetting = AppSetting()

    override fun loadData(param: StaffParam) {
        loadOnce {
            staffId = param.staffId

            disposables.add(
                userRepository.getIsAuthenticated().zipWith(userRepository.getAppSetting()) { isAuthenticated, appSetting ->
                    return@zipWith isAuthenticated to appSetting
                }
                    .applyScheduler()
                    .subscribe { (isAuthenticated, appSetting) ->
                        this.appSetting = appSetting
                        _isAuthenticated.onNext(isAuthenticated)
                        _staffAdapterComponent.onNext(appSetting)
                        loadStaff()
                    }
            )
        }

        if (staff.id != 0)
            checkFavorite()
    }

    fun reloadData() {
        loadStaff()
    }

    private fun checkFavorite() {
        if (staff.id == 0)
            return

        if (_isAuthenticated.value != true) {
            _isAuthenticated.onNext(_isAuthenticated.value ?: false)
            return
        }

        disposables.add(
            userRepository.getViewer(Source.CACHE)
                .map {
                    it.favourites.staff.nodes.find { it.id == staff.id } != null
                }
                .applyScheduler()
                .subscribe {
                    _isFavorite.onNext(it)
                }
        )
    }

    private fun loadStaff() {
        _loading.onNext(true)

        disposables.add(
            browseRepository.getStaff(staffId, 1)
                .applyScheduler()
                .doFinally { _loading.onNext(false) }
                .subscribe(
                    { staff ->
                        this.staff = staff

                        _staffMetadata.onNext(staff)
                        _staffImage.onNext(staff.getImage(appSetting))
                        _staffName.onNext(staff.name.userPreferred)
                        _favoritesCount.onNext(staff.favourites)
                        _isFavorite.onNext(staff.isFavourite)

                        val mediaCount = staff.staffMedia.pageInfo.total + staff.characterMedia.pageInfo.total
                        val characterCount = staff.characters.pageInfo.total
                        
                        _mediaOrCharacterCount.onNext(mediaCount + characterCount)
                        _mediaOrCharacterText.onNext(R.string.media)
                        _mediaOrCharacterCountVisibility.onNext(true)

                        updateStaffItemList()
                    },
                    {
                        _error.onNext(it.getStringResource())
                    }
                )
        )
    }

    fun loadStaffLink() {
        _staffLink.onNext(staff.siteUrl)
    }

    fun copyStaffLink() {
        disposables.add(
            clipboardService.copyPlainText(staff.siteUrl)
                .applyScheduler()
                .subscribe(
                    {
                        _success.onNext(R.string.link_copied)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun loadStaffImage() {
        if (staff.image.large.isNotBlank())
            _staffImageForPreview.onNext(staff.image.large)
    }

    fun toggleFavorite() {
        _loading.onNext(true)

        disposables.add(
            userRepository.toggleFavorite(staffId = staff.id)
                .applyScheduler()
                .doFinally {
                    _loading.onNext(false)
                }
                .subscribe(
                    {
                        val isFavorited = _isFavorite.value ?: false
                        _isFavorite.onNext(!isFavorited)
                    },
                    {
                        _error.onNext(it.getStringResource())
                    }
                )
        )
    }

    fun setTab(tab: StaffTab) {
        if (_currentTab.value == tab) return
        _currentTab.onNext(tab)
        updateStaffItemList()
    }

    private fun updateStaffItemList() {
        if (staff.id == 0) return

        val itemList = ArrayList<StaffItem>()
        
        // Characters section (Voice Roles)
        if (staff.characters.edges.isNotEmpty()) {
            itemList.add(StaffItem(title = "Characters", viewType = StaffItem.VIEW_TYPE_CHARACTER_GROUP))
            staff.characters.edges.forEach { edge ->
                itemList.add(StaffItem(characterEdge = edge, viewType = StaffItem.VIEW_TYPE_CHARACTER_ITEM))
            }
        }

        // Media section (Production + Voice Roles)
        val mergedEdges = (staff.staffMedia.edges + staff.characterMedia.edges)
            .groupBy { it.node.getId() }
            .map { (id, edges) ->
                if (edges.size > 1) {
                    // Merge roles
                    val first = edges.find { it.staffRole.isNotBlank() } ?: edges.first()
                    val staffRoles = edges.mapNotNull { if (it.staffRole.isNotBlank()) it.staffRole else null }.distinct().joinToString(", ")
                    val characterRoles = edges.mapNotNull { if (it.characterName.isNotBlank()) "${it.characterName} (${it.getCharacterRoleString()})" else null }.distinct().joinToString(", ")
                    
                    first.copy(
                        staffRole = if (staffRoles.isNotBlank() && characterRoles.isNotBlank()) "$staffRoles | $characterRoles" else staffRoles.ifBlank { characterRoles }
                    )
                } else {
                    val edge = edges.first()
                    if (edge.staffRole.isBlank() && edge.characterName.isNotBlank()) {
                        edge.copy(staffRole = "${edge.characterName} (${edge.getCharacterRoleString()})")
                    } else {
                        edge
                    }
                }
            }

        if (mergedEdges.isNotEmpty()) {
            val mediaGroups = mergedEdges.groupBy { it.node.type }
            
            mediaGroups[MediaType.ANIME]?.let { edges ->
                itemList.add(StaffItem(title = "Anime", viewType = StaffItem.VIEW_TYPE_MEDIA_GROUP))
                edges.sortedByDescending { it.node.popularity }.forEach { edge ->
                    itemList.add(StaffItem(mediaEdge = edge, viewType = StaffItem.VIEW_TYPE_MEDIA_ITEM))
                }
            }
            
            mediaGroups[MediaType.MANGA]?.let { edges ->
                itemList.add(StaffItem(title = "Manga", viewType = StaffItem.VIEW_TYPE_MEDIA_GROUP))
                edges.sortedByDescending { it.node.popularity }.forEach { edge ->
                    itemList.add(StaffItem(mediaEdge = edge, viewType = StaffItem.VIEW_TYPE_MEDIA_ITEM))
                }
            }
            
            val others = mergedEdges.filter { it.node.type != MediaType.ANIME && it.node.type != MediaType.MANGA }
            if (others.isNotEmpty()) {
                itemList.add(StaffItem(title = "Others", viewType = StaffItem.VIEW_TYPE_MEDIA_GROUP))
                others.sortedByDescending { it.node.popularity }.forEach { edge ->
                    itemList.add(StaffItem(mediaEdge = edge, viewType = StaffItem.VIEW_TYPE_MEDIA_ITEM))
                }
            }
        }

        _staffItemList.onNext(itemList)
    }
}