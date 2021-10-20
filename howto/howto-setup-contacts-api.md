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

It's up to you if you just want to create instances on demand or hold on to instances as a singleton
that is injected to your dependency graph (via something like dagger) to easily mock it during tests.

The `context` parameter can come from anywhere; Application, Activity, Fragment, or View. It does
not matter what context you pass in. The API will only use and store the Application context, to
avoid leaks :D

Instances of `Contacts` are stateless, unless you integrate custom data without using the
`GlobalCustomDataRegistry`. For how to create instances of the `Contacts` API with custom data
integration, read [How do I integrate custom data?](/contacts-android/howto/howto-integrate-custom-data.html)

> This library also provides an API for accounts related stuff. For more info, read 
> [How do I setup the Accounts API?](/contacts-android/howto/howto-setup-accounts-api.html)