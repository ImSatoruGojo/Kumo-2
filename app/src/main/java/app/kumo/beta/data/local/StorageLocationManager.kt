package app.kumo.beta.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

class StorageLocationManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("kumo_storage", Context.MODE_PRIVATE)

    fun getTreeUri(): Uri? = prefs.getString(KEY_TREE_URI, null)?.let { runCatching { Uri.parse(it) }.getOrNull() }

    fun setTreeUri(uri: Uri, flags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) {
        runCatching { appContext.contentResolver.takePersistableUriPermission(uri, flags) }
        prefs.edit().putString(KEY_TREE_URI, uri.toString()).apply()
    }

    fun clearTreeUri() {
        getTreeUri()?.let { uri ->
            runCatching { appContext.contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
        }
        prefs.edit().remove(KEY_TREE_URI).apply()
    }

    fun hasValidLocation(): Boolean {
        val uri = getTreeUri() ?: return false
        return runCatching {
            appContext.contentResolver.query(
                DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)),
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null
            )?.use { it.moveToFirst() } == true
        }.getOrDefault(false)
    }

    fun displayName(): String? {
        val uri = getTreeUri() ?: return null
        return runCatching {
            appContext.contentResolver.query(
                DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)),
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null
            )?.use { if (it.moveToFirst()) it.getString(0) else null }
        }.getOrNull()
    }

    companion object { private const val KEY_TREE_URI = "download_tree_uri" }
}
