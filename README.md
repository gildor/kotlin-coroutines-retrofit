# Kotlin Coroutines for Retrofit

This is small library that provides  [Kotlin Coroutines](https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md) [suspending](https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md#suspending-functions) extension `Call.await()` for [Retrofit 2](https://github.com/square/retrofit)

Based on [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) implementation

## Download
Download the [JAR](https://bintray.com/gildor/maven/kotlin-coroutines-retrofit#files/ru/gildor/coroutines/kotlin-coroutines-retrofit):

Gradle:

```groovy
compile 'ru.gildor.coroutines:kotlin-coroutines-retrofit:0.5.1'
```

Maven:

```xml
<dependency>
  <groupId>ru.gildor.coroutines</groupId>
  <artifactId>kotlin-coroutines-retrofit</artifactId>
  <version>0.5.0</version>
</dependency>
```


## How to use
There are three suspending extensions:

### `.await()`

Common await API that returns result or throw exception
```kotlin
fun Call<T>.await(): T
```

In case of HTTP error or invocation exception `await()` throws exception

```kotlin
// You can use retrofit suspended extension inside any coroutine block
fun main(args: Array<String>) = runBlocking {
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

Common await API that returns response or throw exception
```kotlin
fun Call<T>.awaitResponse(): Response<T>
```

In case of invocation exception `awaitResponse()` throws exception

```kotlin
// You can use retrofit suspended extension inside any coroutine block
fun main(args: Array<String>) = runBlocking {
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

API based on sealed class `Result`:

```kotlin
fun Call<T>.awaitResult(): Result<T>
```

```kotlin
fun main(args: Array<String>) = runBlocking {
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
fun main(args: Array<String>) = runBlocking {
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
fun main(args: Array<String>) = runBlocking {
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
