# How do I create/insert groups?

This library provides the `GroupsInsert` API that allows you to create/insert groups associated to
an `Account`.

An instance of the `GroupsInsert` API is obtained by,

```kotlin
val query = Contacts(context).groups().insert()
```

## A basic insert

To create/insert a new group for an Account,

```kotlin
val insertResult = Contacts(context)
    .groups()
    .insert()
    .group(
        title = "Besties",
        account = Account("john.doe@gmail.com", "com.google")
    )
    .commit()
```

If you need to insert multiple groups,

```kotlin
val mutableGroup1 = MutableGroup("Goodies", Account("john.doe@gmail.com", "com.google"))
val mutableGroup2 = MutableGroup("Baddies", Account("john.doe@gmail.com", "com.google"))

val insertResult = Contacts(context)
    .groups()
    .insert()
    .groups(mutableGroup1, mutableGroup2)
    .commit()
```

## Groups and Accounts

A set of groups exist for each Account. When there are no accounts in the system, there are no 
groups and inserting groups will fail.

The get accounts permission is required here because this API retrieves all available accounts,
if any, and does the following;

- if the account specified is found in the list of accounts returned by the system, then the account
  is used
- if the account specified is not found in the list of accounts returned by the system, then the 
  insertion fails for that group
- if there are no accounts in the system, [commit] does nothing and fails immediately

> For more info on the relationship of Groups and Accounts, read [How do I retrieve groups?](/contacts-android/howto/howto-query-groups.html)

## Groups and duplicate titles

The Contacts Provider allows multiple groups with the same title (case-sensitive comparison) 
belonging to the same account to exist. In older versions of Android, the native Contacts app 
allows the creation of new groups with existing titles. In newer versions, duplicate titles are not 
allowed. Therefore, this library does not allow for duplicate titles.

In newer versions, the group with the duplicate title gets deleted either automatically by the 
Contacts Provider or when viewing groups in the native Contacts app. It's not an immediate failure 
on insert or update. This could lead to bugs!

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
val firstInsertSuccessful = insertResult.isSuccessful(mutableGroup1)
```

To get the Group IDs of all the newly created Groups,

```kotlin
val allGroupIds = insertResult.groupIds
```

To get the Group ID of a particular Group,

```kotlin
val secondGroupId = insertResult.groupId(mutableGroup2)
```

Once you have the Group IDs, you can retrieve the newly created Groups via the `GroupsQuery` API,

```kotlin
val groups = contactsApi
    .groups()
    .query()
    .where(GroupsFields.Id `in` allGroupIds)
    .find()
```

> For more info, read [How do I retrieve groups?](/contacts-android/howto/howto-query-groups.html)

Alternatively, you may use the extensions provided in `GroupsInsertResult`. To get all newly created
Groups,

```kotlin
val groups = insertResult.groups(contactsApi)
```

To get a particular group,

```kotlin
val group = insertResult.group(contactsApi, mutableGroup1)
```

### Handling insert failure 

The insert may fail for a particular group for various reasons,

```kotlin
if (!insertResult.isSuccessful(mutableGroup1)) {
    insertResult.failureReason(mutableGroup1)?.let {
        when (it) {
            TITLE_ALREADY_EXIST -> promptUserToPickDifferentTitle()
            INVALID_ACCOUNT -> promptUserToPickDifferentAccount()
            UNKNOWN -> showGenericErrorMessage()
        }   
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

## Performing the insert with permission

Inserts require the `android.permission.WRITE_CONTACTS` and `android.permission.GET_ACCOUNTS` 
permissions. If not granted, the insert will do nothing and return a failed result.

To perform the insert with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/contacts-android/howto/howto-use-api-with-permissions-handling.html)

You may, of course, use other permission handling libraries or just do it yourself =)
