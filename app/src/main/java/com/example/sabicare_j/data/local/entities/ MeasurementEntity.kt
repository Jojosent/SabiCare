package com.example.sabicare_j.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MeasurementType(
    val displayNameRu: String,
    val displayNameKz: String,
    val unit: String,
    val intervalDays: Int
) {
    HEIGHT("Рост", "Бой", "см", 14),
    WEIGHT("Вес", "Салмақ", "г", 7),
    FEEDINGS_COUNT("Кормлений в день", "Емізу саны", "раз", 1),
    CALORIES("Калории", "Калория", "ккал", 1),
    SLEEP_DURATION("Сон", "Ұйқы", "мин", 1)
}

@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["child_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["child_id"])]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "child_id")
    val childId: Long,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "value")
    val value: Double,

    @ColumnInfo(name = "recorded_at")
    val recordedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "note")
    val note: String? = null
)