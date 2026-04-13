package com.example.sabicare_j.ui.tracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sabicare_j.R
import com.example.sabicare_j.databinding.ItemMeasurementTypeBinding
import java.text.SimpleDateFormat
import java.util.*

class MeasurementAdapter(
    private val onCardClick: (MeasurementCardState) -> Unit
) : ListAdapter<MeasurementCardState, MeasurementAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeasurementTypeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val b: ItemMeasurementTypeBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(state: MeasurementCardState) {
            val ctx = b.root.context

            // Icon + name
            b.tvTypeName.text = state.type.displayNameRu
            b.tvTypeNameKz.text = state.type.displayNameKz
            b.tvTypeIcon.text = getTypeIcon(state.type)

            // Last value
            if (state.latestValue != null && state.latestDateMillis != null) {
                b.tvLastValue.text = formatValue(state.latestValue, state.type.unit)
                b.tvLastDate.text = "Соңғы: ${dateFormat.format(Date(state.latestDateMillis))}"
            } else {
                b.tvLastValue.text = "—"
                b.tvLastDate.text = "Деректер жоқ"
            }

            // Status badge
            when {
                state.isDue -> {
                    b.tvStatus.text = "Өлшеу уақыты келді"
                    b.tvStatus.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.warning)
                    )
                    b.tvStatus.setTextColor(Color.WHITE)
                    b.cardMeasurement.strokeColor =
                        ContextCompat.getColor(ctx, R.color.warning)
                    b.cardMeasurement.strokeWidth = 2
                }
                state.daysUntilNext < 0 -> {
                    b.tvStatus.text = "${-state.daysUntilNext} күн өтіп кетті"
                    b.tvStatus.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.error)
                    )
                    b.tvStatus.setTextColor(Color.WHITE)
                    b.cardMeasurement.strokeColor =
                        ContextCompat.getColor(ctx, R.color.error)
                    b.cardMeasurement.strokeWidth = 2
                }
                else -> {
                    b.tvStatus.text = "${state.daysUntilNext} күннен кейін"
                    b.tvStatus.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.success)
                    )
                    b.tvStatus.setTextColor(Color.WHITE)
                    b.cardMeasurement.strokeColor =
                        ContextCompat.getColor(ctx, R.color.outline)
                    b.cardMeasurement.strokeWidth = 1
                }
            }

            b.root.setOnClickListener { onCardClick(state) }
        }

        private fun getTypeIcon(type: com.example.sabicare_j.data.local.entities.MeasurementType): String {
            return when (type) {
                com.example.sabicare_j.data.local.entities.MeasurementType.HEIGHT -> "📏"
                com.example.sabicare_j.data.local.entities.MeasurementType.WEIGHT -> "⚖️"
                com.example.sabicare_j.data.local.entities.MeasurementType.FEEDINGS_COUNT -> "🍼"
                com.example.sabicare_j.data.local.entities.MeasurementType.CALORIES -> "🔥"
                com.example.sabicare_j.data.local.entities.MeasurementType.SLEEP_DURATION -> "😴"
            }
        }

        private fun formatValue(value: Double, unit: String): String {
            return if (value == value.toLong().toDouble()) {
                "${value.toLong()} $unit"
            } else {
                "${"%.1f".format(value)} $unit"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MeasurementCardState>() {
        override fun areItemsTheSame(a: MeasurementCardState, b: MeasurementCardState) =
            a.type == b.type
        override fun areContentsTheSame(a: MeasurementCardState, b: MeasurementCardState) =
            a == b
    }
}