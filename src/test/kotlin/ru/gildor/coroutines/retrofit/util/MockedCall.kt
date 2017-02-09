package ru.gildor.coroutines.retrofit.util

import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.gildor.coroutines.retrofit.HttpError

class MockedCall(
        val ok: String? = null,
        val error: HttpError? = null,
        val exception: Throwable? = null
) : Call<String> {
    var executed: Boolean = false
    var cancelled: Boolean = false

    override fun execute(): Response<String> {
        markAsExecuted()
        return when {
            ok != null -> Response.success(ok)
            error != null -> errorResponse(error.code)
            exception != null -> throw exception
            else -> throw IllegalStateException("Wrong MockedCall state")
        }
    }

    override fun enqueue(callback: Callback<String>) {
        markAsExecuted()
        when {
            ok != null -> callback.onResponse(this, Response.success(ok))
            error != null -> callback.onResponse(this, errorResponse(error.code))
            exception != null -> callback.onFailure(this, exception)
        }
    }

    override fun isCanceled() = cancelled

    override fun isExecuted() = executed

    override fun clone(): Call<String> = throw IllegalStateException("Not mocked")

    override fun request(): Request = throw IllegalStateException("Not mocked")

    private fun markAsExecuted() {
        if (executed) throw IllegalStateException("Request already executed")
        executed = true
    }

    override fun cancel() {
        cancelled = true
    }

}

fun errorResponse(code: Int = 400, message: String = "Error response $code"): Response<String> =
        Response.error(code, ResponseBody.create(MediaType.parse("text/plain"), message))

fun okHttpResponse(code: Int = 200): okhttp3.Response = okhttp3.Response.Builder()
        .code(code)
        .protocol(Protocol.HTTP_1_1)
        .request(Request.Builder().url("http://localhost").build())
        .build()