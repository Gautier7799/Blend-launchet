package com.example.ui.screens.viewer

import com.example.model.Document
import com.example.model.Note

data class Chapter(
    val title: String,
    val content: String
)

data class ViewerState(
    val document: Document? = null,
    val notes: List<Note> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val selectedChapter: Chapter? = null,
    val isLoading: Boolean = false,
    val isAiProcessing: Boolean = false,
    val aiResultTitle: String = "ملخص Gemini الذكي",
    val aiResultContent: String = "",
    val showAiResultDialog: Boolean = false,
    val isNotesDrawerOpen: Boolean = false
)
