package com.example.sabicare_j.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sabicare_j.databinding.ItemMedicationCardBinding

class MedicationAdapter : ListAdapter<MedicationItem, MedicationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicationCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val b: ItemMedicationCardBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: MedicationItem) {
            b.tvEmoji.text = item.emoji
            b.tvName.text = item.name
            b.tvDescription.text = item.description
            b.tvAgeRange.text = item.ageRange
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MedicationItem>() {
        override fun areItemsTheSame(a: MedicationItem, b: MedicationItem) = a.name == b.name
        override fun areContentsTheSame(a: MedicationItem, b: MedicationItem) = a == b
    }
}