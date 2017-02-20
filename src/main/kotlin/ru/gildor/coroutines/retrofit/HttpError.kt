package ru.gildor.coroutines.retrofit

import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Exception wrapper for Retrofit response with http error
 */
class HttpError(response: Response<*>) : RuntimeException() {
    override val message = "${response.code()}: ${response.message()}"

    val errorBody: ResponseBody = response.errorBody()

    val code = response.code()
}