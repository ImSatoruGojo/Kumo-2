package app.kumo.beta.provider

import app.kumo.beta.model.MediaType
import java.util.concurrent.ConcurrentHashMap

class ProviderRegistry {
    private val providers = ConcurrentHashMap<String, KumoProvider>()
    private val enabled = ConcurrentHashMap.newKeySet<String>()

    fun registerProvider(provider: KumoProvider) {
        providers[provider.id] = provider
        enabled += provider.id
    }

    fun unregisterProvider(id: String) {
        providers.remove(id)
        enabled.remove(id)
    }

    fun enableProvider(id: String) {
        if (providers.containsKey(id)) enabled += id
    }

    fun disableProvider(id: String) {
        enabled.remove(id)
    }

    fun getProvider(id: String): KumoProvider? = providers[id]
    fun getProviders(): List<KumoProvider> = providers.values.toList()
    fun getEnabledProviders(): List<KumoProvider> = enabled.mapNotNull { providers[it] }

    fun getProvidersForMediaType(type: MediaType): List<KumoProvider> =
        getEnabledProviders().filter { type in it.supportedTypes }

    fun getProvidersForLanguage(language: String): List<KumoProvider> =
        getEnabledProviders().filter { it.language.equals(language, ignoreCase = true) }
}
