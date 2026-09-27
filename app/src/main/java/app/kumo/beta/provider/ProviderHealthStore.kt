package app.kumo.beta.provider

import android.content.Context

data class ProviderHealth(
    val providerId: String,
    val consecutiveFailures: Int,
    val lastSuccess: Long,
    val lastFailure: Long
) {
    val coolingDown: Boolean
        get() = consecutiveFailures >= 3 &&
            System.currentTimeMillis() - lastFailure < COOLDOWN_MS

    companion object {
        const val COOLDOWN_MS = 5 * 60 * 1000L
    }
}

class ProviderHealthStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kumo_provider_health", Context.MODE_PRIVATE)

    fun get(providerId: String): ProviderHealth = ProviderHealth(
        providerId = providerId,
        consecutiveFailures = prefs.getInt("$providerId.failures", 0),
        lastSuccess = prefs.getLong("$providerId.last_success", 0L),
        lastFailure = prefs.getLong("$providerId.last_failure", 0L)
    )

    fun canTry(providerId: String): Boolean = !get(providerId).coolingDown

    fun markSuccess(providerId: String) {
        prefs.edit()
            .putInt("$providerId.failures", 0)
            .putLong("$providerId.last_success", System.currentTimeMillis())
            .apply()
    }

    fun markFailure(providerId: String) {
        val current = get(providerId).consecutiveFailures
        prefs.edit()
            .putInt("$providerId.failures", (current + 1).coerceAtMost(10))
            .putLong("$providerId.last_failure", System.currentTimeMillis())
            .apply()
    }

    fun clear(providerId: String) {
        prefs.edit()
            .remove("$providerId.failures")
            .remove("$providerId.last_success")
            .remove("$providerId.last_failure")
            .apply()
    }
}
