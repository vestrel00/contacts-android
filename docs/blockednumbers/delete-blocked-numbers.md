# Delete blocked numbers

This library provides the `BlockedNumbersDelete` API that allows you to delete existing 
BlockedNumbers.

An instance of the `BlockedNumbersDelete` API is obtained by,

```kotlin
val delete = Contacts(context).blockedNumbers().delete()
```

Note that blocked number deletions will only work for privileged apps. For more info, read about
[Blocked numbers](./../blockednumbers/about-blocked-numbers.md).

## A basic delete

To delete a set of existing blocked numbers,

```kotlin
val deleteResult = delete
    .blockedNumbers()
    .delete()
    .blockedNumbers(existingBlockedNumbers)
    .commit()
```
To delete a set of existing blocked numbers using IDs,

```kotlin
val deleteResult = delete
    .blockedNumbers()
    .delete()
    .blockedNumbersWithId(1, 2, 3)
    .commit()
```

## An advanced delete

You may specify a matching criteria, like in queries, that will delete all matching blocked numbers,

```kotlin
val deleteResult = delete
    .dataWhere { Number contains "555" }
    .commit()
```

## Executing the delete

To execute the delete,

```kotlin
.commit()
```

If you want to delete all given blockedNumbers in a single atomic transaction,

```kotlin
.commitInOneTransaction()
```

The call to `commitInOneTransaction` will only succeed if ALL given blocked numbers are successfully
deleted. If one delete fails, the entire operation will fail and everything will be reverted prior
to the delete operation. In contrast, `commit` allows for some deletes to succeed and some to fail.

### Handling the delete result

The `commit` and `commitInOneTransaction` functions returns a `Result`,

To check if all deletes succeeded,

```kotlin
val allDeletesSuccessful = deleteResult.isSuccessful
```

To check if a particular delete succeeded,

```kotlin
val firstDeleteSuccessful = deleteResult.isSuccessful(blockedNumber1)
```

## Performing the delete and result processing asynchronously

Deletes are executed when the `commit` or `commitInOneTransaction` function is invoked. The work is
done in the same thread as the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the delete with permission

There are no permissions required for blocked numbers. However, there are privileges that must be
acquired. For more info, read about [Blocked numbers](./../blockednumbers/about-blocked-numbers.md).