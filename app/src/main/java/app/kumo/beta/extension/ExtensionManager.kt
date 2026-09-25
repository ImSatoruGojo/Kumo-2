package app.kumo.beta.extension

import android.content.Context
import app.kumo.beta.provider.ProviderRegistry
import app.kumo.beta.repository.ExtensionStore
import java.io.File

class ExtensionManager(
    context: Context,
    private val registry: ProviderRegistry
) {
    private val appContext = context.applicationContext
    private val store = ExtensionStore(appContext)
    private val runtime = ExtensionRuntime(appContext, registry)

    fun loadInstalled(): List<ExtensionRuntime.RuntimeInfo> =
        store.load()
            .filter { it.enabled }
            .map { installed ->
                val file = File(installed.filePath)
                if (!file.exists()) {
                    ExtensionRuntime.RuntimeInfo(
                        file = file,
                        id = installed.id,
                        name = installed.id,
                        version = installed.version,
                        format = ExtensionRuntime.Format.UNKNOWN,
                        loaded = false,
                        providerCount = 0,
                        error = "Installed extension file is missing"
                    )
                } else {
                    runtime.load(file, installed.id)
                }
            }

    fun unloadAll() {
        runtime.loadedExtensions().toList().forEach { extension ->
            store.load().firstOrNull { it.id == extension.id }?.let { runtime.unload(File(it.filePath)) }
        }
    }

    fun runtime(): ExtensionRuntime = runtime
}
