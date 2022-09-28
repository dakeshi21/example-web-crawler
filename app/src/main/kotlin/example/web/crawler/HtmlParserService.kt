package example.web.crawler

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL

class HtmlParserService(var domain: String = "", private val timeoutInMillis: Int = 30_000) {

    suspend fun internalLinks(pageUrl: String): Result<Set<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val page = Jsoup.connect(pageUrl)
                .timeout(timeoutInMillis)
                .get()

            page.select("a[href]").asSequence()
                .map { it.absUrl("href") }
                .filter {
                    URL(it).host == domain
                }
                .toSet()
        }
    }

}