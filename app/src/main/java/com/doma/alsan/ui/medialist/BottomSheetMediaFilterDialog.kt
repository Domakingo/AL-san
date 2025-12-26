package com.doma.alsan.ui.medialist

import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.R
import com.doma.alsan.data.entity.MediaFilter
import com.doma.alsan.data.response.anilist.MediaListGroup
import com.doma.alsan.databinding.DialogBottomSheetMediaFilterBinding
import com.doma.alsan.helper.enums.MediaType
import com.doma.alsan.helper.enums.Sort
import com.doma.alsan.helper.enums.getString
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.show
import com.doma.alsan.helper.pojo.ListItem
import com.doma.alsan.helper.pojo.SliderItem
import com.doma.alsan.type.ScoreFormat
import com.doma.alsan.ui.base.BaseActivity
import com.doma.alsan.ui.base.BaseDialogFragment
import com.doma.alsan.ui.base.DialogManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class BottomSheetMediaFilterDialog : BaseDialogFragment<DialogBottomSheetMediaFilterBinding>() {

    private val viewModel by viewModel<BottomSheetMediaFilterViewModel>()

    private var listener: MediaFilterListener? = null
    private var isLoadingIncludedGenres = true // Track which genre list to update
    
    // Params to be set before showing the dialog
    private var params: BottomSheetMediaFilterParam? = null
    
    // DialogManager from parent activity
    private val dialogManager: DialogManager? by lazy {
        (activity as? BaseActivity<*>)?.dialogManager
    }

    interface MediaFilterListener {
        fun onFilterApplied(mediaFilter: MediaFilter, sectionIndex: Int)
    }

    override fun generateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogBottomSheetMediaFilterBinding {
        return DialogBottomSheetMediaFilterBinding.inflate(inflater, container, false)
    }

    override fun setUpLayout() {
        binding.apply {
            // Section card click - shows list dialog
            filterSectionCard.clicks {
                showSectionDialog()
            }
            
            // Sort By card click
            filterSortByCard.clicks {
                viewModel.loadSortByOptions()
            }

            // Order By toggle
            filterOrderByCard.setOnClickListener {
                viewModel.toggleOrderBy()
            }

            // Filter option cards
            filterFormatCard.clicks {
                viewModel.loadMediaFormats()
            }

            filterStatusCard.clicks {
                viewModel.loadMediaStatuses()
            }

            filterSourceCard.clicks {
                viewModel.loadMediaSources()
            }

            filterCountryCard.clicks {
                viewModel.loadCountries()
            }

            filterSeasonCard.clicks {
                viewModel.loadSeasons()
            }

            filterYearCard.clicks {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val sliderItem = SliderItem(
                    1970,
                    currentYear + 1,
                    viewModel.getMinYear(),
                    viewModel.getMaxYear()
                )
                dialogManager?.showSliderDialog(sliderItem) { minValue, maxValue ->
                    viewModel.updateYears(minValue, maxValue)
                }
            }

            filterGenreIncludeCard.clicks {
                isLoadingIncludedGenres = true
                viewModel.loadIncludedGenres()
            }

            filterGenreExcludeCard.clicks {
                isLoadingIncludedGenres = false
                viewModel.loadExcludedGenres()
            }

            // Persist filter checkbox
            filterPersistCheckBox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updatePersistFilter(isChecked)
            }

            filterPersistLabel.clicks {
                filterPersistCheckBox.isChecked = !filterPersistCheckBox.isChecked
            }
            
            // Persist info button - shows explanation dialog
            filterPersistInfoButton.clicks {
                dialogManager?.showMessageDialog(
                    R.string.persist_filter,
                    R.string.persist_filter_explanation,
                    android.R.string.ok
                )
            }

            // Bottom buttons
            filterResetButton.clicks {
                viewModel.resetFilter()
                updateSectionText() // Reset section selection
            }

            filterApplyButton.clicks {
                viewModel.applyFilter()
            }
        }
    }

    private fun showSectionDialog() {
        val sections = viewModel.listSections
        if (sections.isEmpty()) return
        
        val selectedIndex = viewModel.selectedSectionIndex
        val isAllAtTop = viewModel.isAllListPositionAtTop
        
        // Build list items
        val items = mutableListOf<ListItem<Int>>()
        
        // Calculate total entries
        var totalEntries = 0
        sections.forEach { totalEntries += it.entries.size }
        
        // Add "All" option
        val allExpectedIndex = if (isAllAtTop) 0 else sections.size
        
        if (isAllAtTop) {
            items.add(ListItem("All ($totalEntries)", allExpectedIndex, selectedIndex == allExpectedIndex))
        }
        
        // Add section options
        sections.forEachIndexed { index, group ->
            val expectedIndex = if (isAllAtTop) index + 1 else index
            items.add(ListItem("${group.name} (${group.entries.size})", expectedIndex, selectedIndex == expectedIndex))
        }
        
        if (!isAllAtTop) {
            items.add(ListItem("All ($totalEntries)", allExpectedIndex, selectedIndex == allExpectedIndex))
        }
        
        dialogManager?.showListDialog(items) { data, _ ->
            viewModel.updateSelectedSection(data)
            updateSectionText()
        }
    }
    
    private fun updateSectionText() {
        val sections = viewModel.listSections
        val selectedIndex = viewModel.selectedSectionIndex
        val isAllAtTop = viewModel.isAllListPositionAtTop
        
        // Calculate total entries
        var totalEntries = 0
        sections.forEach { totalEntries += it.entries.size }
        
        val allExpectedIndex = if (isAllAtTop) 0 else sections.size
        
        binding.filterSectionText.text = when {
            selectedIndex == allExpectedIndex -> "All ($totalEntries)"
            isAllAtTop && selectedIndex > 0 && selectedIndex <= sections.size -> {
                val group = sections[selectedIndex - 1]
                "${group.name} (${group.entries.size})"
            }
            !isAllAtTop && selectedIndex < sections.size -> {
                val group = sections[selectedIndex]
                "${group.name} (${group.entries.size})"
            }
            else -> "All ($totalEntries)"
        }
    }
    
    private fun updateOrderText(isDescending: Boolean) {
        binding.filterOrderByText.text = if (isDescending) {
            getString(R.string.descending_short)
        } else {
            getString(R.string.ascending_short)
        }
        // Rotate icon: down arrow for DESC, up arrow for ASC
        binding.filterOrderByIcon.rotation = if (isDescending) 0f else 180f
    }
    
    private fun setupSectionVisibility() {
        val sections = viewModel.listSections
        
        // Hide section card if no sections
        if (sections.isEmpty()) {
            binding.filterSectionTitle.visibility = android.view.View.GONE
            binding.filterSectionCard.visibility = android.view.View.GONE
        } else {
            binding.filterSectionTitle.visibility = android.view.View.VISIBLE
            binding.filterSectionCard.visibility = android.view.View.VISIBLE
            updateSectionText()
        }
    }

    override fun setUpObserver() {
        disposables.addAll(
            viewModel.sortBy.subscribe {
                binding.filterSortByText.text = it.getString(requireContext())
            },
            viewModel.orderByDescending.subscribe {
                updateOrderText(it)
            },
            viewModel.mediaFormatsText.subscribe {
                binding.filterFormatText.text = it
            },
            viewModel.mediaStatusesText.subscribe {
                binding.filterStatusText.text = it
            },
            viewModel.mediaSourcesText.subscribe {
                binding.filterSourceText.text = it
            },
            viewModel.countriesText.subscribe {
                binding.filterCountryText.text = it
            },
            viewModel.seasonsText.subscribe {
                binding.filterSeasonText.text = it
            },
            viewModel.yearsText.subscribe {
                binding.filterYearText.text = it
            },
            viewModel.includedGenresText.subscribe {
                binding.filterGenreIncludeText.text = it
            },
            viewModel.excludedGenresText.subscribe {
                binding.filterGenreExcludeText.text = it
            },
            viewModel.persistFilter.subscribe {
                binding.filterPersistCheckBox.isChecked = it
            },
            viewModel.seasonVisibility.subscribe {
                binding.filterSeasonCard.show(it)
                binding.filterSeasonSeparator.show(it)
            },
            // Dialog triggers
            viewModel.sortByList.subscribe { options ->
                dialogManager?.showListDialog(options) { data, _ ->
                    viewModel.updateSortBy(data)
                }
            },
            viewModel.mediaFormatList.subscribe { (list, selectedIndices) ->
                dialogManager?.showMultiSelectDialog(list, selectedIndices) { data ->
                    viewModel.updateMediaFormats(data)
                }
            },
            viewModel.mediaStatusList.subscribe { (list, selectedIndices) ->
                dialogManager?.showMultiSelectDialog(list, selectedIndices) { data ->
                    viewModel.updateMediaStatuses(data)
                }
            },
            viewModel.mediaSourceList.subscribe { (list, selectedIndices) ->
                dialogManager?.showMultiSelectDialog(list, selectedIndices) { data ->
                    viewModel.updateMediaSources(data)
                }
            },
            viewModel.countryList.subscribe { (list, selectedIndices) ->
                dialogManager?.showMultiSelectDialog(list, selectedIndices) { data ->
                    viewModel.updateCountries(data)
                }
            },
            viewModel.seasonList.subscribe { (list, selectedIndices) ->
                dialogManager?.showMultiSelectDialog(list, selectedIndices) { data ->
                    viewModel.updateSeasons(data)
                }
            },
            viewModel.genreList.subscribe { (list, selectedIndices) ->
                dialogManager?.showMultiSelectDialog(list, selectedIndices) { data ->
                    if (isLoadingIncludedGenres) {
                        viewModel.updateIncludedGenres(data)
                    } else {
                        viewModel.updateExcludedGenres(data)
                    }
                }
            },
            viewModel.filterResult.subscribe { (filter, sectionIndex) ->
                listener?.onFilterApplied(filter, sectionIndex)
                dismiss()
            }
        )

        // Load initial data from params
        params?.let { param ->
            viewModel.loadData(param)
            // Setup section visibility after loading data
            setupSectionVisibility()
        }
    }
    
    fun setParams(param: BottomSheetMediaFilterParam) {
        this.params = param
    }
    
    fun setListener(listener: MediaFilterListener) {
        this.listener = listener
    }

    companion object {
        fun newInstance(
            mediaFilter: MediaFilter,
            mediaType: MediaType,
            scoreFormat: ScoreFormat,
            isUserList: Boolean,
            hasBigList: Boolean,
            isViewer: Boolean,
            listSections: List<MediaListGroup>,
            selectedSectionIndex: Int,
            isAllListPositionAtTop: Boolean,
            listener: MediaFilterListener
        ): BottomSheetMediaFilterDialog {
            return BottomSheetMediaFilterDialog().apply {
                this.listener = listener
                this.params = BottomSheetMediaFilterParam(
                    mediaFilter = mediaFilter,
                    mediaType = mediaType,
                    scoreFormat = scoreFormat,
                    isUserList = isUserList,
                    hasBigList = hasBigList,
                    isViewer = isViewer,
                    listSections = listSections,
                    selectedSectionIndex = selectedSectionIndex,
                    isAllListPositionAtTop = isAllListPositionAtTop
                )
            }
        }
    }
}
