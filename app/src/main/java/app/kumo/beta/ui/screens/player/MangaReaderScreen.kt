package app.kumo.beta.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.kumo.beta.data.local.ReaderProgressStore
import app.kumo.beta.data.local.SettingsPreferencesStore
import app.kumo.beta.model.Chapter
import app.kumo.beta.model.Title
import app.kumo.beta.provider.ChapterResolver
import coil.compose.AsyncImage

@Composable
fun MangaReaderScreen(
    title: Title,
    chapter: Chapter,
    chapterResolver: ChapterResolver,
    onBack: () -> Unit,
    onPreviousChapter: () -> Unit = {},
    onNextChapter: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings = remember { SettingsPreferencesStore(context).get() }
    val progressStore = remember { ReaderProgressStore(context) }
    var pages by remember(chapter.id) { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember(chapter.id) { mutableStateOf(true) }
    var error by remember(chapter.id) { mutableStateOf<String?>(null) }
    var loadNonce by remember(chapter.id) { mutableIntStateOf(0) }
    var currentPage by remember(chapter.id) { mutableIntStateOf(progressStore.getPage(chapter.id)) }
    val verticalListState = rememberLazyListState()

    LaunchedEffect(chapter.id, loadNonce) {
        loading = true
        error = null
        val resolved = runCatching { chapterResolver.resolve(chapter) }.getOrDefault(emptyList())
        pages = resolved
        loading = false
        if (resolved.isEmpty()) error = "No chapter pages are available from the enabled manga providers"
    }

    LaunchedEffect(currentPage, pages.size) {
        if (pages.isNotEmpty()) {
            currentPage = currentPage.coerceIn(0, pages.lastIndex)
            progressStore.savePage(chapter.id, currentPage)
        }
    }

    LaunchedEffect(chapter.id, pages, settings.readingMode) {
        if (pages.isNotEmpty() && !settings.readingMode.equals("Paged", true)) {
            verticalListState.scrollToItem(currentPage.coerceIn(0, pages.lastIndex))
            snapshotFlow { verticalListState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { pageIndex ->
                    currentPage = pageIndex
                    progressStore.savePage(chapter.id, pageIndex)
                }
        }
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Column(Modifier.weight(1f)) {
                Text(title.title, style = MaterialTheme.typography.titleMedium)
                Text(chapter.title ?: "Chapter " + chapter.number, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

            pages.isEmpty() -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(error ?: "No pages found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { loadNonce++ }) { Text("Retry") }
            }

            settings.readingMode.equals("Paged", true) -> {
                val index = currentPage.coerceIn(0, pages.lastIndex)
                Column(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = pages[index],
                            contentDescription = "Page " + (index + 1),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = onPreviousChapter) { Text("Prev chapter") }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (settings.readingDirection.equals("Right to Left", true)) currentPage = (currentPage + 1).coerceAtMost(pages.lastIndex)
                                    else currentPage = (currentPage - 1).coerceAtLeast(0)
                                }
                            ) { Text("Prev") }
                            Text((index + 1).toString() + " / " + pages.size, modifier = Modifier.padding(top = 10.dp))
                            OutlinedButton(
                                onClick = {
                                    if (settings.readingDirection.equals("Right to Left", true)) currentPage = (currentPage - 1).coerceAtLeast(0)
                                    else currentPage = (currentPage + 1).coerceAtMost(pages.lastIndex)
                                }
                            ) { Text("Next") }
                        }
                        Button(onClick = onNextChapter) { Text("Next chapter") }
                    }
                }
            }

            else -> {
                Column(Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = verticalListState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(pages) { page ->
                            AsyncImage(
                                model = page,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = onPreviousChapter) { Text("Prev chapter") }
                        Button(onClick = onNextChapter) { Text("Next chapter") }
                    }
                }
            }
        }
    }
}