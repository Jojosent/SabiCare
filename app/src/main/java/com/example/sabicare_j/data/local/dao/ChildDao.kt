package com.example.sabicare_j.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sabicare_j.data.local.entities.ChildEntity

@Dao
interface ChildDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity): Long

    @Update
    suspend fun updateChild(child: ChildEntity)

    @Delete
    suspend fun deleteChild(child: ChildEntity)

    @Query("DELETE FROM children")
    suspend fun deleteAllChildren()

    @Query("UPDATE children SET is_active = 0")
    suspend fun deactivateAllChildren()

    @Query("UPDATE children SET is_active = 1 WHERE id = :childId")
    suspend fun setActiveChild(childId: Long)

    @Transaction
    suspend fun switchActiveChild(childId: Long) {
        deactivateAllChildren()
        setActiveChild(childId)
    }

    @Query("SELECT * FROM children ORDER BY created_at ASC")
    fun getAllChildrenLive(): LiveData<List<ChildEntity>>

    @Query("SELECT * FROM children ORDER BY created_at ASC")
    suspend fun getAllChildren(): List<ChildEntity>

    @Query("SELECT * FROM children WHERE is_active = 1 LIMIT 1")
    fun getActiveChildLive(): LiveData<ChildEntity?>

    @Query("SELECT * FROM children WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveChild(): ChildEntity?

    @Query("SELECT * FROM children WHERE id = :childId LIMIT 1")
    suspend fun getChildById(childId: Long): ChildEntity?

    @Query("SELECT COUNT(*) FROM children")
    suspend fun getChildCount(): Int
}