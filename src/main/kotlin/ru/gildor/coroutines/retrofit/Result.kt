package ru.gildor.coroutines.retrofit

import okhttp3.Response

/**
 * Sealed class of Http result
 */
@Suppress("unused")
sealed class Result<out T> {
    /**
     * Successful result of request without errors
     */
    class Ok<out T>(
            val value: T,
            override val response: Response
    ) : Result<T>(), ResponseResult

    /**
     * HTTP error
     */
    class Error(
            override val exception: HttpError,
            override val response: Response
    ) : Result<Nothing>(), ErrorResult, ResponseResult

    /**
     * Network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response
     */
    class Exception(
            override val exception: Throwable
    ) : Result<Nothing>(), ErrorResult

}

/**
 * Interface for [Result] classes with [okhttp3.Response]: [Result.Ok] and [Result.Error]
 */
interface ResponseResult {
    val response: Response
}

/**
 * Interface for [Result] classes that contains [Throwable]: [Result.Error] and [Result.Exception]
 */
interface ErrorResult {
    val exception: Throwable
}

/**
 * Returns [Result.Ok.value] or `null`
 */
fun <T> Result<T>.getOrNull() =
        if (this is Result.Ok) this.value else null

/**
 * Returns [Result.Ok.value] or [default]
 */
fun <T> Result<T>.getOrDefault(default: T) =
        getOrNull() ?: default

/**
 * Returns [Result.Ok.value] or throw [throwable] or [ErrorResult.exception]
 */
fun <T> Result<T>.getOrThrow(throwable: Throwable? = null): T {
    return when (this) {
        is Result.Ok -> value
        is Result.Error -> throw throwable ?: exception
        is Result.Exception -> throw throwable ?: exception
    }
}
