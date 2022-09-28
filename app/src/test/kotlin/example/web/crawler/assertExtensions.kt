package example.web.crawler

import kotlin.test.assertEquals

fun <T> Result<T>.assertSuccess(expected: T) {
    val actual = this.getOrThrow()
    assertEquals(expected, actual)
}