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

import okhttp3.Response
import retrofit2.HttpException

/**
 * Sealed class of HTTP result
 */
@Suppress("unused")
public sealed class Result<out T : Any> {
    /**
     * Successful result of request without errors
     */
    public class Ok<out T : Any>(
        public val value: T,
        override val response: Response
    ) : Result<T>(), ResponseResult {
        override fun toString() = "Result.Ok{value=$value, response=$response}"
    }

    /**
     * HTTP error
     */
    public class Error(
        override val exception: HttpException,
        override val response: Response
    ) : Result<Nothing>(), ErrorResult, ResponseResult {
        override fun toString() = "Result.Error{exception=$exception}"
    }

    /**
     * Network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response
     */
    public class Exception(
        override val exception: Throwable
    ) : Result<Nothing>(), ErrorResult {
        override fun toString() = "Result.Exception{$exception}"
    }

}

/**
 * Interface for [Result] classes with [okhttp3.Response]: [Result.Ok] and [Result.Error]
 */
public interface ResponseResult {
    val response: Response
}

/**
 * Interface for [Result] classes that contains [Throwable]: [Result.Error] and [Result.Exception]
 */
public interface ErrorResult {
    val exception: Throwable
}

/**
 * Returns [Result.Ok.value] or `null`
 */
public fun <T : Any> Result<T>.getOrNull(): T? = (this as? Result.Ok)?.value

/**
 * Returns [Result.Ok.value] or [default]
 */
public fun <T : Any> Result<T>.getOrDefault(default: T): T = getOrNull() ?: default

/**
 * Returns [Result.Ok.value] or throw [throwable] or [ErrorResult.exception]
 */
public fun <T : Any> Result<T>.getOrThrow(throwable: Throwable? = null): T {
    return when (this) {
        is Result.Ok -> value
        is Result.Error -> throw throwable ?: exception
        is Result.Exception -> throw throwable ?: exception
    }
}
