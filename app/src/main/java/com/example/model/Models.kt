package com.example.launcher.data.model

data class AppItem(
    val id: String,
    val name: String,
    val iconRes: Int,
    val packageName: String = ""
)

data class FolderItem(
    val title: String,
    val apps: List<AppItem>
)
