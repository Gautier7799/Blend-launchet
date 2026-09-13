package com.example.ui.screens.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiHelper
import com.example.model.Document
import com.example.model.Note
import com.example.repository.DocumentRepository
import com.example.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewerViewModel(
    private val documentRepository: DocumentRepository,
    private val noteRepository: NoteRepository,
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewerState())
    val uiState = _uiState.asStateFlow()

    // Mock fallback dictionary for seamless prototype preview
    private val sampleDocuments = mapOf(
        "doc-1" to Document(
            id = "doc-1",
            title = "تقرير الذكاء الاصطناعي 2026.txt",
            type = "txt",
            uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
            size = 14200L,
            tags = listOf("ذكاء_اصطناعي", "تقارير", "تقنية"),
            content = "الفصل الأول: مقدمة في النماذج التوليدية الحديثة.\nتشهد التكنولوجيا تطوراً متسارعاً مع دمج تقنيات Material You والذكاء الاصطناعي المتقدم. يقدم التطبيق واجهة ديناميكية تتكيف مع خلفية النظام وألوان المستخدم بأسلوب Google الحصري.\n\nالفصل الثاني: البنية السحابية وإدارة الملفات.\nتعتمد الحلول السحابية الحديثة على التخزين الفوري والمزامنة مع قواعد بيانات Firestore والتخزين السحابي لتسهيل العمل الجماعي ومشاركة الوثائق بسهولة عبر المنصات.\n\nالفصل الثالث: أدوات التحرير وتحليل المستندات.\nيتيح التطبيق للمستخدمين عزل الفصول، وإضافة تعليقات توضيحية، ومشاركة الروابط مع أعضاء الفريق فورياً."
        ),
        "doc-2" to Document(
            id = "doc-2",
            title = "دليل تصميم واجهات Material 3.pdf",
            type = "pdf",
            uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
            size = 2450000L,
            tags = listOf("تصميم", "Google", "Android"),
            content = "الفصل الأول: نظرة عامة على Material You.\nتركز فلسفة التصميم على التخصيص الشخصي (Personalization) باستخدام نظام Dynamic Color والأشكال الهندسية البيضاوية (Pills and Squircles) مع أزرار الإجراءات العائمة والتدرجات الهادئة التي تحاكي بيئة عمل Google Workspace.\n\nالفصل الثاني: مكونات واجهة المستخدم.\nتطبيق معايير M3 الدقيقة من حيث شريط البحث العائم (Floating Search Bar)، وأزرار التصفية (Filter Chips)، والبطاقات المنظمة بأسلوب Google Drive."
        ),
        "doc-3" to Document(
            id = "doc-3",
            title = "ملاحظات اجتماع الفريق التقني.txt",
            type = "txt",
            uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 48,
            size = 5400L,
            tags = listOf("ملاحظات", "فريق", "عمل"),
            content = "الفصل الأول: مراجعة الميزات المنجزة.\n1. مراجعة ميزات البحث الفوري في مكتبة المستندات.\n2. تحسين تجربة محرر النصوص مع شريط الأدوات المتقدم.\n\nالفصل الثاني: الخطوات القادمة.\n1. تفعيل التلخيص التلقائي بالذكاء الاصطناعي عبر Gemini.\n2. تكامل المشاركة السريعة عبر WhatsApp و Telegram."
        )
    )

    fun loadDocument(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            var doc = documentRepository.getDocumentById(id)
            if (doc == null) {
                doc = sampleDocuments[id]
            }

            val notes = noteRepository.getNotesForDocument(id)
            val chapters = parseChapters(doc?.content ?: "")

            _uiState.value = _uiState.value.copy(
                document = doc,
                notes = notes,
                chapters = chapters,
                selectedChapter = null,
                isLoading = false
            )
        }
    }

    private fun parseChapters(content: String): List<Chapter> {
        val lines = content.split("\n\n")
        val chapters = mutableListOf<Chapter>()
        var currentTitle = "مقدمة عامة"
        var currentBody = StringBuilder()

        for (section in lines) {
            if (section.startsWith("الفصل") || section.startsWith("المبحث") || section.startsWith("#")) {
                if (currentBody.isNotBlank()) {
                    chapters.add(Chapter(currentTitle, currentBody.toString().trim()))
                    currentBody = StringBuilder()
                }
                val split = section.split("\n", limit = 2)
                currentTitle = split.getOrNull(0) ?: "فصل"
                currentBody.append(split.getOrNull(1) ?: "")
            } else {
                currentBody.append(section).append("\n\n")
            }
        }
        if (currentBody.isNotBlank()) {
            chapters.add(Chapter(currentTitle, currentBody.toString().trim()))
        }
        return chapters
    }

    fun selectChapter(chapter: Chapter?) {
        _uiState.value = _uiState.value.copy(selectedChapter = chapter)
    }

    fun summarizeCurrent() {
        val targetText = _uiState.value.selectedChapter?.content
            ?: _uiState.value.document?.content
            ?: return

        _uiState.value = _uiState.value.copy(isAiProcessing = true)
        viewModelScope.launch {
            val summary = geminiHelper.summarize(targetText)
            _uiState.value = _uiState.value.copy(
                isAiProcessing = false,
                aiResultTitle = "ملخص Gemini للوثيقة ✦",
                aiResultContent = summary,
                showAiResultDialog = true
            )
        }
    }

    fun extractKeyTakeaways() {
        val targetText = _uiState.value.selectedChapter?.content
            ?: _uiState.value.document?.content
            ?: return

        _uiState.value = _uiState.value.copy(isAiProcessing = true)
        viewModelScope.launch {
            val prompt = "قم باستخراج أهم 3-5 نقاط رئيسية ومعلومات جوهرية من هذا النص في نقاط نقطية واضحة:\n\n$targetText"
            val takeaways = geminiHelper.summarize(prompt)
            _uiState.value = _uiState.value.copy(
                isAiProcessing = false,
                aiResultTitle = "أهم النقاط المستخرجة ✦",
                aiResultContent = takeaways,
                showAiResultDialog = true
            )
        }
    }

    fun dismissAiDialog() {
        _uiState.value = _uiState.value.copy(showAiResultDialog = false)
    }

    fun addNote(noteContent: String, pageOrChapter: String = "") {
        val docId = _uiState.value.document?.id ?: return
        viewModelScope.launch {
            val note = Note(documentId = docId, content = noteContent, pageOrChapter = pageOrChapter)
            val result = noteRepository.saveNote(note)
            if (result.isSuccess) {
                val updatedNotes = noteRepository.getNotesForDocument(docId)
                _uiState.value = _uiState.value.copy(notes = updatedNotes)
            } else {
                // local fallback if offline
                _uiState.value = _uiState.value.copy(notes = _uiState.value.notes + note)
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
            _uiState.value = _uiState.value.copy(notes = _uiState.value.notes.filter { it.id != noteId })
        }
    }

    companion object {
        fun provideFactory(
            documentRepository: DocumentRepository,
            noteRepository: NoteRepository,
            geminiHelper: GeminiHelper
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ViewerViewModel(documentRepository, noteRepository, geminiHelper) as T
            }
        }
    }
}
