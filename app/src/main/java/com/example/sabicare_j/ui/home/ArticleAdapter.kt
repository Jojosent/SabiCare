package com.example.sabicare_j.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sabicare_j.databinding.ItemArticleCardBinding

class ArticleAdapter : ListAdapter<ArticleItem, ArticleAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemArticleCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val b: ItemArticleCardBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: ArticleItem) {
            b.tvEmoji.text = item.emoji
            b.tvTitle.text = item.title
            b.tvSubtitle.text = item.subtitle
            b.tvCategory.text = item.category
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ArticleItem>() {
        override fun areItemsTheSame(a: ArticleItem, b: ArticleItem) = a.title == b.title
        override fun areContentsTheSame(a: ArticleItem, b: ArticleItem) = a == b
    }
}