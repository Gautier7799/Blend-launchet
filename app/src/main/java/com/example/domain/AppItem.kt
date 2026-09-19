package com.example.domain

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap

data class AppCategoryInfo(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val iconEmoji: String,
    val sortOrder: Int
)

data class AppItem(
    val label: String,
    val packageName: String,
    val icon: Drawable? = null,
    val iconBitmap: ImageBitmap? = null,
    val category: String = "other"
)
