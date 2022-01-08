# How do I setup the Contacts API?

The main library functions are all accessible via the `contacts.core.Contacts` API.

There's no setup required. Just create an instance of `Contacts` and the world of contacts is at
your disposal =)

In Kotlin,

```kotlin
Contacts(context)
```

in Java,

```java
ContactsFactory.create(context);
```

> The `context` parameter can come from anywhere; Application, Activity, Fragment, or View. It does
> not matter what context you pass in. The API will only use and store the Application context, to
> avoid leaks :D

It's up to you if you just want to create instances on demand. Or, hold on to instances as a 
[singleton][singleton] that is injected to your dependency graph (via something like 
[dagger][dagger], [hilt][hilt], or [koin][koin]), which will make 
[black box testing][black-box-testing] and [white box testing][white-box-testing] a walk in the
park!

## Logging support

Instances of `Contacts` hold on to a `Logger` for logging support.

For more info, read [How do I log API input and output?](/contacts-android/howto/howto-log-api-input-output.html)

## Custom data integration

Instances of `Contacts` hold on to an instance of `CustomDataRegistry` for custom data integration.

For more info, read [How do I integrate custom data?](/contacts-android/howto/howto-integrate-custom-data.html)

## Optional, but recommended setup

It is recommended to use a single instance of the `Contacts` API throughout your application using 
[dependency injection][di]. This will allow you to;

1. Use the same `Contacts` API instance throughout your app.
    - This especially important when integrating custom data.
2. Easily substitute your `Contacts` API instance with an instance of `TestContacts`.
    - This is useful in [black box testing][black-box-testing] (UI instrumentation tests; `androidTest/`).
    - It may also be used in your production apps "test mode".
3. Easily substitute your `Contacts` API instance with an instance of `MockContacts`
    - This is useful in [white box testing][white-box-testing] (unit & integration tests; `test/`).
    
For more info, read [How do I use the test module to simplify testing in my app?](/contacts-android/howto/howto-use-api-for-testing.html)
    
> Of course, this library does not (and will not) force you to do things you don't want. If you 
> don't care about all of the above and just want to get out a quick prototype of a feature in your 
> app or an entire app, then go right ahead! 

[singleton]: https://en.wikipedia.org/wiki/Singleton_pattern
[dagger]: https://developer.android.com/training/dependency-injection/dagger-android
[hilt]: https://developer.android.com/training/dependency-injection/hilt-android
[koin]: https://insert-koin.io
[black-box-testing]: https://en.wikipedia.org/wiki/Black-box_testing
[white-box-testing]: https://en.wikipedia.org/wiki/White-box_testing
[di]: https://developer.android.com/training/dependency-injection
