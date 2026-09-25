package app.kumo.beta.extension

import android.content.Context
import app.kumo.beta.provider.KumoProvider

interface KumoExtension {
    val id: String
    val name: String
    val version: String
    fun load(context: Context): List<KumoProvider>
    fun unload()
}