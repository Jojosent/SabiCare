package com.example.sabicare_j.ui.results

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.databinding.ItemResultCardBinding
import com.example.sabicare_j.utils.GrowthStandards
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ResultsAdapter : ListAdapter<ResultCardState, ResultsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResultCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val b: ItemResultCardBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(state: ResultCardState) {
            val ctx = b.root.context

            // Icon + Title
            b.tvIcon.text = getIcon(state.type)
            b.tvTypeName.text = state.type.displayNameRu
            b.tvTypeNameKz.text = state.type.displayNameKz

            // Current value
            if (state.latestValue != null) {
                b.tvCurrentValue.text = formatValue(state.latestValue, state.type.unit)
            } else {
                b.tvCurrentValue.text = "Деректер жоқ"
            }

            // Norm range
            state.norm?.let { norm ->
                b.tvNormRange.text = "Норма: ${formatValue(norm.min, "")} – ${formatValue(norm.max, state.type.unit)}"
            }

            // Progress bar
            b.progressBar.progress = state.compliancePercent
            val progressColor = when (state.status) {
                GrowthStandards.ComplianceStatus.NORMAL -> ContextCompat.getColor(ctx, R.color.progress_good)
                GrowthStandards.ComplianceStatus.BELOW -> ContextCompat.getColor(ctx, R.color.progress_warning)
                GrowthStandards.ComplianceStatus.ABOVE -> ContextCompat.getColor(ctx, R.color.progress_warning)
                GrowthStandards.ComplianceStatus.CRITICAL -> ContextCompat.getColor(ctx, R.color.progress_danger)
            }
            b.progressBar.setIndicatorColor(progressColor)

            // Status badge
            val (statusText, statusColor) = when (state.status) {
                GrowthStandards.ComplianceStatus.NORMAL ->
                    "✅ Норма" to ContextCompat.getColor(ctx, R.color.success)
                GrowthStandards.ComplianceStatus.BELOW ->
                    "⚠️ Төмен" to ContextCompat.getColor(ctx, R.color.warning)
                GrowthStandards.ComplianceStatus.ABOVE ->
                    "⚠️ Жоғары" to ContextCompat.getColor(ctx, R.color.warning)
                GrowthStandards.ComplianceStatus.CRITICAL ->
                    "🔴 Назар аудар" to ContextCompat.getColor(ctx, R.color.error)
            }
            b.tvStatus.text = statusText
            b.tvStatus.setTextColor(statusColor)

            // Progress percent label
            b.tvPercent.text = "${state.compliancePercent}%"

            // Line chart
            setupChart(state)
        }

        private fun setupChart(state: ResultCardState) {
            if (state.history.size < 2) {
                b.lineChart.clear()
                b.lineChart.setNoDataText("График үшін деректер жеткіліксіз")
                b.lineChart.setNoDataTextColor(
                    ContextCompat.getColor(b.root.context, R.color.on_surface_variant)
                )
                return
            }

            val entries = state.history.mapIndexed { index, measurement ->
                Entry(index.toFloat(), measurement.value.toFloat())
            }

            val dataSet = LineDataSet(entries, state.type.displayNameRu).apply {
                color = getChartColor(state.type)
                setCircleColor(getChartColor(state.type))
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawValues(false)
                setDrawFilled(true)
                fillAlpha = 30
                fillColor = getChartColor(state.type)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            b.lineChart.apply {
                data = LineData(dataSet)
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.textColor =
                    ContextCompat.getColor(context, R.color.on_surface_variant)
                xAxis.isEnabled = false
                setTouchEnabled(false)
                animateX(500)
                invalidate()
            }
        }

        private fun getChartColor(type: MeasurementType): Int {
            val ctx = b.root.context
            return when (type) {
                MeasurementType.HEIGHT ->
                    ContextCompat.getColor(ctx, R.color.chart_height)
                MeasurementType.WEIGHT ->
                    ContextCompat.getColor(ctx, R.color.chart_weight)
                MeasurementType.FEEDINGS_COUNT ->
                    ContextCompat.getColor(ctx, R.color.chart_feedings)
                MeasurementType.CALORIES ->
                    ContextCompat.getColor(ctx, R.color.chart_calories)
                MeasurementType.SLEEP_DURATION ->
                    ContextCompat.getColor(ctx, R.color.chart_sleep)
            }
        }

        private fun getIcon(type: MeasurementType) = when (type) {
            MeasurementType.HEIGHT -> "📏"
            MeasurementType.WEIGHT -> "⚖️"
            MeasurementType.FEEDINGS_COUNT -> "🍼"
            MeasurementType.CALORIES -> "🔥"
            MeasurementType.SLEEP_DURATION -> "😴"
        }

        private fun formatValue(value: Double, unit: String): String {
            return if (value == value.toLong().toDouble())
                "${value.toLong()} $unit".trim()
            else "${"%.1f".format(value)} $unit".trim()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ResultCardState>() {
        override fun areItemsTheSame(a: ResultCardState, b: ResultCardState) =
            a.type == b.type
        override fun areContentsTheSame(a: ResultCardState, b: ResultCardState) =
            a == b
    }
}