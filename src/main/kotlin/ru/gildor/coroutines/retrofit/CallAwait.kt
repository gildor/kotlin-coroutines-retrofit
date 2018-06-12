package ru.gildor.coroutines.retrofit

import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * @return Result of request or throw exception
 */
public suspend fun <T : Any> Call<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T?>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        continuation.resumeWithException(
                                NullPointerException("Response body is null: $response")
                        )
                    } else {
                        continuation.resume(body)
                    }
                } else {
                    continuation.resumeWithException(HttpException(response))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                continuation.resumeWithException(t)
            }
        })

        registerOnCompletion(continuation)
    }
}

/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * @return Response for request or throw exception
 */
public suspend fun <T : Any?> Call<T>.awaitResponse(): Response<T> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T>) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                continuation.resumeWithException(t)
            }
        })

        registerOnCompletion(continuation)
    }
}

/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * @return sealed class [Result] object that can be
 *         casted to [Result.Ok] (success) or [Result.Error] (HTTP error) and [Result.Exception] (other errors)
 */
public suspend fun <T : Any> Call<T>.awaitResult(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T>) {
                continuation.resume(
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body == null) {
                                Result.Exception(NullPointerException("Response body is null"))
                            } else {
                                Result.Ok(body, response.raw())
                            }
                        } else {
                            Result.Error(HttpException(response), response.raw())
                        }
                )
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                continuation.resume(Result.Exception(t))
            }
        })

        registerOnCompletion(continuation)
    }
}

private fun Call<*>.registerOnCompletion(continuation: CancellableContinuation<*>) {
    continuation.invokeOnCancellation {
        if (continuation.isCancelled)
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
    }
}
