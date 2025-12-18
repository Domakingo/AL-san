package com.doma.alsan.ui.settings.app

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.doma.alsan.databinding.ListAppThemeGroupBinding
import com.doma.alsan.helper.enums.AppTheme
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.pojo.AppThemeItem
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class AppThemeRvAdapter(
    private val context: Context,
    list: List<AppThemeItem>,
    private val listener: AppThemeListener?
) : BaseRecyclerViewAdapter<AppThemeItem, ViewBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListAppThemeGroupBinding.inflate(inflater, parent, false)
        return ThemeGroupViewHolder(binding)
    }

    inner class ThemeGroupViewHolder(private val binding: ListAppThemeGroupBinding) : ViewHolder(binding) {
        override fun bind(item: AppThemeItem, index: Int) {
            val darkTheme = item.darkTheme ?: return
            val lightTheme = item.lightTheme ?: return
            val themeName = item.themeName ?: return
            
            binding.apply {
                themeNameText.text = themeName
                
                // Dark theme colors
                darkPrimaryColor.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, darkTheme.colors.first)
                )
                darkSecondaryColor.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, darkTheme.colors.second)
                )
                
                // Light theme colors
                lightPrimaryColor.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, lightTheme.colors.first)
                )
                lightSecondaryColor.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, lightTheme.colors.second)
                )
                
                // Click listeners
                darkThemeLayout.clicks {
                    listener?.getSelectedAppTheme(darkTheme)
                }
                
                lightThemeLayout.clicks {
                    listener?.getSelectedAppTheme(lightTheme)
                }
            }
        }
    }

    interface AppThemeListener {
        fun getSelectedAppTheme(appTheme: AppTheme)
    }
}