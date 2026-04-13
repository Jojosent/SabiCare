package com.example.sabicare_j.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.databinding.ItemReminderCardBinding

class ReminderAdapter(
    private val onReminderClick: (ReminderItem) -> Unit
) : ListAdapter<ReminderItem, ReminderAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val b: ItemReminderCardBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: ReminderItem) {
            val ctx = b.root.context

            b.tvIcon.text = getIcon(item.type)
            b.tvTypeName.text = item.type.displayNameRu
            b.tvLastValue.text = item.lastValueText

            when {
                item.isDue || item.daysUntilNext <= 0 -> {
                    b.tvStatusText.text = "Өлшеу уақыты келді!"
                    b.tvStatusText.setTextColor(
                        ContextCompat.getColor(ctx, R.color.warning)
                    )
                    b.viewIndicator.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.warning)
                    )
                    b.cardReminder.strokeColor =
                        ContextCompat.getColor(ctx, R.color.warning)
                    b.cardReminder.strokeWidth = 2
                }
                item.daysUntilNext == 1 -> {
                    b.tvStatusText.text = "Ертең өлшеу керек"
                    b.tvStatusText.setTextColor(
                        ContextCompat.getColor(ctx, R.color.secondary)
                    )
                    b.viewIndicator.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.secondary)
                    )
                    b.cardReminder.strokeColor =
                        ContextCompat.getColor(ctx, R.color.outline)
                    b.cardReminder.strokeWidth = 1
                }
                else -> {
                    b.tvStatusText.text = "${item.daysUntilNext} күннен кейін"
                    b.tvStatusText.setTextColor(
                        ContextCompat.getColor(ctx, R.color.success)
                    )
                    b.viewIndicator.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.success)
                    )
                    b.cardReminder.strokeColor =
                        ContextCompat.getColor(ctx, R.color.outline)
                    b.cardReminder.strokeWidth = 1
                }
            }

            b.root.setOnClickListener { onReminderClick(item) }
        }

        private fun getIcon(type: MeasurementType) = when (type) {
            MeasurementType.HEIGHT -> "📏"
            MeasurementType.WEIGHT -> "⚖️"
            MeasurementType.FEEDINGS_COUNT -> "🍼"
            MeasurementType.CALORIES -> "🔥"
            MeasurementType.SLEEP_DURATION -> "😴"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReminderItem>() {
        override fun areItemsTheSame(a: ReminderItem, b: ReminderItem) = a.type == b.type
        override fun areContentsTheSame(a: ReminderItem, b: ReminderItem) = a == b
    }
}