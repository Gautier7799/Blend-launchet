package com.example.ui.screens.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.GoogleDocsBlue
import com.example.ui.theme.GoogleGeminiPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()

    var showAiDialog by remember { mutableStateOf(false) }
    var aiCustomPrompt by remember { mutableStateOf("") }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = title,
                        onValueChange = viewModel::onTitleChange,
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = GoogleDocsBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::undo, enabled = canUndo) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "تراجع",
                            tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    IconButton(onClick = viewModel::redo, enabled = canRedo) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "إعادة",
                            tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    IconButton(onClick = { showAiDialog = true }) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "مساعد Gemini",
                            tint = GoogleGeminiPurple
                        )
                    }
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = viewModel::saveDocument) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "حفظ",
                                tint = GoogleDocsBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Google Docs Bottom Formatting Toolbar
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { viewModel.appendFormatting("**", "**") }) {
                        Icon(Icons.Default.FormatBold, contentDescription = "غامق")
                    }
                    IconButton(onClick = { viewModel.appendFormatting("*", "*") }) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "مائل")
                    }
                    IconButton(onClick = { viewModel.appendFormatting("__", "__") }) {
                        Icon(Icons.Default.FormatUnderlined, contentDescription = "تسطير")
                    }
                    IconButton(onClick = { viewModel.appendFormatting("• ") }) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "قائمة نقطية")
                    }
                    IconButton(onClick = { viewModel.appendFormatting("# ") }) {
                        Icon(Icons.Default.Title, contentDescription = "عنوان رئيسي")
                    }
                    IconButton(onClick = { viewModel.appendFormatting("## ") }) {
                        Icon(Icons.Default.FormatQuote, contentDescription = "اقتباس / عنوان فرعي")
                    }

                    VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

                    // Gemini Quick Assist Button
                    FilledTonalButton(
                        onClick = { showAiDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = GoogleGeminiPurple.copy(alpha = 0.15f),
                            contentColor = GoogleGeminiPurple
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مساعدة في الكتابة", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            // Google Docs Elevated Paper Canvas
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = viewModel::onContentChange,
                        placeholder = {
                            Text(
                                "ابدأ بكتابة مستندك هنا...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )

                    if (isAiGenerating) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Gemini يقوم بتوليد النص...",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Gemini AI Assistance Dialog
    if (showAiDialog) {
        AlertDialog(
            onDismissRequest = { showAiDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoogleGeminiPurple
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مساعد Google Gemini الذكي")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "اختر مساعدة سريعة أو اكتب طلباً مخصصاً:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(
                            onClick = {
                                viewModel.aiAssist("أكمل كتابة هذا المستند بأسلوب احترافي")
                                showAiDialog = false
                            },
                            label = { Text("إكمال النص") }
                        )
                        SuggestionChip(
                            onClick = {
                                viewModel.aiAssist("أعد صياغة وتحسين لغة هذا النص")
                                showAiDialog = false
                            },
                            label = { Text("تحسين الصياغة") }
                        )
                    }

                    OutlinedTextField(
                        value = aiCustomPrompt,
                        onValueChange = { aiCustomPrompt = it },
                        placeholder = { Text("طلب مخصص لـ Gemini...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (aiCustomPrompt.isNotBlank()) {
                            viewModel.aiAssist(aiCustomPrompt)
                            aiCustomPrompt = ""
                        }
                        showAiDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleGeminiPurple)
                ) {
                    Text("تطبيق")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
