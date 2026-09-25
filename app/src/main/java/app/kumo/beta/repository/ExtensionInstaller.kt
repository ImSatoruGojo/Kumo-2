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
        runCatching {
            require(extension.downloadUrl.startsWith("http://") || extension.downloadUrl.startsWith("https://"))

            val directory = File(root, extension.type.name.lowercase())
                .resolve(extension.repositoryId)
            directory.mkdirs()

            val temp = File(directory, extension.id.replace(Regex("[^A-Za-z0-9._-]"), "_") + ".part")
            val finalFile = File(directory, extension.id.replace(Regex("[^A-Za-z0-9._-]"), "_"))

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

            if (finalFile.exists()) finalFile.delete()
            check(temp.renameTo(finalFile)) { "Unable to install extension file" }

            store.upsert(
                InstalledExtension(
                    id = extension.id,
                    packageName = extension.packageName,
                    filePath = finalFile.absolutePath,
                    version = extension.version,
                    versionCode = extension.versionCode,
                    enabled = true
                )
            )
            finalFile
        }.onFailure {
            File(root, extension.type.name.lowercase())
                .walkTopDown()
                .filter { it.name.endsWith(".part") }
                .forEach { it.delete() }
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
