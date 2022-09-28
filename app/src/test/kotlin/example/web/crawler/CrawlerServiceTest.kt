package example.web.crawler

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CrawlerServiceTest {
    private val htmlParserService = mockk<HtmlParserService>()

    @Test
    fun `returns all visited links up to level 2`() = runBlocking {
        val crawlerService = CrawlerService(htmlParserService = htmlParserService, maxLevel = 2)
        val subLinks = setOf("http://www.example.com/page_1", "http://www.example.com/page_2")

        every { htmlParserService.domain } returns "www.example.com"
        coEvery { htmlParserService.internalLinks("http://www.example.com") } returns Result.success(subLinks)
        coEvery { htmlParserService.internalLinks("http://www.example.com/page_1") } returns Result.success(emptySet())
        coEvery { htmlParserService.internalLinks("http://www.example.com/page_2") } returns Result.success(emptySet())

        crawlerService.crawl(baseUrl = "http://www.example.com")

        assertEquals(
            expected = setOf(
                "http://www.example.com",
                "http://www.example.com/page_1",
                "http://www.example.com/page_2"
            ),
            actual = crawlerService.readVisitedLinksView()
        )
    }

    @Test
    fun `returns all visited links up to level 1`() = runBlocking {
        val crawlerService = CrawlerService(htmlParserService = htmlParserService, maxLevel = 1)
        val subLinks = setOf("http://www.example.com/page_1", "http://www.example.com/page_2")

        every { htmlParserService.domain } returns "www.example.com"
        coEvery { htmlParserService.internalLinks("http://www.example.com") } returns Result.success(subLinks)
        coEvery { htmlParserService.internalLinks("http://www.example.com/page_1") } returns Result.success(emptySet())
        coEvery { htmlParserService.internalLinks("http://www.example.com/page_2") } returns Result.success(emptySet())

        crawlerService.crawl(baseUrl = "http://www.example.com")

        assertEquals(setOf("http://www.example.com"), crawlerService.readVisitedLinksView())
    }

    @Test
    fun `returns top-level link when sub-links don't match domain`() = runBlocking {
        val crawlerService = CrawlerService(htmlParserService = htmlParserService, maxLevel = 2)

        every { htmlParserService.domain } returns "www.example.com"
        coEvery { htmlParserService.internalLinks("http://www.example.com") } returns Result.success(emptySet())

        crawlerService.crawl(baseUrl = "http://www.example.com")

        assertEquals(setOf("http://www.example.com"), crawlerService.readVisitedLinksView())
    }

    @Test
    fun `returns top-level link when sub-links return an error`() = runBlocking {
        val crawlerService = CrawlerService(htmlParserService = htmlParserService, maxLevel = 2)

        every { htmlParserService.domain } returns "www.example.com"
        coEvery { htmlParserService.internalLinks("http://www.example.com") } returns Result.failure(RuntimeException("HTTP 404 Resource not found"))

        crawlerService.crawl(baseUrl = "http://www.example.com")

        assertEquals(setOf("http://www.example.com"), crawlerService.readVisitedLinksView())
    }
}