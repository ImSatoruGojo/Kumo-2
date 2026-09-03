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

    // Mock initial list of downloads to showcase UI & functionality
    private val defaultDownloads = listOf(
        DownloadItem(
            id = "dl_1",
            mediaId = "1",
            title = "Solo Leveling",
            episodeTitle = "Episode 12 - Arise",
            coverUrl = "https://picsum.photos/300/450?random=1",
            quality = "1080p",
            totalBytes = 450_000_000L,
            downloadedBytes = 450_000_000L,
            status = DownloadStatus.COMPLETED
        ),
        DownloadItem(
            id = "dl_2",
            mediaId = "2",
            title = "Jujutsu Kaisen Season 2",
            episodeTitle = "Episode 9 - Shibuya Incident",
            coverUrl = "https://picsum.photos/300/450?random=2",
            quality = "1080p",
            totalBytes = 520_000_000L,
            downloadedBytes = 312_000_000L,
            status = DownloadStatus.DOWNLOADING,
            speed = "3.4 MB/s",
            eta = "1 min remaining"
        ),
        DownloadItem(
            id = "dl_3",
            mediaId = "3",
            title = "Demon Slayer: Hashira Training Arc",
            episodeTitle = "Episode 1 - To Defeat Muzan",
            coverUrl = "https://picsum.photos/300/450?random=3",
            quality = "720p",
            totalBytes = 380_000_000L,
            downloadedBytes = 120_000_000L,
            status = DownloadStatus.PAUSED,
            speed = "0 KB/s",
            eta = "Paused"
        ),
        DownloadItem(
            id = "dl_4",
            mediaId = "4",
            title = "Attack on Titan Final Season",
            episodeTitle = "Episode 28 - The Dawn of Humanity",
            coverUrl = "https://picsum.photos/300/450?random=4",
            quality = "1080p",
            totalBytes = 600_000_000L,
            downloadedBytes = 45_000_000L,
            status = DownloadStatus.FAILED,
            speed = "0 KB/s",
            eta = "Failed - Network Timeout"
        )
    )

    fun getDownloads(): List<DownloadItem> {
        return defaultDownloads
    }
}
