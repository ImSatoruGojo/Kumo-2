package app.kumo.beta.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class ExtensionInstaller(context: Context) {
    private val appContext = context.applicationContext
    private val store = ExtensionStore(appContext)
    private val root = File(context.filesDir, "extensions")

    suspend fun install(extension: ExtensionInfo): Result<File> = withContext(Dispatchers.IO) {
        var partialFile: File? = null
        runCatching {
            require(extension.downloadUrl.startsWith("http://") || extension.downloadUrl.startsWith("https://"))

            val directory = File(root, extension.type.name.lowercase())
                .resolve(extension.repositoryId)
            directory.mkdirs()

            val safeId = extension.id.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val downloadedName = extension.downloadUrl
                .substringBefore("?")
                .substringBefore("#")
                .substringAfterLast("/")
                .replace(Regex("[^A-Za-z0-9._-]"), "_")
                .take(100)
                .takeIf { it.contains(".") && it != "." && it != ".." }
            val finalName = downloadedName ?: safeId + ".extension"
            val temp = File(directory, finalName + ".part")
            partialFile = temp
            val finalFile = File(directory, finalName)


            val connection = URL(extension.downloadUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Kumo/0.1")

            val code = connection.responseCode
            require(code in 200..299) { "Extension download returned HTTP $code" }

            connection.inputStream.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            }

            require(temp.length() > 0) { "Downloaded extension is empty" }

            extension.fileHash?.takeIf { it.isNotBlank() }?.let { expected ->
                val actual = sha256(temp)
                require(actual.equals(expected.trim(), ignoreCase = true)) {
                    "Extension SHA-256 verification failed"
                }
            }

            // Keep the previous installed file until the new file is fully downloaded,
            // verified, moved into place, and recorded in the install store.
            val backup = File(directory, finalName + ".bak")
            if (backup.exists()) backup.delete()
            val hadPreviousFile = finalFile.exists()
            if (hadPreviousFile) {
                check(finalFile.renameTo(backup)) { "Unable to preserve the previous extension version" }
            }

            try {
                check(temp.renameTo(finalFile)) { "Unable to install extension file" }
                val wasEnabled = store.load().firstOrNull { it.id == extension.id }?.enabled ?: true
                store.upsert(
                    InstalledExtension(
                        id = extension.id,
                        packageName = extension.packageName,
                        filePath = finalFile.absolutePath,
                        version = extension.version,
                        versionCode = extension.versionCode,
                        enabled = wasEnabled
                    )
                )
                backup.delete()
                finalFile
            } catch (error: Throwable) {
                finalFile.delete()
                if (hadPreviousFile && backup.exists()) backup.renameTo(finalFile)
                throw error
            }
        }.onFailure {
            // Do not delete partial downloads belonging to other extensions installing concurrently.
            partialFile?.delete()
        }
    }

    fun installed(): List<InstalledExtension> = store.load()

    fun setEnabled(id: String, enabled: Boolean) = store.setEnabled(id, enabled)

    fun removeInstalled(id: String) {
        store.load().firstOrNull { it.id == id }?.let { File(it.filePath).delete() }
        store.remove(id)
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count <= 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
