package com.example.sabicare_j.ui.shared

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.data.local.entities.ChildEntity
import kotlinx.coroutines.launch

class ChildViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as SabiCareApplication).childRepository

    val activeChild: LiveData<ChildEntity?> = repo.getActiveChildLive()
    val allChildren: LiveData<List<ChildEntity>> = repo.getAllChildrenLive()

    fun addChild(child: ChildEntity) = viewModelScope.launch {
        val newId = repo.addChild(child)
        if (repo.getChildCount() == 1) {
            repo.switchActiveChild(newId)
        }
    }

    fun updateChild(child: ChildEntity) = viewModelScope.launch {
        repo.updateChild(child)
    }

    fun deleteChild(child: ChildEntity) = viewModelScope.launch {
        repo.deleteChild(child)
        if (child.isActive) {
            repo.getAllChildren().firstOrNull()?.let {
                repo.switchActiveChild(it.id)
            }
        }
    }

    fun switchActiveChild(childId: Long) = viewModelScope.launch {
        repo.switchActiveChild(childId)
    }

    suspend fun getChildCount(): Int = repo.getChildCount()
}