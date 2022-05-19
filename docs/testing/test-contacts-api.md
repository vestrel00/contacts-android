# Contacts API Testing

TODO Complete this docs when implementation is complete
TODO Add a reference to this docs in the README and the blog

This library provides the `TestContacts` and `MockContacts`, which you can use as a substitute to 
your `Contacts` API instance in;

- [black box tests][black-box-testing]; UI instrumentation tests in `androidTest/`
- [white box tests][white-box-testing]; unit & integration tests in `test/`

## UI instrumentation tests

TODO Show usage of `TestContacts`

## Unit & integration tests

TODO Show usage of `MockContacts`

## Production test mode

The `TestContacts` may also be used in your production apps, not just in tests. If you want your 
production app to interact (query, insert, update, delete) with only "test contacts", all you would
need to do is substitute your `Contacts` API instance with an instance of `TestContacts`.

```kotlin
@Singleton
fun provideContactsApi(context: Context): Contacts = if (test) {
    TestContacts(context)
} else {
    Contacts(context)
}
```

> ℹ️ The above code block is just pseudo-code for a dependency injection setup.

For example, if you are building a contacts app, you can add a "test" or "debug" mode such that only
test contacts are;

- returned by query APIs
- updated by update APIs
- inserted by insert APIs
- deleted by delete APIs

When turning off test/debug mode, you can easily delete all test contacts created during the session
and return to normal mode.

[black-box-testing]: https://en.wikipedia.org/wiki/Black-box_testing
[white-box-testing]: https://en.wikipedia.org/wiki/White-box_testing