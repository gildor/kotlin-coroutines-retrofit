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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import ru.gildor.coroutines.retrofit.util.errorResponse
import ru.gildor.coroutines.retrofit.util.okHttpResponse

class ResultTest {
    private val result = "result"
    private val default = "default"
    private val ok = Result.Ok(result, okHttpResponse())
    private val error = Result.Error(HttpException(errorResponse<Nothing>()), okHttpResponse(401))
    private val exception = Result.Exception(IllegalArgumentException("Exception message"))
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

    @Test(expected = HttpException::class)
    fun getOrThrowError() {
        error.getOrThrow()
    }

    @Test(expected = IllegalArgumentException::class)
    fun getOrThrowException() {
        exception.getOrThrow()
    }

    @Test(expected = IllegalStateException::class)
    fun getOrThrowErrorWithCustomException() {
        error.getOrThrow(IllegalStateException("Custom!"))
    }

    @Test(expected = IllegalStateException::class)
    fun getOrThrowExceptionCustomException() {
        exception.getOrThrow(IllegalStateException("Custom!"))
    }

    @Test
    fun okToString() {
        assertEquals(
            "Result.Ok{value=result, response=Response{protocol=http/1.1, code=200, message=mock response, url=http://localhost/}}",
            ok.toString()
        )
    }

    @Test
    fun errorToString() {
        assertEquals(
            "Result.Error{exception=retrofit2.HttpException: HTTP 400 Response.error()}",
            error.toString()
        )
    }

    @Test
    fun exceptionToString() {
        assertEquals(
            "Result.Exception{java.lang.IllegalArgumentException: Exception message}",
            exception.toString()
        )
    }
}