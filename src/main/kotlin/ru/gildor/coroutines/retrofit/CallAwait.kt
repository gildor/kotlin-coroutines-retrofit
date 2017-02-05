package ru.gildor.coroutines.retrofit

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
                    continuation.resumeWithException(HttpException(response))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                if (call.isCanceled) return
                continuation.resumeWithException(t)
            }
        })

        continuation.onCompletion {
            if (continuation.isCancelled)
                try {
                    cancel()
                } catch (ex: Throwable) {
                    //Ignore cancel exception
                }
        }
    }
}

class HttpException(private val response: Response<*>) : RuntimeException() {
    override val message: String?
        get() = "Status ${response.code()}: ${response.message()}"
}