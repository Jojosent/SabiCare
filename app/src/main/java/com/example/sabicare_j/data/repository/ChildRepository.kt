package com.example.sabicare_j.data.repository

import androidx.lifecycle.LiveData
import com.example.sabicare_j.data.local.dao.ChildDao
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.data.remote.FirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChildRepository(
    private val childDao: ChildDao,
    private val firestoreRepo: FirestoreRepository = FirestoreRepository()
) {

    fun getAllChildrenLive(): LiveData<List<ChildEntity>> = childDao.getAllChildrenLive()
    fun getActiveChildLive(): LiveData<ChildEntity?> = childDao.getActiveChildLive()

    suspend fun getAllChildren(): List<ChildEntity> = childDao.getAllChildren()
    suspend fun getActiveChild(): ChildEntity? = childDao.getActiveChild()
    suspend fun getChildById(id: Long): ChildEntity? = childDao.getChildById(id)
    suspend fun getChildCount(): Int = childDao.getChildCount()

    suspend fun addChild(child: ChildEntity): Long {
        val id = childDao.insertChild(child)
        // Firebase-ке сақтау (фоңда, қате болса Room-да сақталады)
        CoroutineScope(Dispatchers.IO).launch {
            try { firestoreRepo.saveChild(child.copy(id = id)) } catch (_: Exception) {}
        }
        return id
    }

    suspend fun updateChild(child: ChildEntity) {
        childDao.updateChild(child)
        CoroutineScope(Dispatchers.IO).launch {
            try { firestoreRepo.saveChild(child) } catch (_: Exception) {}
        }
    }

    suspend fun deleteChild(child: ChildEntity) {
        childDao.deleteChild(child)
        CoroutineScope(Dispatchers.IO).launch {
            try { firestoreRepo.deleteChild(child.id) } catch (_: Exception) {}
        }
    }

    suspend fun switchActiveChild(childId: Long) = childDao.switchActiveChild(childId)
}