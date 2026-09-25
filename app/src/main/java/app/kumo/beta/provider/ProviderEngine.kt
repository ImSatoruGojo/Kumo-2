package app.kumo.beta.provider

import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ProviderEngine(private val registry: ProviderRegistry) {
    suspend fun search(query:String,type:MediaType?=null):List<KumoSearchResult> = coroutineScope {
        registry.getEnabledProviders().filter{type==null || type in it.supportedTypes}
            .map{p->async{runCatching{p.search(query)}.getOrDefault(emptyList())}}.awaitAll().flatten().let(::mergeTitles)
    }
    suspend fun load(title:Title):Title {
        for(p in registry.getEnabledProviders().filter{title.type in it.supportedTypes})
            runCatching{p.load(title)}.getOrNull()?.takeIf{it.title.isNotBlank()}?.let{return it}
        return title
    }
    private fun mergeTitles(results:List<KumoSearchResult>):List<KumoSearchResult>{
        val merged=LinkedHashMap<String,KumoSearchResult>()
        results.forEach{r->val k=normalize(r.title.title);val old=merged[k];if(old==null||score(r.title)>score(old.title))merged[k]=r}
        return merged.values.toList()
    }
    private fun normalize(v:String)=v.lowercase().replace(Regex("[^a-z0-9]+")," ").trim()
    private fun score(t:Title)=(t.rating?.times(10f)?.toInt()?:0)+(if(t.posterUrl!=null)5 else 0)
}
