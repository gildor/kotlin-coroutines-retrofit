/*
 * Copyright 2018 Andrey Mischenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.gildor.coroutines.retrofit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Call
import retrofit2.HttpException
import ru.gildor.coroutines.retrofit.util.MockedCall
import ru.gildor.coroutines.retrofit.util.NullBodyCall
import ru.gildor.coroutines.retrofit.util.errorResponse

private const val DONE = "Done!"

@ExperimentalCoroutinesApi
class CallAwaitTest {
    @Test
    fun asyncSuccess() = runBlocking {
        assertEquals(DONE, MockedCall(DONE).await())
    }

    @Test(expected = HttpException::class)
    fun asyncHttpException() = runBlocking {
        MockedCall<String>(error = HttpException(errorResponse<String>())).await()
    }

    @Test(expected = NullPointerException::class)
    fun asyncNullBody() = runBlocking {
        NullBodyCall<String>().await()
    }

    @Test(expected = IllegalArgumentException::class)
    fun asyncException() = runBlocking {
        MockedCall<String>(exception = IllegalArgumentException("wrong get param")).await()
    }

    @Test
    fun asyncResponseOk() = runBlocking {
        val result = MockedCall(DONE).awaitResponse()
        assertEquals(DONE, result.body())
    }

    @Test
    fun asyncResponseError() = runBlocking {
        val result = MockedCall<String>(error = HttpException(errorResponse<String>(500))).awaitResponse()
        assertEquals(500, result.code())
    }

    @Test
    fun awaitRequestCancel() = runBlocking {
        checkJobCancel { it.await() }
    }

    @Test
    fun awaitResponseRequestCancel() = runBlocking {
        checkJobCancel { it.awaitResponse() }
    }

    @Test
    fun awaitResultRequestCancel() = runBlocking {
        checkJobCancel { it.awaitResult() }
    }

    @Test
    fun requestCancelWithException() = runBlocking {
        checkRequestCancelWithException { it.awaitResponse() }
    }

    @Test
    fun awaitRequestCancelWithException() = runBlocking {
        checkRequestCancelWithException { it.await() }
    }

    @Test
    fun awaitResultCancelWithException() = runBlocking {
        checkRequestCancelWithException { it.awaitResult() }
    }

    @Test(expected = IllegalArgumentException::class)
    fun asyncResponseException() = runBlocking {
        MockedCall<String>(exception = IllegalArgumentException()).awaitResponse()
    }

    @Test
    fun awaitJobCancelWithException() = runBlocking {
        checkJobCancelWithException { it.await() }
    }

    @Test
    fun awaitResponseJobCancelWithException() = runBlocking {
        checkJobCancelWithException { it.awaitResponse() }
    }

    @Test
    fun awaitResultJobCancelWithException() = runBlocking {
        checkJobCancelWithException { it.awaitResult() }
    }

    @Test
    fun asyncResponseNullBody() = runBlocking {
        val result = NullBodyCall<String>().awaitResponse()
        assertNull(result.body())
    }

    @Test
    fun asyncResponseNullableBody() = runBlocking {
        //Check that we can call awaitResponse() on nullable body
        val result = NullBodyCall<String?>().awaitResponse()
        assertNull(result.body())
    }

    @Test
    fun asyncResponseFailure() = runBlocking {
        val exception = IllegalStateException()
        try {
            MockedCall<String>(exception = exception).awaitResult()
        } catch (e: Exception) {
            assertSame(e, exception)
        }
    }

    @Test
    fun asyncResultOk() = runBlocking {
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
    fun asyncResultNullBody() = runBlocking {
        val result = NullBodyCall<String>().awaitResult()
        assertNull(result.getOrNull())
    }

    @Test(expected = NullPointerException::class)
    fun asyncResultNullPointerForNullBody() = runBlocking {
        val result = NullBodyCall<String>().awaitResult()
        assertNull(result.getOrThrow())
    }

    @Test
    fun asyncResultByType() = runBlocking {
        val result = MockedCall(DONE).awaitResult()
        when (result) {
            is ResponseResult -> {
                assertEquals(200, result.response.code())
            }
            is ErrorResult -> fail()
            else -> fail()
        }
    }

    @Test
    fun resultOkTypes() = runBlocking {
        val result = MockedCall(DONE).awaitResult()
        if (result is ResponseResult) {
            // Mocked raw response doesn't contain body, but we can check code
            assertTrue(result.response.isSuccessful)
        }

        assertFalse(result is ErrorResult)
    }

    @Test
    fun resultErrorTypes() = runBlocking {
        val errorResponse = errorResponse<String>(500)
        val httpException = HttpException(errorResponse)
        val errorResult = MockedCall<String>(error = httpException).awaitResult()

        if (errorResult is ResponseResult) {
            assertEquals(500, errorResult.response.code())
        } else {
            fail()
        }

        if (errorResult is ErrorResult) {
            assertEquals(httpException.toString(), errorResult.exception.toString())
        } else {
            fail()
        }
    }

    @Test
    fun resultExceptionTypes() = runBlocking {
        val exception = IllegalStateException()
        val errorResult = MockedCall<String>(exception = exception).awaitResult()

        if (errorResult is ErrorResult) {
            assertEquals(exception, errorResult.exception)
        }

        assertFalse(errorResult is ResponseResult)
    }


    @Test
    fun asyncResultError() = runBlocking {
        val error = HttpException(errorResponse<String>(500))
        val result = MockedCall<String>(error = error).awaitResult()
        when (result) {
            is Result.Error -> {
                assertEquals(error.code(), result.exception.code())
                assertEquals(error.message, result.exception.message)
                assertEquals("Error response 500", result.exception.response().errorBody()?.string())
                assertEquals(500, result.response.code())
            }
            is Result.Ok, is Result.Exception -> fail()
            else -> fail()
        }
    }

    @Test
    fun asyncResultException() = runBlocking {
        val exception = IllegalArgumentException("wrong argument")
        val result = MockedCall<String>(exception = exception).awaitResult()
        when (result) {
            is Result.Exception -> {
                assertEquals(exception::class.java, result.exception::class.java)
                assertEquals(exception.message, result.exception.message)
            }
            is Result.Ok, is Result.Error -> fail()
            else -> fail()
        }
    }

    private fun <T> checkRequestCancelWithException(
        block: suspend (Call<String>) -> T
    ) = runBlocking {
        val request = MockedCall(
            ok = DONE,
            autoStart = false,
            cancelException = IllegalStateException()
        )
        val async = async(Dispatchers.Unconfined, block = { block(request) })
        //We shouldn't crash on cancel exception
        try {
            assertFalse(request.isCanceled)
            async.cancel()
            assertTrue(request.isCanceled)
        } catch (e: Exception) {
            fail()
        }
    }

    private fun <T> checkJobCancelWithException(block: suspend (Call<String>) -> T) = runBlocking {
        val request = MockedCall<String>(
            exception = IllegalArgumentException(),
            autoStart = false
        )
        val result = async(Dispatchers.Unconfined) {
            block(request)
        }
        result.cancel()
        request.start()
        assertTrue(result.isCancelled)
    }

    private fun <T> checkJobCancel(
        block: suspend (Call<String>) -> T
    ) = runBlocking {
        val request = MockedCall(DONE, autoStart = false)
        val async = async(Dispatchers.Unconfined) { block(request) }
        assertFalse(request.isCanceled)
        async.cancel()
        assertTrue(request.isCanceled)
    }

}


