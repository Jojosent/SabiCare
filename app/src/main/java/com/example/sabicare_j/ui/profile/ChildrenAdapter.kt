package com.example.sabicare_j.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.databinding.ItemChildCardBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ChildrenAdapter(
    private val onChildClick: (ChildEntity) -> Unit,
    private val onEditClick: (ChildEntity) -> Unit,
    private val onDeleteClick: (ChildEntity) -> Unit
) : ListAdapter<ChildEntity, ChildrenAdapter.ChildViewHolder>(DiffCallback()) {

    private var activeChildId: Long = -1L

    fun setActiveChildId(id: Long) {
        activeChildId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = ItemChildCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(getItem(position), activeChildId)
    }

    inner class ChildViewHolder(private val binding: ItemChildCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(child: ChildEntity, activeId: Long) {
            binding.tvChildName.text = child.name
            binding.tvChildAge.text = getAgeString(child.birthDate)
            binding.tvGender.text = if (child.gender == "MALE") "👦 Ұл" else "👧 Қыз"

            // Highlight active child
            val isActive = child.id == activeId
            binding.cardChild.strokeWidth = if (isActive) 3 else 0
            binding.ivActiveIndicator.visibility =
                if (isActive) android.view.View.VISIBLE else android.view.View.GONE

            binding.root.setOnClickListener { onChildClick(child) }
            binding.btnEdit.setOnClickListener { onEditClick(child) }
            binding.btnDelete.setOnClickListener { onDeleteClick(child) }
        }

        private fun getAgeString(birthDateMillis: Long): String {
            val now = System.currentTimeMillis()
            val diffMs = now - birthDateMillis
            val days = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
            return when {
                days < 30 -> "$days күн"
                days < 365 -> "${days / 30} ай"
                else -> "${days / 365} жас"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChildEntity>() {
        override fun areItemsTheSame(a: ChildEntity, b: ChildEntity) = a.id == b.id
        override fun areContentsTheSame(a: ChildEntity, b: ChildEntity) = a == b
    }
}