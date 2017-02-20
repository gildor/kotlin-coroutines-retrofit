package ru.gildor.coroutines.retrofit

import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * @return Result of request or throw exception
 */
suspend fun <T> Call<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T>) {
                if (response.isSuccessful) {
                    continuation.resume(response.body())
                } else {
                    continuation.resumeWithException(HttpError(response))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                //if (call.isCanceled) return
                /**
                 * It is better just to invoke resumeWithException:
                 * 1. If the coroutine was cancelled, then resumeWithException is just ignored
                 * 2. However, if somebody just invokes Call.cancel(), then we should resume
                 *    continuation that was doing `await` on this call, or it'll await forever.
                 */
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
suspend fun <T> Call<T>.awaitResult(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T>) {
                continuation.resume(
                        if (response.isSuccessful) {
                            Result.Ok(response.body(), response.raw())
                        } else {
                            Result.Error(HttpError(response), response.raw())
                        }
                )
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                //if (call.isCanceled) return
                /** see comment above */
                continuation.resume(Result.Exception(t))
            }
        })

        registerOnCompletion(continuation)
    }
}

private fun Call<*>.registerOnCompletion(continuation: CancellableContinuation<*>) {
    continuation.invokeOnCompletion {
        if (continuation.isCancelled)
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
    }
}

