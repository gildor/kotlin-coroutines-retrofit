package ru.gildor.coroutines.retrofit

import okhttp3.ResponseBody
import retrofit2.Response

class HttpError(response: Response<*>) : RuntimeException() {
    override val message = "${response.code()}: ${response.message()}"

    val errorBody: ResponseBody = response.errorBody()

    val code = response.code()
}