package ru.gildor.coroutines.retrofit

import org.junit.Assert.*
import org.junit.Test
import ru.gildor.coroutines.retrofit.util.errorResponse
import ru.gildor.coroutines.retrofit.util.okHttpResponse

class ResultTest {
    private val result = "result"
    private val default = "default"
    private val ok = Result.Ok(result, okHttpResponse())
    private val error = Result.Error(HttpError(errorResponse()), okHttpResponse(401))
    private val exception = Result.Exception(IllegalArgumentException())
    @Test
    fun getOrNull() {
        assertEquals(result, ok.getOrNull())
        assertNull(error.getOrNull())
        assertNull(exception.getOrNull())
    }

    @Test
    fun getOrDefault() {
        assertEquals(result, ok.getOrDefault(default))
        assertEquals(default, error.getOrDefault(default))
        assertEquals(default, exception.getOrDefault(default))
    }

    @Test
    fun getOrThrowOk() {
        assertEquals(result, ok.getOrThrow())
    }

    @Test(expected = HttpError::class)
    fun getOrThrowError() {
        error.getOrThrow()
    }

    @Test(expected = IllegalArgumentException::class)
    fun getOrThrowException() {
        exception.getOrThrow()
    }

    @Test(expected = IllegalStateException::class)
    fun getOrThrowCustomException() {
        exception.getOrThrow(IllegalStateException("Custom!"))
    }
}