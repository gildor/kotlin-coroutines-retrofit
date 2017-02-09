# Kotlin Coroutines for Retrofit

This is small library that provides await() extensions for Retrofit 2

There is two different API:

```kotlin
fun Call<T>.await(): T
```

In case of HTTP error or invocation exception `await()` throws exception

```kotlin
// Any coroutine implementation
launch(CoroutineContext) {
    try {
        // Wait (suspend) for response
        val user: User = api.getUser("username").await()
        // Now we can work with result object
        saveToDb(user)
        log("User ${user.name} saved")
    } catch (e: HttpError) {
        // Catch any error HTTP statuses 
        log("HTTP error with code ${e.code}", e)
    } catch (e: Throwable) {
        // All other exception while request invocation
        log("Something broken", e)
    }
}
```

API based on sealed class `Result`:

```kotlin
fun Call<T>.await(): Result<T>
```

```kotlin
launch(CoroutineContext) {
        // Wait (suspend) for Result
        val result: User = api.getUser("username").awaitResult()
        // Check result type
        when (result) {
            //Successful HTTP result
            is Result.Ok -> saveToDb(result.value)
            // Any HTTP error
            is Result.Error -> log("HTTP error with code ${result.error.code}", result.error)
            // Exception while request invocation
            is Result.Exception -> log("Something broken", e)
        }
}
```

Also, `Result` has a few handy extension functions that allow to avoid `when` block matching:

```kotlin
launch(CoroutineContext) {
        val result: User = api.getUser("username").awaitResult()
        
        //Return value for success or null for any http error or exception
        result.getOrNull()
        
        //Return result or default value
        result.getOrDefault(User("empty-user"))
        
        //Return value or throw exception (HttpError or original exception)
        result.getOrThrow()
        //Also supports custom exceptions to override original ones
        result.getOrThrow(IlleagalStateException("User request failed"))
}
```