## How do I setup the Contacts API?

The main library functions are all accessible via the `contacts.core.Contacts` API.

There's no setup required. Just create an instance oc `Contacts` and the world of contacts is at
your disposal =)

In Kotlin,

```kotlin
Contacts(context)
```

in Java,

```java
ContactsFactory.create(context)
```

Instances of `Contacts` are stateless, unless you integrate custom data without using the
`GlobalCustomDataRegistry`.

It's up to you if you want to hold on to instances as a singleton that is injected to your
dependency graph (via something like dagger) or just create instances on demand.

The `context` parameter can come from anywhere; Application, Activity, Fragment, or View. It does
not matter what context you pass in. The API will only use and store the Application context, to
avoid leaks :D

For how to create instances of the `Contacts` API with custom data integration, read
[How to integrate custom data](/howto/howto-integrate-custom-data.md).