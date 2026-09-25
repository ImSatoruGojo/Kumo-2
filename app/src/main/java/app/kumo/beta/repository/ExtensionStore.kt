package app.kumo.beta.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class InstalledExtension(val id:String,val packageName:String?,val filePath:String,val version:String?,val versionCode:Long?,val enabled:Boolean=true)

class ExtensionStore(context: Context) {
    private val prefs=context.applicationContext.getSharedPreferences("kumo_extensions",Context.MODE_PRIVATE)
    fun load():List<InstalledExtension>{
        val a=JSONArray(prefs.getString("installed","[]"))
        return (0 until a.length()).mapNotNull { i -> runCatching {
            val o=a.getJSONObject(i)
            InstalledExtension(o.getString("id"),o.optString("packageName").takeIf(String::isNotBlank),o.getString("filePath"),o.optString("version").takeIf(String::isNotBlank),if(o.has("versionCode")) o.optLong("versionCode") else null,o.optBoolean("enabled",true))
        }.getOrNull() }
    }
    fun upsert(e:InstalledExtension){ save(load().filterNot{it.id==e.id}+e) }
    fun remove(id:String){ save(load().filterNot{it.id==id}) }
    fun setEnabled(id:String,enabled:Boolean){ save(load().map{if(it.id==id)it.copy(enabled=enabled) else it}) }
    private fun save(items:List<InstalledExtension>){
        val a=JSONArray()
        items.forEach{e->a.put(JSONObject().apply{put("id",e.id);put("packageName",e.packageName?:"");put("filePath",e.filePath);put("version",e.version?:"");e.versionCode?.let{put("versionCode",it)};put("enabled",e.enabled)})}
        prefs.edit().putString("installed",a.toString()).apply()
    }
}
