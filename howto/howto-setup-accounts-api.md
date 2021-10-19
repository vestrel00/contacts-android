## How do I setup the Accounts API?

There are extra library functions that revolve around `android.accounts.Account`. The functions are 
all accessible via the `contacts.core.accounts.Accounts` API.

There's no setup required. Just create an instance of `Accounts` and it is ready for use.

In Kotlin,

```kotlin
Accounts(context)
```

in Java,

```java
AccountsFactory.create(context);
```

It's up to you if you just want to create instances on demand or hold on to instances as a singleton
that is injected to your dependency graph (via something like dagger) to easily mock it during tests.

The `context` parameter can come from anywhere; Application, Activity, Fragment, or View. It does
not matter what context you pass in. The API will only use and store the Application context, to
avoid leaks :D

Instances of `Accounts` are stateless.


> The main functions of this library are about contacts. For more info, read
> [How do I setup the Contacts API?](/howto/howto-setup-contacts-api.md)