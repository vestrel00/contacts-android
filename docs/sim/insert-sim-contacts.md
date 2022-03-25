# Insert contacts into SIM card

This library provides the `SimContactsInsert` API that allows you to create/insert contacts into
the SIM card.

An instance of the `SimContactsInsert` API is obtained by,

```kotlin
val insert = Contacts(context).sim().insert()
```

## A basic insert

To create/insert a new contact into the SIM card,

```kotlin
val insertResult = Contacts(context)
    .sim()
    .insert()
    .simContact(NewSimContact(name = "Dude", number = "5555555555"))
    .commit()
```

If you need to insert multiple contacts,

```kotlin
val newContact1 = NewSimContact(name = "Dude1", number = "1234567890")
val newContact2 = NewSimContact(name = "Dude2", number = "0987654321")

val insertResult = Contacts(context)
    .sim()
    .insert()
    .simContacts(newContact1, newContact2)
    .commit()
```

## Blank contacts are ignored

Blank contacts (name AND number are both null or blank) will NOT be inserted. The name OR number 
can be null or blank but not both.

## Character limits

The `name` and `number` are subject to the SIM card's maximum character limit, which is typically
around 20-30 characters (in modern times). This may vary per SIM card. Inserts or updates will fail
if the limit is breached.

## Executing the insert

To execute the insert,

```kotlin
.commit()
```

### Handling the insert result

The `commit` function returns a `Result`.

To check if all inserts succeeded,

```kotlin
val allInsertsSuccessful = insertResult.isSuccessful
```

To check if a particular insert succeeded,

```kotlin
val firstInsertSuccessful = insertResult.isSuccessful(newContact1)
```

> The `IccProvider` does not yet return the row ID os newly inserted contacts. Look at the "TODO"
> at line 259 of Android's [IccProvider](https://android.googlesource.com/platform/frameworks/opt/telephony/+/51302ef/src/java/com/android/internal/telephony/IccProvider.java#259).
> Therefore, this library's insert API is does not yet support getting the new rows from the result.

## Cancelling the insert

To cancel an insert amid execution,

```kotlin
.commit { returnTrueIfInsertShouldBeCancelled() }
```

The `commit` function optionally takes in a function that, if it returns true, will cancel insert
processing as soon as possible. The function is called numerous times during insert processing to
check if processing should stop or continue. This gives you the option to cancel the insert.

For example, to automatically cancel the insert inside a Kotlin coroutine when the coroutine is cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val insertResult = insert.commit { !isActive }
    }
}
```

## Performing the insert and result processing asynchronously

Inserts are executed when the `commit` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in
the `async` module. For more info,
read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the insert with permission

Inserts require the `android.permission.WRITE_CONTACTS` permission. If not granted, the insert will
do nothing and return a failed result.

To perform the insert with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)