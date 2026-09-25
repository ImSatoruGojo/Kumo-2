package app.kumo.beta.extension

import android.content.Context
import android.util.Log
import app.kumo.beta.provider.KumoProvider
import app.kumo.beta.provider.ProviderRegistry
import dalvik.system.PathClassLoader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONObject

class ExtensionRuntime(
    private val context: Context,
    private val registry: ProviderRegistry
) {
    data class RuntimeInfo(
        val file: File,
        val id: String,
        val name: String,
        val version: String?,
        val format: Format,
        val loaded: Boolean,
        val providerCount: Int,
        val error: String? = null
    )

    enum class Format { KUMO_NATIVE, CLOUDSTREAM, UNKNOWN }

    private data class Loaded(val extension: KumoExtension, val providerIds: List<String>)
    private val loaded = ConcurrentHashMap<String, Loaded>()

    fun inspect(file: File, fallbackId: String): RuntimeInfo {
        val manifest = readManifest(file)
            ?: return RuntimeInfo(file, fallbackId, fallbackId, null, Format.UNKNOWN, false, 0, "No manifest.json found")
        val name = manifest.optString("name").ifBlank { fallbackId }
        val version = manifest.optString("version").ifBlank { null }
        val className = manifest.optString("pluginClassName").trim()
        return RuntimeInfo(
            file, fallbackId, name, version, Format.CLOUDSTREAM, false, 0,
            if (className.isBlank()) "Missing pluginClassName" else null
        )
    }

    fun load(file: File, fallbackId: String): RuntimeInfo {
        val manifest = readManifest(file)
            ?: return RuntimeInfo(file, fallbackId, fallbackId, null, Format.UNKNOWN, false, 0, "No manifest.json found")

        val className = manifest.optString("pluginClassName").trim()
        if (className.isBlank()) {
            return RuntimeInfo(file, fallbackId, manifest.optString("name", fallbackId), manifest.optString("version").ifBlank { null }, Format.CLOUDSTREAM, false, 0, "Missing pluginClassName")
        }

        return runCatching {
            val loader = PathClassLoader(file.absolutePath, context.classLoader)
            val clazz = loader.loadClass(className)
            val instance = clazz.getDeclaredConstructor().newInstance()
            if (instance !is KumoExtension) {
                RuntimeInfo(file, fallbackId, manifest.optString("name", fallbackId), manifest.optString("version").ifBlank { null }, Format.CLOUDSTREAM, false, 0, "CloudStream API detected; compatibility adapter is required")
            } else {
                val providers = instance.load(context)
                providers.forEach(registry::registerProvider)
                loaded[file.absolutePath] = Loaded(instance, providers.map { it.id })
                RuntimeInfo(file, fallbackId, instance.name, instance.version, Format.KUMO_NATIVE, true, providers.size)
            }
        }.getOrElse { error ->
            Log.e("ExtensionRuntime", "Failed to load $file", error)
            RuntimeInfo(file, fallbackId, manifest.optString("name", fallbackId), manifest.optString("version").ifBlank { null }, Format.CLOUDSTREAM, false, 0, error.message ?: error.javaClass.simpleName)
        }
    }

    fun unload(file: File): Boolean {
        val item = loaded.remove(file.absolutePath) ?: return false
        item.providerIds.forEach(registry::unregisterProvider)
        runCatching { item.extension.unload() }
        return true
    }

    fun loadedExtensions(): List<KumoExtension> = loaded.values.map { it.extension }

    private fun readManifest(file: File): JSONObject? = runCatching {
        if (!file.exists() || file.length() == 0L) return@runCatching null
        java.util.zip.ZipFile(file).use { zip ->
            val entry = zip.getEntry("manifest.json") ?: return@use null
            InputStreamReader(zip.getInputStream(entry), Charsets.UTF_8).use { JSONObject(it.readText()) }
        }
    }.getOrNull()
}