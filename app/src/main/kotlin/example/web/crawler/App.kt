package example.web.crawler

import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val configProps = ConfigFactory.parseResources("application.conf").resolve()

    runBlocking {
        val time = measureTimeMillis {
            launch {
                CrawlerService(
                    htmlParserService = HtmlParserService(
                        domain = configProps.getString("domain"),
                        timeoutInMillis = 1_000
                    ),
                    maxLevel = configProps.getInt("max_level")
                ).crawl(baseUrl = configProps.getString("base_url"))
            }.join()
        }
        logger.debug { "Crawler finished in $time ms" }
    }
}
