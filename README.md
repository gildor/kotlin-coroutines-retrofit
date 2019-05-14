# Kotlin Coroutines for Retrofit
[![CircleCI](https://circleci.com/gh/gildor/kotlin-coroutines-retrofit.svg?style=shield)](https://circleci.com/gh/gildor/kotlin-coroutines-retrofit)
[![codecov](https://codecov.io/gh/gildor/kotlin-coroutines-retrofit/branch/master/graph/badge.svg)](https://codecov.io/gh/gildor/kotlin-coroutines-retrofit)
[![codebeat badge](https://codebeat.co/badges/ccb15073-b84c-4f7d-a0fc-1bdba2b0e435)](https://codebeat.co/projects/github-com-gildor-kotlin-coroutines-retrofit-master)


This is a small library that provides the [Kotlin Coroutines](https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md) [suspending](https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md#suspending-functions) extension `Call.await()` for [Retrofit 2](https://github.com/square/retrofit)

Based on [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) implementation.

New version of library (after 1.0.0) support only Kotlin 1.3 

Kotlin 1.2 and experimental coroutines are not supported anymore, but you can use version `0.13.0` for old projects.

## Download

Download the [JAR](https://bintray.com/gildor/maven/kotlin-coroutines-retrofit#files/ru/gildor/coroutines/kotlin-coroutines-retrofit):

Gradle:

```groovy
implementation 'ru.gildor.coroutines:kotlin-coroutines-retrofit:1.1.0'
```

Maven:

```xml
<dependency>
  <groupId>ru.gildor.coroutines</groupId>
  <artifactId>kotlin-coroutines-retrofit</artifactId>
  <version>1.1.0</version>
</dependency>
```

## How to use


> NOTE: All examples in this README use [`runBlocking`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/run-blocking.html) to build coroutine but it is only useful for testing or examples.
>
> For a real application you probably want to use some other coroutines builder that does not block a thread, for example [`launch`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/launch.html) from [kotlinx.coroutines](https://github.com/kotlin/kotlinx.coroutines).
> 
> If you want to use this library for UI please also check the [Guide to UI programming with coroutines](https://github.com/Kotlin/kotlinx.coroutines/blob/master/ui/coroutines-guide-ui.md)

There are three suspending extensions:

### `.await()`

Common await API that returns a result or throws an exception
```kotlin
fun Call<T>.await(): T
```

In case of an HTTP error or an invocation exception `await()` throws an exception

```kotlin
// You can use retrofit suspended extension inside any coroutine block
fun main(args: Array<String>): Unit = runBlocking {
    try {
        // Wait (suspend) for result
        val user: User = api.getUser("username").await()
        // Now we can work with result object
        println("User ${user.name} loaded")
    } catch (e: HttpException) {
        // Catch http errors
        println("exception${e.code()}", e)
    } catch (e: Throwable) {
        // All other exceptions (non-http)
        println("Something broken", e)
    }
}
```

### `.awaitResponse()`

Common await API that returns a [Response](https://square.github.io/retrofit/2.x/retrofit/retrofit2/Response.html) or throws an exception
```kotlin
fun Call<T>.awaitResponse(): Response<T>
```

In case of an invocation exception `awaitResponse()` throws an exception

```kotlin
// You can use retrofit suspended extension inside any coroutine block
fun main(args: Array<String>): Unit = runBlocking {
    try {
        // Wait (suspend) for response
        val response: Response<User> = api.getUser("username").awaitResponse()
        if (response.isSuccessful()) {
          // Now we can work with response object
          println("User ${response.body().name} loaded")
        }
    } catch (e: Throwable) {
        // All other exceptions (non-http)
        println("Something broken", e)
    }
}
```

### `.awaitResult()`

API based on sealed class [Result](src/main/kotlin/ru/gildor/coroutines/retrofit/Result.kt):

```kotlin
fun Call<T>.awaitResult(): Result<T>
```

```kotlin
fun main(args: Array<String>): Unit = runBlocking {
    // Wait (suspend) for Result
    val result: Result<User> = api.getUser("username").awaitResult()
    // Check result type
    when (result) {
        //Successful HTTP result
        is Result.Ok -> saveToDb(result.value)
        // Any HTTP error
        is Result.Error -> log("HTTP error with code ${result.error.code()}", result.error)
        // Exception while request invocation
        is Result.Exception -> log("Something broken", e)
    }
}
```

Also, `Result` has a few handy extension functions that allow to avoid `when` block matching:

```kotlin
fun main(args: Array<String>): Unit = runBlocking {
    val result: User = api.getUser("username").awaitResult()
    
    //Return value for success or null for any http error or exception
    result.getOrNull()
    
    //Return result or default value
    result.getOrDefault(User("empty-user"))
    
    //Return value or throw exception (HttpException or original exception)
    result.getOrThrow()
    //Also supports custom exceptions to override original ones
    result.getOrThrow(IlleagalStateException("User request failed"))
}
```

All `Result` classes also implemented one or both interfaces: `ResponseResult` and `ErrorResult`
You can use them for access to shared properties of different classes from `Result`
 
```kotlin
fun main(args: Array<String>): Unit = runBlocking {
  val result: User = api.getUser("username").awaitResult()
  
  //Result.Ok and Result.Error both implement ResponseResult
  if (result is ResponseResult) {
      //And after smart cast you now have an access to okhttp3 Response property of result
      println("Result ${result.response.code()}: ${result.response.message()}")
  }
  
  //Result.Error and Result.Exception implement ErrorResult
  if (result is ErrorResult) {
      // Here yoy have an access to `exception` property of result
      throw result.exception
  }
}
```

## Nullable body

To prevent unexpected behavior with a nullable body of response `Call<Body?>`
extensions `.await()` and `.awaitResult()` are available only for 
non-nullable `Call<Body>` or platform `Call<Body!>` body types:

```kotlin
fun main(args: Array<String>): Unit = runBlocking {
  val user: Call<User> = api.getUser("username")
  val userOrNull: Call<User?> = api.getUserOrNull("username")
  
  // Doesn't work, because User is nullable
  // userOrNull.await()
    
  // Works for non-nullable type
  try {
      val result: User = user.await()  
  } catch (e: NullPointerException) {
      // If body will be null you will get NullPointerException
  }
  
  // You can use .awaitResult() to catch possible problems with nullable body
  val nullableResult = api.getUser("username").awaitResult().getOrNull()
  // But type of body should be non-nullable
  // api.getUserOrNull("username").awaitResult()
  
  // If you still want to use nullable body to clarify your api
  // use awaitResponse() instead:
  val responseBody: User? = userOrNull.awaitResponse().body()
}
``` 

## Parallel requests

By wrapping call with `kotlinx.coroutines` [async()](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/async.html), you may run a few requests parallelly without waiting for the previous request.


```kotlin
fun main(args: Array<String>): Unit = runBlocking {
  val users = listOf("user1", "user2", "user3")
      .map { username ->
        // Pass any coroutine context that fits better for your case
        // Coroutine Dispatcher also controls parallelism level 
        // for CommonPool parallelism is `availableProcessors - 1`
        // But you can use any custom dispatcher with any parallelism strategy
        async(CommonPool) {
            // Send request. We use `awaitResult()` here to avoid try/catch, 
            // but you can use `await()` and catch exceptions
            api.getUser(username).awaitResult() 
        }
      }
      // Handle results
      // in this example we get result or null in case of error and filter all nulls
      .mapNotNull {
        // Wait (suspend) for result of `async()` and get result of request
        // We must call first `await()` only when all `async` blocks are created for parallel requests
        it.await().getOrNull()
      }
}
``` 

You can read more about concurrent usage of async in the [kotlinx.coroutines guide](https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md#concurrent-using-async)
