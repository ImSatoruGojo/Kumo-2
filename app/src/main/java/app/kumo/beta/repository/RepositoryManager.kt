package app.kumo.beta.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class RepositoryManager(context: Context) {
    private val store = RepositoryStore(context.applicationContext)
    private val repositories = store.load().toMutableList()
    private val extensions = mutableMapOf<String, List<ExtensionInfo>>()

    fun getRepositories(): List<Repository> = repositories.toList()
    fun getExtensions(repositoryId: String): List<ExtensionInfo> = extensions[repositoryId].orEmpty()
    fun getAllExtensions(): List<ExtensionInfo> = extensions.values.flatten().distinctBy { it.id }

    fun addRepository(url: String): Repository {
        val normalized = url.trim().removeSuffix("/")
        require(normalized.startsWith("http://") || normalized.startsWith("https://"))
        val id = "repo:" + normalized.lowercase().hashCode().toUInt().toString(16)
        repositories.firstOrNull { it.id == id }?.let { return it }
        return Repository(id = id, url = normalized).also {
            repositories += it
            store.save(repositories)
        }
    }

    fun removeRepository(id: String) {
        repositories.removeAll { it.id == id }
        extensions.remove(id)
        store.save(repositories)
    }

    fun setEnabled(id: String, enabled: Boolean) {
        val index = repositories.indexOfFirst { it.id == id }
        if (index >= 0) {
            repositories[index] = repositories[index].copy(enabled = enabled)
            store.save(repositories)
        }
    }

    suspend fun refreshRepository(id: String): RepositoryResult = withContext(Dispatchers.IO) {
        val repo = repositories.firstOrNull { it.id == id }
            ?: return@withContext RepositoryResult.Failure(
                RepositoryResult.Reason.INVALID_REPOSITORY, "Repository not found"
            )

        try {
            val connection = URL(repo.url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 20000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Kumo/0.1")
            val code = connection.responseCode

            if (code !in 200..299) {
                return@withContext RepositoryResult.Failure(
                    RepositoryResult.Reason.HTTP, "Repository returned HTTP $code"
                )
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val parsed = RepositoryParser.parse(repo.url, body)

            if (parsed is RepositoryResult.Success) {
                val updated = parsed.repository.copy(
                    id = repo.id,
                    url = repo.url,
                    enabled = repo.enabled,
                    lastRefresh = System.currentTimeMillis(),
                    lastRefreshStatus = "OK"
                )
                val normalizedExtensions = parsed.extensions.map { extension ->
                    extension.copy(
                        repositoryId = repo.id,
                        repositoryUrl = repo.url
                    )
                }
                val index = repositories.indexOfFirst { it.id == id }
                if (index >= 0) repositories[index] = updated
                extensions[id] = normalizedExtensions
                store.save(repositories)
                parsed.copy(repository = updated, extensions = normalizedExtensions)
            } else {
                val failure = parsed as RepositoryResult.Failure
                val index = repositories.indexOfFirst { it.id == id }
                if (index >= 0) {
                    repositories[index] = repo.copy(
                        lastRefresh = System.currentTimeMillis(),
                        lastRefreshStatus = failure.reason.name
                    )
                    store.save(repositories)
                }
                failure
            }
        } catch (e: Exception) {
            RepositoryResult.Failure(
                RepositoryResult.Reason.NETWORK,
                e.message ?: "Unable to reach repository"
            )
        }
    }

    suspend fun refreshAllRepositories(): Map<String, RepositoryResult> = withContext(Dispatchers.IO) {
        repositories.filter { it.enabled }
            .map { repo ->
                repo.id to kotlinx.coroutines.async { refreshRepository(repo.id) }
            }
            .associate { (id, deferred) -> id to deferred.await() }
    }
}
