package app.kumo.beta.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class RepositoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("kumo_repositories", Context.MODE_PRIVATE)

    fun save(repositories: List<Repository>) {
        val array = JSONArray()
        repositories.forEach {
            array.put(JSONObject()
                .put("id", it.id)
                .put("url", it.url)
                .put("name", it.name)
                .put("description", it.description)
                .put("type", it.type.name)
                .put("enabled", it.enabled)
                .put("iconUrl", it.iconUrl)
                .put("lastRefresh", it.lastRefresh)
                .put("lastRefreshStatus", it.lastRefreshStatus))
        }
        prefs.edit().putString("repositories", array.toString()).apply()
    }

    fun load(): List<Repository> {
        val raw = prefs.getString("repositories", null) ?: return emptyList()
        val array = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.optJSONObject(i) ?: continue
                add(Repository(
                    id = o.optString("id"),
                    url = o.optString("url"),
                    name = o.optString("name"),
                    description = o.optString("description"),
                    type = runCatching { RepositoryType.valueOf(o.optString("type")) }.getOrDefault(RepositoryType.UNKNOWN),
                    enabled = o.optBoolean("enabled", true),
                    iconUrl = o.optString("iconUrl").takeIf { it.isNotBlank() },
                    lastRefresh = if (o.isNull("lastRefresh")) null else o.optLong("lastRefresh"),
                    lastRefreshStatus = o.optString("lastRefreshStatus").takeIf { it.isNotBlank() }
                ))
            }
        }
    }
}
