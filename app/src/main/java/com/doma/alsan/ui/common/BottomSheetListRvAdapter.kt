package com.doma.alsan.ui.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.doma.alsan.R
import com.doma.alsan.databinding.ListTextBinding
import com.doma.alsan.databinding.ListTextCardBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.pojo.ListItem

class BottomSheetListRvAdapter<T>(
    private val context: Context,
    private var list: List<ListItem<T>>,
    private val listener: BottomSheetListListener<T>
) : RecyclerView.Adapter<BottomSheetListRvAdapter<T>.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_CARD = 1
    }

    override fun getItemViewType(position: Int): Int {
        // Use card layout for items that explicitly request it
        val item = list[position]
        return if (item.useCardLayout) VIEW_TYPE_CARD else VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CARD -> {
                val binding = ListTextCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CardViewHolder(binding)
            }
            else -> {
                val binding = ListTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NormalViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ListItem<T>>, alwaysRefresh: Boolean = false) {
        if (!alwaysRefresh && this.list == newList) return
        this.list = newList
        notifyDataSetChanged()
    }

    abstract inner class ViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: ListItem<T>, index: Int)
    }

    inner class NormalViewHolder(private val binding: ListTextBinding) : ViewHolder(binding) {
        override fun bind(item: ListItem<T>, index: Int) {
            var convertedText = item.text
            item.stringResources.forEachIndexed { counter, stringResource ->
                convertedText = convertedText.replace("{$counter}", context.getString(stringResource))
            }
            binding.itemText.text = convertedText
            
            // Apply theme color for highlighted items (selected sections)
            if (item.isHighlighted) {
                val typedValue = android.util.TypedValue()
                context.theme.resolveAttribute(R.attr.themePrimaryColor, typedValue, true)
                binding.itemText.setTextColor(typedValue.data)
            } else {
                val typedValue = android.util.TypedValue()
                context.theme.resolveAttribute(R.attr.themeContentColor, typedValue, true)
                binding.itemText.setTextColor(typedValue.data)
            }
            
            binding.itemLayout.clicks { listener.getSelectedItem(item.data, index) }
        }
    }

    inner class CardViewHolder(private val binding: ListTextCardBinding) : ViewHolder(binding) {
        override fun bind(item: ListItem<T>, index: Int) {
            var convertedText = item.text
            item.stringResources.forEachIndexed { counter, stringResource ->
                convertedText = convertedText.replace("{$counter}", context.getString(stringResource))
            }
            binding.itemText.text = convertedText
            binding.itemLayout.clicks { listener.getSelectedItem(item.data, index) }
        }
    }

    interface BottomSheetListListener<T> {
        fun getSelectedItem(data: T, index: Int)
    }
}