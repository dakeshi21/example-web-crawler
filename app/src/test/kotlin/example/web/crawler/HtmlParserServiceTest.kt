package example.web.crawler

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Before
import kotlin.test.Test

internal class HtmlParserServiceTest {

    @Before
    fun setUp() {
        mockkStatic(Jsoup::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(Jsoup::class)
    }

    @Test
    fun `returns all internal links from a web page`() = runBlocking {
        val html = """
            <html>
                <body>
                    <a href="http://www.example.com/child_1">child_1</a>
                    <a href="http://www.example.com/child_2">child_2</a>
                </body>
            </html>
        """.trimIndent()
        val document = Jsoup.parse(html)

        every { Jsoup.connect("http://www.example.com").timeout(30000).get() } returns document

        val htmlParserService = HtmlParserService(domain = "www.example.com")
        val subLinks = htmlParserService.internalLinks(pageUrl = "http://www.example.com")

        subLinks.assertSuccess(
            setOf(
                "http://www.example.com/child_1",
                "http://www.example.com/child_2"
            )
        )
    }

    @Test
    fun `returns an empty list when page doesn't contain any links`() = runBlocking {
        val html = """
            <html>
                <body>
                    <p>Empty Page</p>
                </body>
            </html>
        """.trimIndent()
        val document = Jsoup.parse(html)

        every { Jsoup.connect("http://www.example.com").timeout(30000).get() } returns document

        val htmlParserService = HtmlParserService(domain = "www.example.com")
        val subLinks = htmlParserService.internalLinks(pageUrl = "http://www.example.com")

        subLinks.assertSuccess(emptySet())
    }

    @Test
    fun `returns an empty list when page contains only external links`() = runBlocking {
        val html = """
            <html>
                <body>
                    <a href="https://www.facebook.com">Facebook</p>
                    <a href="https://www.twitter.com">Twitter</p>
                </body>
            </html>
        """.trimIndent()
        val document = Jsoup.parse(html)

        every { Jsoup.connect("http://www.example.com").timeout(30000).get() } returns document

        val htmlParserService = HtmlParserService(domain = "www.example.com")
        val subLinks = htmlParserService.internalLinks(pageUrl = "http://www.example.com")

        subLinks.assertSuccess(emptySet())
    }
}
