package app.kumo.beta.data

import app.kumo.beta.model.Title
import java.util.concurrent.ConcurrentHashMap

object CatalogStore {
    private val titles = ConcurrentHashMap<String, Title>()
    fun put(title: Title) { titles[title.id] = title }
    fun putAll(items: Iterable<Title>) { items.forEach(::put) }
    fun get(id: String): Title? = titles[id]
    fun allTitles(): List<Title> = titles.values.toList()
}
