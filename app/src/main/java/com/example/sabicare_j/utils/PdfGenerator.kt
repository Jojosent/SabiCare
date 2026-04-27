package com.example.sabicare_j.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.ui.results.ResultCardState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object PdfGenerator {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun generate(
        context: Context,
        child: ChildEntity,
        cards: List<ResultCardState>,
        from: Long,
        to: Long
    ): File {
        val document = PdfDocument()
        val pageWidth = 595   // A4 width in points
        val pageHeight = 842  // A4 height in points

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // ─── Paints ──────────────────────────────────────────────────────────
        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
            color = android.graphics.Color.parseColor("#2A9D8F")
        }
        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = android.graphics.Color.parseColor("#1A1C1E")
        }
        val bodyPaint = Paint().apply {
            textSize = 12f
            color = android.graphics.Color.parseColor("#1A1C1E")
        }
        val subPaint = Paint().apply {
            textSize = 11f
            color = android.graphics.Color.parseColor("#6B7280")
        }
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E0E5E9")
            strokeWidth = 1f
        }
        val goodPaint = Paint().apply {
            textSize = 11f
            isFakeBoldText = true
            color = android.graphics.Color.parseColor("#4CAF50")
        }
        val warnPaint = Paint().apply {
            textSize = 11f
            isFakeBoldText = true
            color = android.graphics.Color.parseColor("#FFA726")
        }
        val dangerPaint = Paint().apply {
            textSize = 11f
            isFakeBoldText = true
            color = android.graphics.Color.parseColor("#EF5350")
        }

        var y = 60f
        val leftMargin = 48f
        val rightMargin = pageWidth - 48f

        // ─── Title ────────────────────────────────────────────────────────────
        canvas.drawText("SabiCare — Медициналық есеп", leftMargin, y, titlePaint)
        y += 30f

        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 20f

        // ─── Child info ───────────────────────────────────────────────────────
        val ageInDays = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - child.birthDate
        ).toInt()

        canvas.drawText("Бала: ${child.name}", leftMargin, y, headerPaint)
        y += 18f
        canvas.drawText(
            "Туылған күні: ${dateFormat.format(Date(child.birthDate))}   |   Жасы: $ageInDays күн",
            leftMargin, y, bodyPaint
        )
        y += 16f
        canvas.drawText(
            "Жынысы: ${if (child.gender == "MALE") "Ұл бала" else "Қыз бала"}   |   " +
                    "Есеп күні: ${dateFormat.format(Date())}",
            leftMargin, y, bodyPaint
        )
        y += 24f
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 20f

        // ─── Results per type ─────────────────────────────────────────────────
        cards.forEach { card ->
            if (y > pageHeight - 100) return@forEach // prevent overflow

            // Type header
            val icon = getIcon(card.type)
            canvas.drawText(
                "$icon  ${card.type.displayNameRu} / ${card.type.displayNameKz}",
                leftMargin, y, headerPaint
            )
            y += 16f

            // Value
            val valueText = if (card.latestValue != null)
                "Соңғы мән: ${formatValue(card.latestValue, card.type.unit)}"
            else "Деректер жоқ"
            canvas.drawText(valueText, leftMargin + 12f, y, bodyPaint)
            y += 14f

            // Norm
            card.norm?.let { norm ->
                canvas.drawText(
                    "Норма: ${formatValue(norm.min, "")} – ${formatValue(norm.max, card.type.unit)}   " +
                            "Орташа: ${formatValue(norm.average, card.type.unit)}",
                    leftMargin + 12f, y, subPaint
                )
                y += 14f
            }

            // Status
            val statusPaint = when (card.status) {
                GrowthStandards.ComplianceStatus.NORMAL -> goodPaint
                GrowthStandards.ComplianceStatus.BELOW -> warnPaint
                GrowthStandards.ComplianceStatus.ABOVE -> warnPaint
                GrowthStandards.ComplianceStatus.CRITICAL -> dangerPaint
            }
            val statusText = when (card.status) {
                GrowthStandards.ComplianceStatus.NORMAL -> "Норма ✓   ${card.compliancePercent}%"
                GrowthStandards.ComplianceStatus.BELOW -> "Нормадан төмен ⚠   ${card.compliancePercent}%"
                GrowthStandards.ComplianceStatus.ABOVE -> "Нормадан жоғары ⚠   ${card.compliancePercent}%"
                GrowthStandards.ComplianceStatus.CRITICAL -> "Назар аудар !   ${card.compliancePercent}%"
            }
            canvas.drawText(statusText, leftMargin + 12f, y, statusPaint)
            y += 20f

            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
            y += 16f
        }

        // ─── Footer ───────────────────────────────────────────────────────────
        val footerY = pageHeight - 36f
        canvas.drawText(
            "SabiCare қосымшасы арқылы жасалды • ${dateFormat.format(Date())}",
            leftMargin, footerY, subPaint
        )

        document.finishPage(page)

        // ─── Save to file ─────────────────────────────────────────────────────
        val fileName = "SabiCare_${child.name}_${dateFormat.format(Date())}.pdf"
        val outputDir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val outputFile = File(outputDir, fileName)

        document.writeTo(FileOutputStream(outputFile))
        document.close()

        return outputFile
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "PDF жіберу"))
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