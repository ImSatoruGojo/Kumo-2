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
    val error: String? = null,
    val sourceUrl: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val referer: String? = null,
    val mimeType: String? = null
)

class DownloadManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("kumo_downloads", Context.MODE_PRIVATE)
    private val storage = StorageLocationManager(appContext)
    private val settings = SettingsPreferencesStore(appContext)
    private val downloadSlots = java.util.concurrent.Semaphore(12, true)

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
                        error = o.optString("error").takeIf { it.isNotBlank() },
                        sourceUrl = o.optString("sourceUrl").takeIf { it.isNotBlank() },
                        headers = o.optJSONObject("headers")?.let { obj ->
                            obj.keys().asSequence().associateWith { key -> obj.optString(key) }
                        } ?: emptyMap(),
                        referer = o.optString("referer").takeIf { it.isNotBlank() },
                        mimeType = o.optString("mimeType").takeIf { it.isNotBlank() }                    ))
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
        sourceUrl: String,
        headers: Map<String, String> = emptyMap(),
        referer: String? = null,
        mimeType: String? = null
    ): Result<DownloadItem> = withContext(Dispatchers.IO) {
        var currentId: String? = null
        var slotPermits = 0
        runCatching {
            require(storage.hasValidLocation()) { "Choose a download folder in Settings first" }
            require(isNetworkAllowed()) { "Downloads are restricted to Wi Fi while Wi Fi only is enabled" }
            require(sourceUrl.startsWith("http://") || sourceUrl.startsWith("https://")) { "Invalid download URL" }
            getDownloads().firstOrNull {
                it.mediaId == mediaId &&
                    it.episodeTitle == episodeTitle &&
                    it.quality == quality &&
                    it.status == DownloadStatus.COMPLETED &&
                    !it.fileUri.isNullOrBlank()
            }?.let { existing ->
                return@runCatching existing
            }
            slotPermits = 12 / settings.get().maxConcurrentDownloads.coerceIn(1, 4)
            require(downloadSlots.tryAcquire(slotPermits)) { "Download queue is full; try again when an active download finishes" }

            val item = DownloadItem(
                id = UUID.randomUUID().toString(),
                mediaId = mediaId,
                title = title,
                episodeTitle = episodeTitle,
                coverUrl = coverUrl,
                quality = quality,
                totalBytes = 0L,
                downloadedBytes = 0L,
                status = DownloadStatus.DOWNLOADING,
                sourceUrl = sourceUrl,
                headers = headers,
                referer = referer,
                mimeType = mimeType
            )
            currentId = item.id
            upsert(item)

            if (sourceUrl.substringBefore("?").endsWith(".m3u8", ignoreCase = true)) {
                val completed = downloadHls(
                    item = item,
                    playlistUrl = sourceUrl,
                    headers = headers,
                    referer = referer
                )
                upsert(completed)
                return@runCatching completed
            }

            val connection = URL(sourceUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = settings.get().networkTimeoutSeconds * 1000
            connection.readTimeout = settings.get().networkTimeoutSeconds * 2000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Kumo/0.1")
            headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
            referer?.let { connection.setRequestProperty("Referer", it) }
            require(connection.responseCode in 200..299) { "Download returned HTTP " + connection.responseCode }

            val total = connection.contentLengthLong.coerceAtLeast(0L)
            val tree = storage.getTreeUri() ?: error("Download folder is no longer available")
            val extension = extensionFor(sourceUrl, mimeType)
            val name = sanitize(title + " - " + episodeTitle) + extension
            val fileUri = DocumentsContract.createDocument(appContext.contentResolver, tree, mimeFor(name, mimeType), name)
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
            if (slotPermits > 0) downloadSlots.release(slotPermits)
        }
    }

    fun reconcile() {
        val reconciled = getDownloads().map { item ->
            if (item.status == DownloadStatus.COMPLETED && !item.fileUri.isNullOrBlank()) {
                val exists = runCatching {
                    appContext.contentResolver.query(Uri.parse(item.fileUri), arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null)
                        ?.use { it.moveToFirst() } == true
                }.getOrDefault(false)
                if (exists) item else item.copy(
                    status = DownloadStatus.FAILED,
                    error = "Downloaded file is no longer available",
                    speed = "0 KB/s",
                    eta = "Missing"
                )
            } else item
        }
        saveDownloads(reconciled)
    }

    suspend fun retryDownload(id: String): Result<DownloadItem> {
        val item = getDownloads().firstOrNull { it.id == id }
            ?: return Result.failure(IllegalArgumentException("Download not found"))
        val sourceUrl = item.sourceUrl
            ?: return Result.failure(IllegalStateException("The original download source is unavailable"))
        deleteDownload(id)
        return downloadDirect(
            mediaId = item.mediaId,
            title = item.title,
            episodeTitle = item.episodeTitle,
            coverUrl = item.coverUrl,
            quality = item.quality,
            sourceUrl = sourceUrl,
            headers = item.headers,
            referer = item.referer,
            mimeType = item.mimeType
        )
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
                put("sourceUrl", d.sourceUrl ?: ""); put("referer", d.referer ?: ""); put("mimeType", d.mimeType ?: "")
                put("headers", org.json.JSONObject().apply { d.headers.forEach { (key, value) -> put(key, value) } })
            })
        }
        prefs.edit().putString("custom_dls", array.toString()).apply()
    }

    private fun downloadHls(
        item: DownloadItem,
        playlistUrl: String,
        headers: Map<String, String>,
        referer: String?
    ): DownloadItem {
        var currentUrl = playlistUrl
        var playlist = fetchText(currentUrl, headers, referer)

        repeat(2) {
            val variant = parseMasterVariants(currentUrl, playlist).maxByOrNull { it.bandwidth }
            if (variant == null) return@repeat
            currentUrl = variant.url
            playlist = fetchText(currentUrl, headers, referer)
        }

        require(playlist.lines().any { it.startsWith("#EXT-X-ENDLIST") }) {
            "Only completed VOD HLS playlists can be downloaded"
        }
        require(playlist.lines().none { it.startsWith("#EXT-X-KEY") && !it.contains("METHOD=NONE") }) {
            "Encrypted HLS downloads are not supported yet"
        }
        require(playlist.lines().none { it.startsWith("#EXT-X-BYTERANGE") }) {
            "HLS byte range playlists are not supported yet"
        }

        val lines = playlist.lines().map { it.trim() }
        val initMap = lines.firstOrNull { it.startsWith("#EXT-X-MAP:") }?.let { line ->
            Regex("URI=\"([^\"]+)\"").find(line)?.groupValues?.getOrNull(1)
        }
        val segments = lines
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { java.net.URI(currentUrl).resolve(it).toString() }
            .distinct()
        require(segments.isNotEmpty()) { "HLS playlist contains no media segments" }

        val tree = storage.getTreeUri() ?: error("Download folder is no longer available")
        val extension = if (initMap != null) ".mp4" else ".ts"
        val mime = if (initMap != null) "video/mp4" else "video/mp2t"
        val name = sanitize(item.title + " - " + item.episodeTitle) + extension
        val fileUri = DocumentsContract.createDocument(
            appContext.contentResolver,
            tree,
            mime,
            name
        ) ?: error("Unable to create the download file")

        var downloaded = 0L
        try {
            appContext.contentResolver.openOutputStream(fileUri, "w")?.use { output ->
                if (initMap != null) {
                    writeUrlToOutput(java.net.URI(currentUrl).resolve(initMap).toString(), headers, referer, output) { bytes ->
                        downloaded += bytes
                        upsert(item.copy(downloadedBytes = downloaded, status = DownloadStatus.DOWNLOADING))
                    }
                }
                segments.forEachIndexed { index, segment ->
                    writeUrlToOutput(segment, headers, referer, output) { bytes ->
                        downloaded += bytes
                    }
                    upsert(
                        item.copy(
                            totalBytes = 0L,
                            downloadedBytes = downloaded,
                            status = DownloadStatus.DOWNLOADING,
                            speed = "Segment " + (index + 1) + "/" + segments.size,
                            eta = "--"
                        )
                    )
                }
            } ?: error("Unable to open the selected storage folder")
        } catch (e: Exception) {
            runCatching { DocumentsContract.deleteDocument(appContext.contentResolver, fileUri) }
            throw e
        }

        return item.copy(
            totalBytes = downloaded,
            downloadedBytes = downloaded,
            status = DownloadStatus.COMPLETED,
            speed = "Complete",
            eta = "Done",
            fileUri = fileUri.toString()
        )
    }

    private data class HlsVariant(val url: String, val bandwidth: Long)

    private fun parseMasterVariants(baseUrl: String, playlist: String): List<HlsVariant> {
        val lines = playlist.lines().map { it.trim() }
        val variants = mutableListOf<HlsVariant>()
        for (i in lines.indices) {
            if (!lines[i].startsWith("#EXT-X-STREAM-INF:")) continue
            val bandwidth = Regex("BANDWIDTH=(\\d+)").find(lines[i])?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
            val uri = lines.drop(i + 1).firstOrNull { it.isNotBlank() && !it.startsWith("#") } ?: continue
            variants += HlsVariant(java.net.URI(baseUrl).resolve(uri).toString(), bandwidth)
        }
        return variants
    }

    private fun fetchText(url: String, headers: Map<String, String>, referer: String?): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = settings.get().networkTimeoutSeconds * 1000
        connection.readTimeout = settings.get().networkTimeoutSeconds * 2000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "Kumo/0.1")
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        referer?.let { connection.setRequestProperty("Referer", it) }
        return try {
            require(connection.responseCode in 200..299) { "HLS playlist returned HTTP " + connection.responseCode }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun writeUrlToOutput(
        url: String,
        headers: Map<String, String>,
        referer: String?,
        output: java.io.OutputStream,
        onBytes: (Long) -> Unit
    ) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = settings.get().networkTimeoutSeconds * 1000
        connection.readTimeout = settings.get().networkTimeoutSeconds * 2000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "Kumo/0.1")
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        referer?.let { connection.setRequestProperty("Referer", it) }
        try {
            require(connection.responseCode in 200..299) { "HLS segment returned HTTP " + connection.responseCode }
            connection.inputStream.use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count <= 0) break
                    output.write(buffer, 0, count)
                    onBytes(count.toLong())
                }
            }
        } finally {
            connection.disconnect()
        }
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

    private fun extensionFor(url: String, mimeType: String? = null) = when {
        mimeType.equals("video/webm", true) || url.contains(".webm", true) -> ".webm"
        mimeType.equals("video/x-matroska", true) || url.contains(".mkv", true) -> ".mkv"
        mimeType.equals("video/mp2t", true) || url.contains(".ts", true) -> ".ts"
        else -> ".mp4"
    }

    private fun mimeFor(name: String, mimeType: String? = null) = when {
        mimeType?.isNotBlank() == true -> mimeType
        name.endsWith(".webm", true) -> "video/webm"
        name.endsWith(".mkv", true) -> "video/x-matroska"
        name.endsWith(".ts", true) -> "video/mp2t"
        else -> "video/mp4"
    }
}
