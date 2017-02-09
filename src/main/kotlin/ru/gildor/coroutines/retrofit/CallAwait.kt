package ru.gildor.coroutines.retrofit

import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                if (call.isCanceled) return
                continuation.resumeWithException(t)
            }
        })

        registerOnCompletion(continuation)
    }
}

suspend fun <T> Call<T>.awaitResult(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T>) {
                continuation.resume(
                        if (response.isSuccessful) {
                            Result.Ok(response.body(), response.raw())
                        } else {
                            Result.Error<T>(HttpError(response), response.raw())
                        }
                )
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                if (call.isCanceled) return
                continuation.resume(Result.Exception(t))
            }
        })

        registerOnCompletion(continuation)
    }
}

private fun Call<*>.registerOnCompletion(continuation: CancellableContinuation<*>) {
    continuation.onCompletion {
        if (continuation.isCancelled)
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
    }
}
