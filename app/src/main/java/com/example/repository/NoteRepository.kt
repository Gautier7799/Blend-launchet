package com.example.repository

import com.example.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NoteRepository(private val firestore: FirebaseFirestore) {
    private val collection = firestore.collection("notes")

    suspend fun getNotesForDocument(documentId: String): List<Note> {
        return try {
            val snapshot = collection.whereEqualTo("documentId", documentId).get().await()
            snapshot.toObjects(Note::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveNote(note: Note): Result<Note> {
        return try {
            val docRef = if (note.id.isEmpty()) collection.document() else collection.document(note.id)
            val newNote = note.copy(id = docRef.id)
            docRef.set(newNote).await()
            Result.success(newNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(id: String): Result<Unit> {
        return try {
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
