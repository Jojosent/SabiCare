package com.example.sabicare_j.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sabicare_j.data.local.entities.MeasurementEntity

@Dao
interface MeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long

    @Update
    suspend fun updateMeasurement(measurement: MeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: MeasurementEntity)

    @Query("DELETE FROM measurements WHERE child_id = :childId")
    suspend fun deleteAllForChild(childId: Long)

    @Query("DELETE FROM measurements")
    suspend fun deleteAllMeasurements()

    @Query("SELECT * FROM measurements WHERE child_id = :childId ORDER BY recorded_at DESC")
    fun getAllForChildLive(childId: Long): LiveData<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE child_id = :childId ORDER BY recorded_at DESC")
    suspend fun getAllForChild(childId: Long): List<MeasurementEntity>

    @Query("SELECT * FROM measurements WHERE child_id = :childId AND type = :type ORDER BY recorded_at ASC")
    fun getMeasurementsByTypeLive(childId: Long, type: String): LiveData<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE child_id = :childId AND type = :type ORDER BY recorded_at ASC")
    suspend fun getMeasurementsByType(childId: Long, type: String): List<MeasurementEntity>

    @Query("SELECT * FROM measurements WHERE child_id = :childId AND type = :type ORDER BY recorded_at DESC LIMIT 1")
    suspend fun getLatestByType(childId: Long, type: String): MeasurementEntity?

    @Query("SELECT * FROM measurements WHERE child_id = :childId AND type = :type ORDER BY recorded_at DESC LIMIT 1")
    fun getLatestByTypeLive(childId: Long, type: String): LiveData<MeasurementEntity?>

    @Query("SELECT * FROM measurements WHERE child_id = :childId AND recorded_at BETWEEN :fromMillis AND :toMillis ORDER BY recorded_at ASC")
    suspend fun getMeasurementsInRange(childId: Long, fromMillis: Long, toMillis: Long): List<MeasurementEntity>

    @Query("SELECT COUNT(*) FROM measurements WHERE child_id = :childId AND type = :type")
    suspend fun countByType(childId: Long, type: String): Int
}