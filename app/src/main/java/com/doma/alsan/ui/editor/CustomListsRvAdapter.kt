package com.doma.alsan.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.databinding.ListCustomListsBinding
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class CustomListsRvAdapter(
    items: List<Pair<String, Boolean>>,
    private val readOnly: Boolean,
    private val listener: CustomListsListener
) : BaseRecyclerViewAdapter<Pair<String, Boolean>, ListCustomListsBinding>(items) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListCustomListsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListCustomListsBinding) : ViewHolder(binding) {
        override fun bind(item: Pair<String, Boolean>, index: Int) {
            binding.customListsName.text = item.first
            binding.customListsCheckBox.isEnabled = !readOnly
            binding.customListsCheckBox.isChecked = item.second
            binding.customListsCheckBox.setOnClickListener {
                listener.getNewCustomList(item.first to binding.customListsCheckBox.isChecked)
            }
        }
    }

    interface CustomListsListener {
        fun getNewCustomList(newCustomList: Pair<String, Boolean>)
    }
}