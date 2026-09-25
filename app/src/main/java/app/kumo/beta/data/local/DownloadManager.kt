package app.kumo.beta.data.local

import android.content.Context
import android.net.Uri
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

enum class DownloadStatus { QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED }

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
    val eta: String = "--",
    val fileUri: String? = null,
    val error: String? = null
)

class DownloadManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("kumo_downloads", Context.MODE_PRIVATE)
    private val storage = StorageLocationManager(appContext)
    private val settings = SettingsPreferencesStore(appContext)
    private val downloadSlots = java.util.concurrent.Semaphore(settings.get().maxConcurrentDownloads.coerceIn(1, 4), true)

    fun getDownloads(): List<DownloadItem> {
        val raw = prefs.getString("custom_dls", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    add(DownloadItem(
                        id = o.getString("id"),
                        mediaId = o.getString("mediaId"),
                        title = o.getString("title"),
                        episodeTitle = o.getString("episodeTitle"),
                        coverUrl = o.optString("coverUrl", ""),
                        quality = o.optString("quality", "1080p"),
                        totalBytes = o.optLong("totalBytes"),
                        downloadedBytes = o.optLong("downloadedBytes"),
                        status = runCatching { DownloadStatus.valueOf(o.getString("status")) }.getOrDefault(DownloadStatus.QUEUED),
                        speed = o.optString("speed", "0 KB/s"),
                        eta = o.optString("eta", "--"),
                        fileUri = o.optString("fileUri").takeIf { it.isNotBlank() },
                        error = o.optString("error").takeIf { it.isNotBlank() }
                    ))
                }
            }
        }.getOrDefault(emptyList())
    }

    suspend fun downloadDirect(
        mediaId: String,
        title: String,
        episodeTitle: String,
        coverUrl: String,
        quality: String,
        sourceUrl: String
    ): Result<DownloadItem> = withContext(Dispatchers.IO) {
        var currentId: String? = null
        runCatching {
            require(storage.hasValidLocation()) { "Choose a download folder in Settings first" }
            require(isNetworkAllowed()) { "Downloads are restricted to Wi Fi while Wi Fi only is enabled" }
            require(sourceUrl.startsWith("http://") || sourceUrl.startsWith("https://")) { "Invalid download URL" }
            require(!sourceUrl.contains(".m3u8", ignoreCase = true)) { "This HLS source needs segmented download support" }
            require(downloadSlots.tryAcquire()) { "Download queue is full; try again when an active download finishes" }

            val item = DownloadItem(
                id = UUID.randomUUID().toString(),
                mediaId = mediaId,
                title = title,
                episodeTitle = episodeTitle,
                coverUrl = coverUrl,
                quality = quality,
                totalBytes = 0L,
                downloadedBytes = 0L,
                status = DownloadStatus.DOWNLOADING
            )
            currentId = item.id
            upsert(item)

            val connection = URL(sourceUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Kumo/0.1")
            require(connection.responseCode in 200..299) { "Download returned HTTP " + connection.responseCode }

            val total = connection.contentLengthLong.coerceAtLeast(0L)
            val tree = storage.getTreeUri() ?: error("Download folder is no longer available")
            val name = sanitize(title + " - " + episodeTitle) + extensionFor(sourceUrl)
            val fileUri = DocumentsContract.createDocument(appContext.contentResolver, tree, mimeFor(name), name)
                ?: error("Unable to create the download file")

            var downloaded = 0L
            try {
                connection.inputStream.use { input ->
                    appContext.contentResolver.openOutputStream(fileUri, "w")?.use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val count = input.read(buffer)
                            if (count <= 0) break
                            output.write(buffer, 0, count)
                            downloaded += count
                            upsert(item.copy(totalBytes = total, downloadedBytes = downloaded, status = DownloadStatus.DOWNLOADING))
                        }
                    } ?: error("Unable to open the selected storage folder")
                }
            } catch (e: Exception) {
                runCatching { DocumentsContract.deleteDocument(appContext.contentResolver, fileUri) }
                throw e
            } finally {
                connection.disconnect()
            }

            item.copy(
                totalBytes = maxOf(total, downloaded),
                downloadedBytes = downloaded,
                status = DownloadStatus.COMPLETED,
                speed = "Complete",
                eta = "Done",
                fileUri = fileUri.toString()
            ).also { upsert(it) }
        }.onFailure { error ->
            currentId?.let { id ->
                getDownloads().firstOrNull { it.id == id }?.let { item ->
                    upsert(item.copy(status = DownloadStatus.FAILED, speed = "0 KB/s", eta = "Failed", error = error.message))
                }
            }
        }.also {
            if (currentId != null) downloadSlots.release()
        }
    }

    fun pauseDownload(id: String) = update(id) { it.copy(status = DownloadStatus.PAUSED, speed = "0 KB/s", eta = "Paused") }

    fun resumeDownload(id: String) = update(id) { it.copy(status = DownloadStatus.DOWNLOADING, speed = "Downloading", eta = "Active") }

    fun deleteDownload(id: String) {
        val current = getDownloads().toMutableList()
        current.firstOrNull { it.id == id }?.fileUri?.let { runCatching {
            DocumentsContract.deleteDocument(appContext.contentResolver, Uri.parse(it))
        } }
        current.removeAll { it.id == id }
        saveDownloads(current)
    }

    private fun update(id: String, transform: (DownloadItem) -> DownloadItem) {
        saveDownloads(getDownloads().map { if (it.id == id) transform(it) else it })
    }

    private fun upsert(item: DownloadItem) {
        saveDownloads(getDownloads().filterNot { it.id == item.id } + item)
    }

    private fun saveDownloads(items: List<DownloadItem>) {
        val array = JSONArray()
        items.forEach { d ->
            array.put(org.json.JSONObject().apply {
                put("id", d.id); put("mediaId", d.mediaId); put("title", d.title); put("episodeTitle", d.episodeTitle)
                put("coverUrl", d.coverUrl); put("quality", d.quality); put("totalBytes", d.totalBytes)
                put("downloadedBytes", d.downloadedBytes); put("status", d.status.name); put("speed", d.speed); put("eta", d.eta)
                put("fileUri", d.fileUri ?: ""); put("error", d.error ?: "")
            })
        }
        prefs.edit().putString("custom_dls", array.toString()).apply()
    }

    private fun isNetworkAllowed(): Boolean {
        val manager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return false
        if (!settings.get().wifiOnlyDownloads) return true
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }

    private fun sanitize(value: String) =
        value.replace(Regex("[\\/:*?\"<>|]"), "_").trim().take(120).ifBlank { "Kumo Download" }

    private fun extensionFor(url: String) = when {
        url.contains(".webm", true) -> ".webm"
        url.contains(".mkv", true) -> ".mkv"
        else -> ".mp4"
    }

    private fun mimeFor(name: String) = when {
        name.endsWith(".webm", true) -> "video/webm"
        name.endsWith(".mkv", true) -> "video/x-matroska"
        else -> "video/mp4"
    }
}
