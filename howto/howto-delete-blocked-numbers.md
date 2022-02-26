# How do I delete blocked numbers?

This library provides the `BlockedNumbersDelete` API that allows you to delete existing 
BlockedNumbers.

An instance of the `BlockedNumbersDelete` API is obtained by,

```kotlin
val delete = Contacts(context).blockedNumbers().delete()
```

Note that blocked number deletions will only work for privileged apps. For more info, read
[How do I learn more about blocked numbers?](/howto/howto-learn-more-about-blocked-numbers.md)

## A basic delete

To delete a set of existing blocked numbers,

```kotlin
val deleteResult = Contacts(context)
    .blockedNumbers()
    .delete()
    .blockedNumbers(existingBlockedNumbers)
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
For more info, read [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the delete with permission

There are no permissions required for blocked numbers. However, there are privileges that must be
acquired. For more info, read [How do I learn more about blocked numbers?](/howto/howto-learn-more-about-blocked-numbers.md)