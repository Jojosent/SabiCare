package com.example.sabicare_j.data.repository

import androidx.lifecycle.LiveData
import com.example.sabicare_j.data.local.dao.ChildDao
import com.example.sabicare_j.data.local.entities.ChildEntity

class ChildRepository(private val childDao: ChildDao) {

    fun getAllChildrenLive(): LiveData<List<ChildEntity>> = childDao.getAllChildrenLive()
    fun getActiveChildLive(): LiveData<ChildEntity?> = childDao.getActiveChildLive()

    suspend fun getAllChildren(): List<ChildEntity> = childDao.getAllChildren()
    suspend fun getActiveChild(): ChildEntity? = childDao.getActiveChild()
    suspend fun getChildById(id: Long): ChildEntity? = childDao.getChildById(id)
    suspend fun getChildCount(): Int = childDao.getChildCount()

    suspend fun addChild(child: ChildEntity): Long = childDao.insertChild(child)
    suspend fun updateChild(child: ChildEntity) = childDao.updateChild(child)
    suspend fun deleteChild(child: ChildEntity) = childDao.deleteChild(child)
    suspend fun switchActiveChild(childId: Long) = childDao.switchActiveChild(childId)
}