package example.web.crawler

import com.google.common.annotations.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

data class Metadata(val url: String, val depth: Int)

class CrawlerService(private val htmlParserService: HtmlParserService, private val maxLevel: Int = 5) {
    private val visitedLinks = ConcurrentHashMap.newKeySet<String>()
    private val queue = ConcurrentLinkedDeque<Metadata>()

    private var counter = AtomicInteger(0)

    suspend fun crawl(baseUrl: String) = coroutineScope {
        logger.info { "Starting WebCrawler to process '$baseUrl' with domain='${htmlParserService.domain}'" }
        queue.addLast(Metadata(url = baseUrl, depth = 0))
        visitedLinks.add(baseUrl)
        counter.incrementAndGet()

        while (!queue.isEmpty()) {
            queue.map {
                async(Dispatchers.IO) {
                    val (link, depth) = queue.removeFirst()
                    logger.debug { "Visited $link page" }

                    val internalLinks = async { htmlParserService.internalLinks(pageUrl = link) }

                    internalLinks.await()
                        .onSuccess { subLinks ->
                            logger.debug { "List of subLinks found on $link ='$subLinks'" }
                            subLinks.map {
                                if (!visitedLinks.contains(it) && depth + 1 < maxLevel) {
                                    queue.addLast(Metadata(url = it, depth = depth + 1))
                                    visitedLinks.add(it)
                                }
                            }
                        }
                        .onFailure {
                            logger.debug { "Failed to process the web resource, error='${it.message}'" }
                        }
                }
            }.awaitAll()
        }
        logger.info { "Stopped WebCrawler" }
    }

    @VisibleForTesting
    fun readVisitedLinksView() = visitedLinks
}