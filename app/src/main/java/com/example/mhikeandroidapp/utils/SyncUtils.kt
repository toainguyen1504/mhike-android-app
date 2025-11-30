package com.example.mhikeandroidapp.utils

import android.net.Uri
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.data.observation.ObservationModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

object SyncUtils {

    suspend fun syncHikeToCloud(hike: HikeModel, observations: List<ObservationModel>) {
        val db = FirebaseFirestore.getInstance()

        // Upload hike image if exists
        val hikeImageUrl = hike.imageUri?.let { uploadImageToFirebase(it) }

        val hikeData = hashMapOf(
            "name" to hike.name,
            "location" to hike.location,
            "dateMs" to hike.dateMs,
            "parking" to hike.parking,
            "plannedLengthKm" to hike.plannedLengthKm,
            "difficulty" to hike.difficulty,
            "description" to hike.description,
            "estimatedDurationMinutes" to hike.estimatedDurationMinutes,
            "groupSize" to hike.groupSize,
            "latitude" to hike.latitude,
            "longitude" to hike.longitude,
            "imageUri" to hikeImageUrl,
            "reminderMs" to hike.reminderMs,
            "createdAtMs" to hike.createdAtMs,
            "updatedAtMs" to hike.updatedAtMs
        )

        db.collection("hikes").document(hike.id.toString()).set(hikeData).await()

        for (obs in observations) {
            val obsImageUrl = obs.imageObservationUri?.let { uploadImageToFirebase(it) }

            val obsData = hashMapOf(
                "observationText" to obs.observationText,
                "timeMs" to obs.timeMs,
                "comments" to obs.comments,
                "imageObservationUri" to obsImageUrl
            )

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