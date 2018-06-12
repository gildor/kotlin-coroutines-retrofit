# CHANGELOG

## Version 0.11.0 (2017-06-12)

- [kotlinx.coroutines 0.23.1](https://github.com/Kotlin/kotlinx.coroutines/releases/)
- Compiled against Kotlin 1.2.41

Thanks to [Thomas Schmidt](https://github.com/bohsen) for contribution

## Version 0.10.0 (2017-04-26)

- [Retrofit 2.4.0](https://github.com/square/retrofit/blob/parent-2.4.0/CHANGELOG.md#version-240-2018-03-14)
- [kotlinx.coroutines 0.22.5](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/0.22.5)
- Compiled against Kotlin 1.2.40
- Migration of build.gradle to kotlin-dsl
- Gradle 4.7
- Published proper javadoc

## Version 0.9.0 (2017-12-26)

- [kotlinx.coroutines 0.20](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/0.20)
- Compiled against Kotlin 1.2.10

## Version 0.8.2 (2017-10-04)

- Fixed Kotlin stdlib dependency version in pom.xml

## Version 0.8.1 (2017-10-02)

- [kotlinx.coroutines 0.19](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/0.19)
- Compiled against Kotlin 1.1.51

## Version 0.7.1 (2017-07-28)

The previous version was accidentally released with kotlinx.coroutines 0.16 instead 0.17
Thanks to @rciurkot for PR #25 that fixes that. 

- [kotlinx.coroutines 0.17](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/0.17)

## Version 0.7.0 (2017-07-24)

- Compiled against Kotlin 1.1.3-2

## Version 0.6.0 (2017-07-04)

Notice: This release backward incompatible with previous versions:

- `.await()` and `.awaitResult()` can be used now only with non-nullable types of result 
and throw NullPointerException in case of null body. 
See [examples in Readme](README.md#Nullable body)
- [#13](https://github.com/gildor/kotlin-coroutines-retrofit/issues/13) `.toString()` for `Result` classes

## Version 0.5.1 (2017-06-27)

- [Retrofit 2.3.0](https://github.com/square/retrofit/blob/parent-2.3.0/CHANGELOG.md#version-230-2017-05-13)
- [kotlinx.coroutines 0.16](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/0.16)
- Compiled against Kotlin 1.1.3

## Version 0.5.0 (2017-04-01)

- [kotlinx.coroutines 0.14](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/0.14)
- Compiled against Kotlin 1.1.1

## Version 0.4.1 (2017-03-05)

- [#8](https://github.com/gildor/kotlin-coroutines-retrofit/issues/8) (PR [#9](https://github.com/gildor/kotlin-coroutines-retrofit/pull/9)) Call cancellation feedback causes crash when coroutine is cancelled

## Version 0.4.0 (2017-03-01)

- Kotlin 1.1
- [kotlinx.coroutines 0.12](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/0.12)

## Version 0.3.0 (2017-02-25)

- [Retrofit 2.2.0](https://github.com/square/retrofit/blob/parent-2.2.0/CHANGELOG.md#version-220-2017-02-21). Also it is minimum supported version
- [kotlinx.coroutines 0.11-rc](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/0.11-rc)
- Custom HttpError class replaced with [HttpException](https://github.com/square/retrofit/blob/parent-2.2.0/retrofit/src/main/java/retrofit2/HttpException.java) from Retrofit 2.2

## Version 0.2.0 (2017-02-20)

- Depends on Retrofit 2.1.0
- Result classes now implement one or two of interfaces: ResponseResult and ResponseError. It allows simplify access to results in some cases
- Moved to the separate repo
- Kotlin 1.1.0-rc-91
- kotlinx.coroutines 0.10-rc
- JCenter and Maven publish configs
- Updated examples in readme
