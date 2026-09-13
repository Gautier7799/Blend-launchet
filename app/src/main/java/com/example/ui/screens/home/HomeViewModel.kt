package com.example.ui.screens.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.model.Document
import com.example.repository.AuthRepository
import com.example.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DocumentFilter {
    ALL, TXT, PDF, RECENT
}

class HomeViewModel(
    private val repository: DocumentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(DocumentFilter.ALL)
    private val _isGridView = MutableStateFlow(true)
    private val _isLoading = MutableStateFlow(false)

    val searchQuery = _searchQuery.asStateFlow()
    val selectedFilter = _selectedFilter.asStateFlow()
    val isGridView = _isGridView.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    val currentUserEmail: String
        get() = authRepository.currentUser?.email ?: "user@google.com"

    val uiState = combine(_documents, _searchQuery, _selectedFilter) { docs, query, filter ->
        var list = docs
        if (filter == DocumentFilter.TXT) {
            list = list.filter { it.type.equals("txt", ignoreCase = true) }
        } else if (filter == DocumentFilter.PDF) {
            list = list.filter { it.type.equals("pdf", ignoreCase = true) }
        } else if (filter == DocumentFilter.RECENT) {
            list = list.sortedByDescending { it.uploadDate }
        }

        if (query.isNotBlank()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        _isLoading.value = true
        viewModelScope.launch {
            val remoteDocs = repository.getDocuments()
            if (remoteDocs.isEmpty()) {
                // If cloud is empty or unconfigured, provide starter sample documents for instant Google Drive experience
                _documents.value = listOf(
                    Document(
                        id = "doc-1",
                        title = "تقرير الذكاء الاصطناعي 2026.txt",
                        type = "txt",
                        uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                        size = 14200L,
                        tags = listOf("ذكاء_اصطناعي", "تقارير", "تقنية"),
                        content = "الفصل الأول: مقدمة في النماذج التوليدية الحديثة.\nتشهد التكنولوجيا تطوراً متسارعاً مع دمج تقنيات Material You والذكاء الاصطناعي المتقدم. يقدم التطبيق واجهة ديناميكية تتكيف مع خلفية النظام وألوان المستخدم بأسلوب Google الحصري.\n\nالفصل الثاني: البنية السحابية وإدارة الملفات.\nتعتمد الحلول السحابية الحديثة على التخزين الفوري والمزامنة مع قواعد بيانات Firestore والتخزين السحابي لتسهيل العمل الجماعي ومشاركة الوثائق بسهولة عبر المنصات."
                    ),
                    Document(
                        id = "doc-2",
                        title = "دليل تصميم واجهات Material 3.pdf",
                        type = "pdf",
                        uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                        size = 2450000L,
                        tags = listOf("تصميم", "Google", "Android"),
                        content = "دليل إرشادات Material Design 3 و Material You.\nتركز فلسفة التصميم على التخصيص الشخصي (Personalization) باستخدام نظام Dynamic Color والأشكال الهندسية البيضاوية (Pills and Squircles) مع أزرار الإجراءات العائمة والتدرجات الهادئة التي تحاكي بيئة عمل Google Workspace."
                    ),
                    Document(
                        id = "doc-3",
                        title = "ملاحظات اجتماع الفريق التقني.txt",
                        type = "txt",
                        uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 48,
                        size = 5400L,
                        tags = listOf("ملاحظات", "فريق", "عمل"),
                        content = "أجندة الاجتماع:\n1. مراجعة ميزات البحث الفوري في مكتبة المستندات.\n2. تحسين تجربة محرر النصوص مع شريط الأدوات المتقدم.\n3. تفعيل التلخيص التلقائي بالذكاء الاصطناعي عبر Gemini."
                    )
                )
            } else {
                _documents.value = remoteDocs
            }
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: DocumentFilter) {
        _selectedFilter.value = filter
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun uploadFile(context: Context, uri: Uri, forcedType: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.uploadFileFromUri(context, uri, forcedType)
            if (result.isSuccess) {
                val newDoc = result.getOrNull()
                if (newDoc != null) {
                    _documents.value = listOf(newDoc) + _documents.value
                }
            }
            _isLoading.value = false
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
            _documents.value = _documents.value.filter { it.id != id }
        }
    }

    fun logout() {
        authRepository.signOut()
    }

    companion object {
        fun provideFactory(
            repository: DocumentRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository, authRepository) as T
                }
            }
    }
}
