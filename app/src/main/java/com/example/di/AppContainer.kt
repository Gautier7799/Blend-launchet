package com.example.di

import com.example.ai.GeminiHelper
import com.example.repository.AuthRepository
import com.example.repository.DocumentRepository
import com.example.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AppContainer {
    val auth by lazy { FirebaseAuth.getInstance() }
    val firestore by lazy { FirebaseFirestore.getInstance() }
    val storage by lazy { FirebaseStorage.getInstance() }

    val authRepository by lazy { AuthRepository(auth) }
    val documentRepository by lazy { DocumentRepository(firestore, storage) }
    val noteRepository by lazy { NoteRepository(firestore) }
    val geminiHelper by lazy { GeminiHelper() }
}
