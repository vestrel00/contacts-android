# Insert blocked numbers

This library provides the `BlockedNumbersInsert` API that allows you to create/insert blocked numbers.

An instance of the `BlockedNumbersInsert` API is obtained by,

```kotlin
val insert = Contacts(context).blockedNumbers().insert()
```

Note that blocked number insertions will only work for privileged apps. For more info, read about
[Blocked numbers](/docs/blockednumbers/about-blocked-numbers.md).

## A basic insert

To create/insert a new blocked number,

```kotlin
val insertResult = Contacts(context)
    .blockedNumbers()
    .insert()
    .blockedNumber { number = "(555) 555-5555" }
    .commit()
```

If you need to insert multiple blocked numbers,

```kotlin
val newBlockedNumber1 = NewBlockedNumber(number = "(555) 555-5555")
val newBlockedNumber2 = NewBlockedNumber(number = "(123) 456-7890")

val insertResult = Contacts(context)
    .blockedNumbers()
    .insert()
    .blockedNumbers(newBlockedNumber1, newBlockedNumber2)
    .commit()
```

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
val firstInsertSuccessful = insertResult.isSuccessful(newBlockedNumber1)
```

To get the BlockedNumber IDs of all the newly created BlockedNumbers,

```kotlin
val allBlockedNumberIds = insertResult.blockedNumberIds
```

To get the BlockedNumber ID of a particular BlockedNumber,

```kotlin
val secondBlockedNumberId = insertResult.blockedNumberId(newBlockedNumber2)
```

Once you have the BlockedNumber IDs, you can retrieve the newly created BlockedNumbers via the 
`BlockedNumbersQuery` API,

```kotlin
val blockedNumbers = contactsApi
    .blockedNumbers()
    .query()
    .where { Id `in` allBlockedNumberIds }
    .find()
```

> For more info, read [Query blocked numbers](/docs/blockednumbers/query-blocked-numbers.md).

Alternatively, you may use the extensions provided in `BlockedNumbersInsertResult`. To get all 
newly created BlockedNumbers,

```kotlin
val blockedNumbers = insertResult.blockedNumbers(contactsApi)
```

To get a particular blockedNumber,

```kotlin
val blockedNumber = insertResult.blockedNumber(contactsApi, newBlockedNumber1)
```

### Handling insert failure 

The insert may fail for a particular blocked number for various reasons,

```kotlin
insertResult.failureReason(newBlockedNumber1)?.let {
    when (it) {
        NUMBER_ALREADY_BLOCKED -> tellUserTheNumberIsAlreadyBlocked()
        NUMBER_IS_BLANK -> promptUserProvideANonBlankNumber()
        UNKNOWN -> showGenericErrorMessage()
    }   
}
```

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
read [Execute work outside of the UI thread using coroutines](/docs/async/async-execution.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the insert with permission

There are no permissions required for blocked numbers. However, there are privileges that must be
acquired. For more info, read about [Blocked numbers](/docs/blockednumbers/about-blocked-numbers.md).