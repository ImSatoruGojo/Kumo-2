package app.kumo.beta.data.local

import android.content.Context
import android.content.SharedPreferences

enum class DownloadStatus {
    QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED
}

data class DownloadItem(
    val id: String,
    val mediaId: String,
    val title: String,
    val episodeTitle: String,
    val coverUrl: String,
    val quality: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: DownloadStatus,
    val speed: String = "0 KB/s",
    val eta: String = "--"
)

class DownloadManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kumo_downloads", Context.MODE_PRIVATE)

    fun getDownloads(): List<DownloadItem> {
        val jsonStr = prefs.getString("custom_dls", null)
        if (jsonStr.isNullOrEmpty()) return emptyList()

        val list = mutableListOf<DownloadItem>()
        try {
            val array = org.json.JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DownloadItem(
                        id = obj.getString("id"),
                        mediaId = obj.getString("mediaId"),
                        title = obj.getString("title"),
                        episodeTitle = obj.getString("episodeTitle"),
                        coverUrl = obj.optString("coverUrl", ""),
                        quality = obj.optString("quality", "1080p"),
                        totalBytes = obj.optLong("totalBytes", 0L),
                        downloadedBytes = obj.optLong("downloadedBytes", 0L),
                        status = try { DownloadStatus.valueOf(obj.getString("status")) } catch (e: Exception) { DownloadStatus.QUEUED },
                        speed = obj.optString("speed", "0 KB/s"),
                        eta = obj.optString("eta", "--")
                    )
                )
            }
        } catch (e: Exception) {
            return emptyList()
        }
        return list
    }

    fun pauseDownload(id: String) {
        val current = getDownloads().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            current[idx] = current[idx].copy(status = DownloadStatus.PAUSED, speed = "0 KB/s", eta = "Paused")
            saveDownloads(current)
        }
    }

    fun resumeDownload(id: String) {
        val current = getDownloads().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            current[idx] = current[idx].copy(status = DownloadStatus.DOWNLOADING, speed = "2.8 MB/s", eta = "Downloading")
            saveDownloads(current)
        }
    }

    fun deleteDownload(id: String) {
        val current = getDownloads().toMutableList()
        current.removeAll { it.id == id }
        saveDownloads(current)
    }

    private fun saveDownloads(list: List<DownloadItem>) {
        val array = org.json.JSONArray()
        list.forEach { dl ->
            val obj = org.json.JSONObject().apply {
                put("id", dl.id)
                put("mediaId", dl.mediaId)
                put("title", dl.title)
                put("episodeTitle", dl.episodeTitle)
                put("coverUrl", dl.coverUrl)
                put("quality", dl.quality)
                put("totalBytes", dl.totalBytes)
                put("downloadedBytes", dl.downloadedBytes)
                put("status", dl.status.name)
                put("speed", dl.speed)
                put("eta", dl.eta)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_dls", array.toString()).apply()
    }
}
