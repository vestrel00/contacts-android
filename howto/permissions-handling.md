# Permissions handling using coroutines

This library provides extensions in the `permissions` module that allow you to prompt users for 
required permissions before executing a core API function. These extensions use 
[Kotlin Coroutines][coroutines].

For all core API functions that requires certain permissions to be granted (e.g. query, insert,
update, and deletes), there is a corresponding `xxxWithPermission` extension function.

## Using `withPermission` extensions

To perform an query, insert, update, and delete **with permission**,

```kotlin
launch {
    val contactsApi = Contacts(context)
    val query = contactsApi.queryWithPermission()
    val insert = contactsApi.insertWithPermission()
    val update = contactsApi.updateWithPermission()
    val delete = contactsApi.deleteWithPermission()
}
```

For each invocation of `xxxWithPermission`, if the required permission(s) are not yet granted, 
the current coroutine is suspended, user is prompted to grant permissions, and then an operation
instance is returned (which may then be executed to get a result).

If permission(s) are already granted, then an operation instance is returned immediately without
suspending the coroutine and prompting the user for permission.

If permission(s) are not granted, then the operation will immediately fail and the result you get 
is incorrect (usually null or empty when it should not be).

> Prior to Android 6.0 Marshmallow (API level 23), users are NOT prompted for permission at runtime
> because users must already grant all permissions prior to app install.

## Not compatible with Java

Unlike the `core` module, the `permissions` module is not compatible with Java because it requires 
Kotlin Coroutines.

## These extensions are optional

You are free to use the core APIs however you want with whatever libraries or frameworks you want 
that works with Java or use your own DIY solution.

[coroutines]: https://kotlinlang.org/docs/coroutines-overview.html