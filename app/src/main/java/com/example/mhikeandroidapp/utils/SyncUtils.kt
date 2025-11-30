package com.example.mhikeandroidapp.utils

import android.net.Uri
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.data.observation.ObservationModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

object SyncUtils {

    private fun HikeModel.toMap(imageUrl: String?): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "location" to location,
        "dateMs" to dateMs,
        "parking" to parking,
        "plannedLengthKm" to plannedLengthKm,
        "difficulty" to difficulty,
        "description" to description,
        "estimatedDurationMinutes" to estimatedDurationMinutes,
        "groupSize" to groupSize,
        "latitude" to latitude,
        "longitude" to longitude,
        "imageUri" to imageUrl,
        "reminderMs" to reminderMs,
        "createdAtMs" to createdAtMs,
        "updatedAtMs" to System.currentTimeMillis()
    )

    private fun ObservationModel.toMap(imageUrl: String?): Map<String, Any?> = mapOf(
        "id" to id,
        "hikeId" to hikeId,
        "observationText" to observationText,
        "timeMs" to timeMs,
        "comments" to comments,
        "imageObservationUri" to imageUrl
    )

    private suspend fun ensureUploaded(urlOrPath: String?): String? {
        if (urlOrPath.isNullOrBlank()) return null
        if (urlOrPath.startsWith("http")) return urlOrPath // đã là URL
        return uploadImageToFirebase(urlOrPath) // local path
    }

    suspend fun syncHikeToCloud(hike: HikeModel, observations: List<ObservationModel>) {
        require(hike.id != 0L) { "Hike chưa có id hợp lệ (autoGenerate). Insert trước khi sync." }

        val db = FirebaseFirestore.getInstance()

        val hikeImageUrl = ensureUploaded(hike.imageUri)
        val hikeData = hike.toMap(hikeImageUrl)

        db.collection("hikes")
            .document(hike.id.toString())
            .set(hikeData)
            .await()

        for (obs in observations) {
            val obsImageUrl = ensureUploaded(obs.imageObservationUri)
            val obsData = obs.toMap(obsImageUrl)

            db.collection("hikes")
                .document(hike.id.toString())
                .collection("observations")
                .document(obs.id.toString())
                .set(obsData)
                .await()
        }
    }

    suspend fun uploadImageToFirebase(localPath: String): String? {
        val file = File(localPath)
        val uri = Uri.fromFile(file)
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${file.name}")
        return try {
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
