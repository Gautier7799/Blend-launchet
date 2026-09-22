package com.gautier7799.blend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gautier7799.blend.ui.AppLibraryScreen // استيراد الشاشة الجديدة
import com.gautier7799.blend.ui.theme.BlendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlendTheme {
                // استدعي واجهة مكتبة التطبيقات الجديدة هنا بدلاً من الواجهة القديمة
                AppLibraryScreen(
                    categories = getSampleCategories(), // أو البيانات القادمة من نظام التطبيقات لديك
                    onFolderClick = { category ->
                        // إجراء عند فتح المجلد
                    }
                )
            }
        }
    }
}
