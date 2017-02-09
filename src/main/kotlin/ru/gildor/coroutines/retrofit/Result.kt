package ru.gildor.coroutines.retrofit

import okhttp3.Response

sealed class Result<T> {
    class Ok<T>(val value: T, val response: Response) : Result<T>()
    class Error<T>(val error: HttpError, val response: Response) : Result<T>()
    class Exception<T>(val exception: Throwable) : Result<T>()
}

fun <T> Result<T>.getOrNull() =
        if (this is Result.Ok) this.value else null

fun <T> Result<T>.getOrDefault(default: T) =
        getOrNull() ?: default

fun <T> Result<T>.getOrThrow(throwable: Throwable? = null): T {
    when (this) {
        is Result.Ok -> return value
        is Result.Error -> throw throwable ?: error
        is Result.Exception -> throw throwable ?: exception
    }
}
