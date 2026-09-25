package app.kumo.beta.repository

enum class RepositoryType { CLOUDSTREAM, CLOUDSTREAM_PLUGIN_LIST, ANIYOMI, UNKNOWN }

data class Repository(
    val id: String,
    val url: String,
    val name: String = url,
    val description: String = "",
    val type: RepositoryType = RepositoryType.UNKNOWN,
    val enabled: Boolean = true,
    val iconUrl: String? = null,
    val lastRefresh: Long? = null,
    val lastRefreshStatus: String? = null
)

data class ExtensionInfo(
    val id: String,
    val name: String,
    val internalName: String? = null,
    val packageName: String? = null,
    val repositoryId: String,
    val repositoryUrl: String,
    val type: RepositoryType,
    val downloadUrl: String,
    val version: String? = null,
    val versionCode: Long? = null,
    val language: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
    val fileSize: Long? = null,
    val fileHash: String? = null,
    val nsfw: Boolean = false,
    val installed: Boolean = false,
    val enabled: Boolean = true
)

sealed class RepositoryResult {
    data class Success(val repository: Repository, val extensions: List<ExtensionInfo>) : RepositoryResult()
    data class Failure(val reason: Reason, val message: String) : RepositoryResult()
    enum class Reason { NETWORK, HTTP, INVALID_JSON, UNSUPPORTED_FORMAT, INVALID_REPOSITORY, EMPTY_REPOSITORY }
}
