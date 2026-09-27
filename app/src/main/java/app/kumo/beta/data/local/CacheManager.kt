package app.kumo.beta.data.local

import android.content.Context
import java.io.File

class CacheManager(context: Context) {
    private val cacheDir = context.applicationContext.cacheDir

    fun sizeBytes(): Long =
        cacheDir.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }

    fun clear(): Long = cacheDir.walkTopDown().filter { it.isFile }.map { file -> val size = file.length(); if (file.delete()) size else 0L }.sum()

    fun trimToLimit(limitMb: Int): Long {
        val limit = limitMb.coerceAtLeast(32) * 1024L * 1024L
        var current = sizeBytes()
        if (current <= limit) return 0L

        var deleted = 0L
        cacheDir.walkTopDown()
            .filter { it.isFile }
            .sortedBy { it.lastModified() }
            .forEach { file ->
                if (current <= limit) return@forEach
                val size = file.length()
                if (file.delete()) {
                    current -= size
                    deleted += size
                }
            }
        return deleted
    }
}
