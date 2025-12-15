package com.doma.alsan.ui.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.R
import com.doma.alsan.databinding.ListPlainTextBinding
import com.doma.alsan.helper.extensions.clicks
import com.doma.alsan.helper.extensions.getAttrValue
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter

class TextRvAdapter(
    private val context: Context,
    list: List<String>,
    private val listener: TextListener? = null
) : BaseRecyclerViewAdapter<String, ListPlainTextBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListPlainTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListPlainTextBinding) : ViewHolder(binding) {
        override fun bind(item: String, index: Int) {
            binding.apply {
                itemText.text = item
                if (listener == null) {
                    itemText.setTextColor(context.getAttrValue(R.attr.themeContentColor))
                    itemText.isEnabled = false
                } else {
                    itemText.setTextColor(context.getAttrValue(R.attr.themePrimaryColor))
                    itemText.isEnabled = true
                }
                itemText.clicks {
                    listener?.getText(item)
                }
            }
        }
    }

    interface TextListener {
        fun getText(text: String) {}
        fun getSelectedIndex(index: Int) {}
    }
}