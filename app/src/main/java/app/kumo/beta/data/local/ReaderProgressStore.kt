package app.kumo.beta.data.local

import android.content.Context

class ReaderProgressStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kumo_reader_progress", Context.MODE_PRIVATE)

    fun getPage(chapterId: String): Int = prefs.getInt("page_$chapterId", 0).coerceAtLeast(0)

    fun savePage(chapterId: String, page: Int) {
        prefs.edit().putInt("page_$chapterId", page.coerceAtLeast(0)).apply()
    }

    fun clearChapter(chapterId: String) {
        prefs.edit().remove("page_$chapterId").apply()
    }
}
