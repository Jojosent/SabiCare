package com.example.sabicare_j.data.repository

import androidx.lifecycle.LiveData
import com.example.sabicare_j.data.local.dao.MeasurementDao
import com.example.sabicare_j.data.local.entities.MeasurementEntity
import com.example.sabicare_j.data.local.entities.MeasurementType

class MeasurementRepository(private val measurementDao: MeasurementDao) {

    fun getAllForChildLive(childId: Long): LiveData<List<MeasurementEntity>> =
        measurementDao.getAllForChildLive(childId)

    fun getMeasurementsByTypeLive(childId: Long, type: MeasurementType): LiveData<List<MeasurementEntity>> =
        measurementDao.getMeasurementsByTypeLive(childId, type.name)

    fun getLatestByTypeLive(childId: Long, type: MeasurementType): LiveData<MeasurementEntity?> =
        measurementDao.getLatestByTypeLive(childId, type.name)

    suspend fun getAllForChild(childId: Long): List<MeasurementEntity> =
        measurementDao.getAllForChild(childId)

    suspend fun getMeasurementsByType(childId: Long, type: MeasurementType): List<MeasurementEntity> =
        measurementDao.getMeasurementsByType(childId, type.name)

    suspend fun getLatestByType(childId: Long, type: MeasurementType): MeasurementEntity? =
        measurementDao.getLatestByType(childId, type.name)

    suspend fun getMeasurementsInRange(childId: Long, from: Long, to: Long): List<MeasurementEntity> =
        measurementDao.getMeasurementsInRange(childId, from, to)

    suspend fun addMeasurement(measurement: MeasurementEntity): Long =
        measurementDao.insertMeasurement(measurement)

    suspend fun updateMeasurement(measurement: MeasurementEntity) =
        measurementDao.updateMeasurement(measurement)

    suspend fun deleteMeasurement(measurement: MeasurementEntity) =
        measurementDao.deleteMeasurement(measurement)

    suspend fun isDue(childId: Long, type: MeasurementType): Boolean {
        val latest = measurementDao.getLatestByType(childId, type.name) ?: return true
        val nextDue = latest.recordedAt + (type.intervalDays * 24L * 60 * 60 * 1000)
        return System.currentTimeMillis() >= nextDue
    }

    suspend fun daysUntilNext(childId: Long, type: MeasurementType): Int {
        val latest = measurementDao.getLatestByType(childId, type.name) ?: return 0
        val nextDue = latest.recordedAt + (type.intervalDays * 24L * 60 * 60 * 1000)
        val diff = nextDue - System.currentTimeMillis()
        return (diff / (24L * 60 * 60 * 1000)).toInt()
    }

    // Clears all local measurements on logout / user switch
    suspend fun clearLocalData() {
        measurementDao.deleteAllMeasurements()
    }
}