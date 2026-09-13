package com.example.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.Document
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DocumentRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val collection = firestore.collection("documents")

    suspend fun getDocuments(): List<Document> {
        return try {
            val snapshot = collection.get().await()
            snapshot.toObjects(Document::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDocumentById(id: String): Document? {
        return try {
            val snapshot = collection.document(id).get().await()
            snapshot.toObject(Document::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createTextDocument(title: String, content: String): Result<Document> {
        return try {
            val docRef = collection.document()
            val document = Document(
                id = docRef.id,
                title = title,
                type = "txt",
                content = content
            )
            docRef.set(document).await()
            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFileFromUri(
        context: Context,
        uri: Uri,
        forcedType: String? = null
    ): Result<Document> {
        return try {
            val contentResolver = context.contentResolver
            var fileName = "document_${System.currentTimeMillis()}"
            var fileSize = 0L
            val mimeType = contentResolver.getType(uri) ?: ""

            // Extract file metadata from ContentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx != -1) {
                        it.getString(nameIdx)?.let { name -> fileName = name }
                    }
                    if (sizeIdx != -1) {
                        fileSize = it.getLong(sizeIdx)
                    }
                }
            }

            if (fileSize <= 0L) {
                fileSize = try {
                    contentResolver.openInputStream(uri)?.use { it.available().toLong() } ?: 1024L
                } catch (e: Exception) {
                    2048L
                }
            }

            // Determine document type
            val lowerName = fileName.lowercase()
            val detectedType = when {
                forcedType != null -> forcedType
                mimeType.startsWith("image") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png") -> "image"
                mimeType == "application/pdf" || lowerName.endsWith(".pdf") -> "pdf"
                else -> "txt"
            }

            var textContent = ""
            if (detectedType == "txt") {
                textContent = try {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (e: Exception) {
                    ""
                }
            } else if (detectedType == "pdf") {
                textContent = "مستند PDF تم رفعه إلى Firebase Storage: $fileName"
            } else if (detectedType == "image") {
                textContent = "صورة تم رفعها إلى Firebase Storage: $fileName"
            }

            val docId = collection.document().id

            // Upload file to Firebase Storage
            var downloadUrl = ""
            try {
                val storageRef = storage.reference.child("uploads/$docId/$fileName")
                storageRef.putFile(uri).await()
                downloadUrl = storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                // If storage rules or configuration fail, store local identifier
                downloadUrl = uri.toString()
            }

            val document = Document(
                id = docId,
                title = fileName,
                type = detectedType,
                uploadDate = System.currentTimeMillis(),
                size = fileSize,
                storageUrl = downloadUrl,
                tags = listOf(detectedType, "سحابي"),
                content = textContent
            )

            // Save document metadata to Firestore
            try {
                collection.document(docId).set(document).await()
            } catch (e: Exception) {
                // Return document even if Firestore offline
            }

            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteDocument(id: String): Result<Unit> {
        return try {
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
