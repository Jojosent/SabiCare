package com.example.sabicare_j.data.remote

import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.data.local.entities.MeasurementEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun uid() = auth.currentUser?.uid ?: throw Exception("Пайдаланушы кірмеген")

    // ── CHILDREN ──────────────────────────────────────────────

    suspend fun saveChild(child: ChildEntity) {
        val data = hashMapOf(
            "id" to child.id,
            "name" to child.name,
            "birthDate" to child.birthDate,
            "gender" to child.gender,
            "photoUri" to child.photoUri,
            "isActive" to child.isActive,
            "createdAt" to child.createdAt
        )
        db.collection("users").document(uid())
            .collection("children").document(child.id.toString())
            .set(data).await()
    }

    suspend fun deleteChild(childId: Long) {
        db.collection("users").document(uid())
            .collection("children").document(childId.toString())
            .delete().await()
    }

    // ── MEASUREMENTS ──────────────────────────────────────────

    suspend fun saveMeasurement(m: MeasurementEntity) {
        val data = hashMapOf(
            "id" to m.id,
            "childId" to m.childId,
            "type" to m.type,
            "value" to m.value,
            "note" to m.note,
            "recordedAt" to m.recordedAt
        )
        db.collection("users").document(uid())
            .collection("measurements").document(m.id.toString())
            .set(data).await()
    }

    suspend fun deleteMeasurement(measurementId: Long) {
        db.collection("users").document(uid())
            .collection("measurements").document(measurementId.toString())
            .delete().await()
    }

    // ── SYNC: Firebase → Room ──────────────────────────────────
    // Жаңа құрылғыда немесе қайта кіргенде деректерді жүктеу
    suspend fun fetchChildren(): List<Map<String, Any>> {
        val snapshot = db.collection("users").document(uid())
            .collection("children").get().await()
        return snapshot.documents.mapNotNull { it.data }
    }

    suspend fun fetchMeasurements(): List<Map<String, Any>> {
        val snapshot = db.collection("users").document(uid())
            .collection("measurements").get().await()
        return snapshot.documents.mapNotNull { it.data }
    }
}
