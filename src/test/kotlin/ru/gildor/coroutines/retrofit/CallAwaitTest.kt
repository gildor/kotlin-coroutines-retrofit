package ru.gildor.coroutines.retrofit

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.*
import org.junit.Test
import ru.gildor.coroutines.retrofit.util.MockedCall
import ru.gildor.coroutines.retrofit.util.errorResponse

private const val DONE = "Done!"

class CallAwaitTest {
    @Test
    fun asyncSuccess() = testBlocking {
        assertEquals(DONE, MockedCall(DONE).await())
    }

    @Test(expected = HttpError::class)
    fun asyncHttpError() = testBlocking {
        MockedCall(error = HttpError(errorResponse())).await()
    }

    @Test(expected = IllegalArgumentException::class)
    fun asyncException() = testBlocking {
        MockedCall(exception = IllegalArgumentException("wrong get param")).await()
    }

    @Test
    fun asyncResultOk() = testBlocking {
        val result = MockedCall(DONE).awaitResult()
        when (result) {
            is Result.Ok -> {
                assertEquals(DONE, result.value)
                assertEquals(200, result.response.code())
            }
            is Result.Error, is Result.Exception -> fail()
            else -> fail()

        }
    }

    @Test
    fun asyncResultError() = testBlocking {
        val error = HttpError(errorResponse(500))
        val result = MockedCall(error = error).awaitResult()
        when (result) {
            is Result.Error -> {
                assertEquals(error.code, result.error.code)
                assertEquals(error.message, result.error.message)
                assertEquals("Error response 500", result.error.errorBody.string())
                assertEquals(500, result.response.code())
            }
            is Result.Ok, is Result.Exception -> fail()
            else -> fail()
        }
    }

    @Test
    fun asyncResultException() = testBlocking {
        val exception = IllegalArgumentException("wrong argument")
        val result = MockedCall(exception = exception).awaitResult()
        when (result) {
            is Result.Exception -> {
                assertEquals(exception.javaClass, result.exception.javaClass)
                assertEquals(exception.message, result.exception.message)
            }
            is Result.Ok, is Result.Error -> fail()
            else -> fail()
        }
    }

}

private fun testBlocking(block: suspend CoroutineScope.() -> Unit) {
    runBlocking(Unconfined, block)
}