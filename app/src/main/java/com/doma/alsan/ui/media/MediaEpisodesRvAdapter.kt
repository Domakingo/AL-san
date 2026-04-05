package com.doma.alsan.ui.media

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.doma.alsan.R
import com.doma.alsan.ui.base.BaseRecyclerViewAdapter
import com.doma.alsan.helper.extensions.*
import com.doma.alsan.data.response.Episode
import androidx.viewbinding.ViewBinding
import com.doma.alsan.databinding.ListEpisodeBinding
import com.doma.alsan.databinding.ListEpisodeMoreBinding
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Typeface
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.util.TypedValue
import android.view.View

class MediaEpisodesRvAdapter(
    private val context: Context,
    list: List<Episode>,
    private val hasMore: Boolean = false,
    private var currentProgress: Int? = null,
    private var currentPage: Int = 1,
    private var totalPages: Int = 1,
    private val showJump: Boolean = true,
    private val listener: MediaEpisodesListener
) : BaseRecyclerViewAdapter<Episode, ViewBinding>(list) {
    private var recyclerView: RecyclerView? = null

    companion object {
        private const val VIEW_TYPE_EPISODE = 0
        private const val VIEW_TYPE_MORE = 1
        private const val VIEW_TYPE_PAGINATION = 2
        private const val VIEW_TYPE_JUMP = 3
    }

    interface MediaEpisodesListener {
        fun onEpisodeClick(episode: Episode)
        fun onEpisodeLongClick(episode: Episode)
        fun onShowMoreClick() {}
        fun onPageClick(page: Int) {}
        fun onJumpToProgressClick(page: Int, episodeNumber: Int) {}
    }

    override fun getItemCount(): Int {
        var count = list.size
        if (hasMore) count++
        if (totalPages > 1) count += 2 // Top and Bottom
        if (showJump && currentProgress != null && currentProgress!! > 0) count++ // Jump Button
        return count
    }

    override fun getItemViewType(position: Int): Int {
        val listSize = list.size
        val hasPagination = totalPages > 1
        val hasJump = showJump && currentProgress != null && currentProgress!! > 0
        
        var currentPos = position
        
        if (hasJump) {
            if (currentPos == 0) return VIEW_TYPE_JUMP
            currentPos--
        }
        
        if (hasPagination) {
            if (currentPos == 0) return VIEW_TYPE_PAGINATION
            currentPos--
        }
        
        if (currentPos < listSize) return VIEW_TYPE_EPISODE
        currentPos -= listSize
        
        if (hasMore) {
            if (currentPos == 0) return VIEW_TYPE_MORE
            currentPos--
        }
        
        if (hasPagination) {
            if (currentPos == 0) return VIEW_TYPE_PAGINATION
        }
        
        return VIEW_TYPE_EPISODE
    }

    fun updateEpisodes(newList: List<Episode>, page: Int, total: Int) {
        this.list = newList
        this.currentPage = page
        this.totalPages = total
        notifyDataSetChanged()
    }

    fun setCurrentProgress(progress: Int) {
        this.currentProgress = progress
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    fun scrollToEpisode(episodeNumber: Int) {
        val index = list.indexOfFirst { ep: Episode -> ep.number == episodeNumber }
        if (index != -1) {
            val hasJump = showJump && currentProgress != null && currentProgress!! > 0
            val hasPagination = totalPages > 1
            val offset = (if (hasJump) 1 else 0) + (if (hasPagination) 1 else 0)
            recyclerView?.scrollToPosition(index + offset)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewAdapter<Episode, ViewBinding>.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_MORE -> {
                val view = com.doma.alsan.databinding.ListEpisodeMoreBinding.inflate(inflater, parent, false)
                MoreViewHolder(view)
            }
            VIEW_TYPE_PAGINATION -> {
                val view = HorizontalScrollView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    setPadding(16, 16, 16, 16)
                }
                PaginationViewHolder(view)
            }
            VIEW_TYPE_JUMP -> {
                val view = TextView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    val padding = 32
                    setPadding(padding, padding, padding, padding)
                    gravity = Gravity.CENTER
                    textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                }
                JumpViewHolder(view)
            }
            else -> {
                val view = ListEpisodeBinding.inflate(inflater, parent, false)
                ItemViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter<Episode, ViewBinding>.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when (viewType) {
            VIEW_TYPE_EPISODE -> {
                val hasJump = showJump && currentProgress != null && currentProgress!! > 0
                val hasPagination = totalPages > 1
                val offset = (if (hasJump) 1 else 0) + (if (hasPagination) 1 else 0)
                holder.bind(list[position - offset], position - offset)
            }
            VIEW_TYPE_MORE -> (holder as MoreViewHolder).bindInternal()
            VIEW_TYPE_PAGINATION -> (holder as PaginationViewHolder).bindInternal()
            VIEW_TYPE_JUMP -> (holder as JumpViewHolder).bindInternal()
        }
    }

    inner class ItemViewHolder(private val binding: ListEpisodeBinding) : BaseRecyclerViewAdapter<Episode, ViewBinding>.ViewHolder(binding) {
        override fun bind(item: Episode, index: Int) {
            binding.episodeNumberText.text = item.number.toString()
            val title = if (item.title.isNullOrBlank()) {
                if (item.titleRomanji.isNullOrBlank()) item.titleJapanese else item.titleRomanji
            } else item.title
            binding.episodeTitleText.text = title

            when {
                item.filler -> {
                    binding.episodeBadgeText.show(true)
                    binding.episodeBadgeText.text = "Filler"
                }
                item.recap -> {
                    binding.episodeBadgeText.show(true)
                    binding.episodeBadgeText.text = "Recap"
                }
                else -> {
                    binding.episodeBadgeText.show(false)
                }
            }

            val isCurrent = currentProgress != null && item.number == currentProgress
            if (isCurrent) {
                val secondaryTV = android.util.TypedValue()
                context.theme.resolveAttribute(com.doma.alsan.R.attr.themeSecondaryColor, secondaryTV, true)
                val secondaryColor = secondaryTV.data

                val backgroundTV = android.util.TypedValue()
                context.theme.resolveAttribute(com.doma.alsan.R.attr.themeBackgroundColor, backgroundTV, true)
                val backgroundColor = backgroundTV.data

                binding.root.setBackgroundColor(secondaryColor)
                binding.episodeNumberText.setTextColor(backgroundColor)
                binding.episodeTitleText.setTextColor(backgroundColor)
                binding.episodeBadgeText.setTextColor(backgroundColor)
            } else {
                binding.root.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                val secondaryTV = android.util.TypedValue()
                context.theme.resolveAttribute(com.doma.alsan.R.attr.themeSecondaryColor, secondaryTV, true)
                val secondaryColor = secondaryTV.data

                val contentTV = android.util.TypedValue()
                context.theme.resolveAttribute(com.doma.alsan.R.attr.themeContentColor, contentTV, true)
                val contentColor = contentTV.data

                binding.episodeNumberText.setTextColor(secondaryColor)
                binding.episodeTitleText.setTextColor(contentColor)
                binding.episodeBadgeText.setTextColor(secondaryColor)
            }

            binding.root.setOnClickListener {
                listener.onEpisodeClick(item)
            }

            binding.root.setOnLongClickListener {
                listener.onEpisodeLongClick(item)
                true
            }
        }
    }

    inner class MoreViewHolder(private val binding: ListEpisodeMoreBinding) : BaseRecyclerViewAdapter<Episode, ViewBinding>.ViewHolder(binding) {
        override fun bind(item: Episode, index: Int) {
            bindInternal()
        }
    
        fun bindInternal() {
            binding.root.setOnClickListener {
                listener.onShowMoreClick()
            }
        }
    }

    inner class JumpViewHolder(private val textView: TextView) : BaseRecyclerViewAdapter<Episode, ViewBinding>.ViewHolder(object : ViewBinding {
        override fun getRoot(): View = textView
    }) {
        override fun bind(item: Episode, index: Int) {
            bindInternal()
        }

        fun bindInternal() {
            val progress = currentProgress ?: return
            val targetPage = (progress - 1) / 100 + 1
            
            textView.text = if (totalPages > 1) {
                "Jump to Episode $progress (Page $targetPage)"
            } else {
                "Jump to Episode $progress"
            }

            val secondaryTV = android.util.TypedValue()
            context.theme.resolveAttribute(com.doma.alsan.R.attr.themeSecondaryColor, secondaryTV, true)
            val secondaryColor = secondaryTV.data

            val secondaryTransparent20TV = android.util.TypedValue()
            context.theme.resolveAttribute(com.doma.alsan.R.attr.themeSecondaryTransparent20Color, secondaryTransparent20TV, true)
            val secondaryTransparent20Color = secondaryTransparent20TV.data

            textView.setTextColor(secondaryColor)
            textView.setBackgroundColor(secondaryTransparent20Color)

            textView.setOnClickListener {
                listener.onJumpToProgressClick(targetPage, progress)
            }
        }
    }

    inner class PaginationViewHolder(private val scrollView: HorizontalScrollView) : BaseRecyclerViewAdapter<Episode, ViewBinding>.ViewHolder(object : ViewBinding {
        override fun getRoot(): android.view.View = scrollView
    }) {
        override fun bind(item: Episode, index: Int) {
            bindInternal()
        }

        fun bindInternal() {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            scrollView.removeAllViews()
            scrollView.addView(layout)

            val secondaryTV = android.util.TypedValue()
            context.theme.resolveAttribute(com.doma.alsan.R.attr.themeSecondaryColor, secondaryTV, true)
            val secondaryColor = secondaryTV.data

            val contentTV = android.util.TypedValue()
            context.theme.resolveAttribute(com.doma.alsan.R.attr.themeContentColor, contentTV, true)
            val contentColor = contentTV.data

            val secondaryTransparent20TV = android.util.TypedValue()
            context.theme.resolveAttribute(com.doma.alsan.R.attr.themeSecondaryTransparent20Color, secondaryTransparent20TV, true)
            val secondaryTransparent20Color = secondaryTransparent20TV.data

            for (i in 1..totalPages) {
                val button = TextView(context).apply {
                    text = i.toString()
                    val padding = 24
                    setPadding(padding, padding, padding, padding)
                    textSize = 16f
                    gravity = Gravity.CENTER
                    
                    if (i == currentPage) {
                        setTextColor(android.graphics.Color.WHITE)
                        setBackgroundColor(secondaryColor)
                        setTypeface(null, Typeface.BOLD)
                    } else {
                        setTextColor(contentColor)
                        setBackgroundColor(secondaryTransparent20Color)
                    }

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(8, 8, 8, 8)
                    }

                    setOnClickListener {
                        listener.onPageClick(i)
                    }
                }
                layout.addView(button)
            }
        }
    }
}
