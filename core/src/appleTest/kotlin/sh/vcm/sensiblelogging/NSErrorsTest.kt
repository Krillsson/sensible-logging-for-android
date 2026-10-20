package sh.vcm.sensiblelogging

import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class NSErrorsTest {

    @Test
    internal fun `should wrap a plain NSError`() {
        // GIVEN
        val error = NSError.errorWithDomain("MoniteeiOS.NetworkError", 2, mapOf<Any?, Any?>(NSLocalizedDescriptionKey to "Timed out"))

        // WHEN
        val throwable = error.asThrowable()

        // THEN
        val exception = assertIs<NSErrorException>(throwable)
        assertSame(error, exception.error)
        assertEquals("Timed out", exception.message)
    }

    @Test
    internal fun `should return the original throwable of an NSError created from a kotlin exception`() {
        // GIVEN
        val original = IllegalStateException("boom")
        val error = NSError.errorWithDomain(
            "KotlinException",
            0,
            mapOf<Any?, Any?>("KotlinException" to original, NSLocalizedDescriptionKey to "boom")
        )

        // WHEN
        val throwable = error.asThrowable()

        // THEN
        assertSame(original, throwable)
    }

    @Test
    internal fun `should include domain and code when printed`() {
        // GIVEN
        val error = NSError.errorWithDomain("MoniteeiOS.NetworkError", 2, mapOf<Any?, Any?>(NSLocalizedDescriptionKey to "Timed out"))

        // WHEN
        val result = NSErrorException(error).toString()

        // THEN
        assertEquals("NSErrorException(domain=MoniteeiOS.NetworkError, code=2): Timed out", result)
    }
}
