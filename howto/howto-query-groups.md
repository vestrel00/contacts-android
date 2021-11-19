# How do I retrieve groups?

This library provides the `GroupsQuery` API that allows you to get groups associated with an 
`Account` or `RawContact`.

An instance of the `GroupsQuery` API is obtained by,

```kotlin
val query = Contacts(context).groups().query()
```

To get all of the groups for all accounts,

```kotlin
val groupsFromAllAccounts = Contacts(context)
    .groups()
    .query()
    .find()
```

> Note that it is recommended to get sets of groups for a single account at a time to avoid confusion.

## Specifying Accounts

To limit the search to only those Groups associated with one of the given accounts,

```kotlin
.accounts(accounts)
```

For example, to limit the search to groups belonging to only one account,

```kotlin
.accounts(Account("john.doe@gmail.com", "com.google"))
```

> For more info, read [How do I query for Accounts?](/contacts-android/howto/howto-query-accounts.html)

If no accounts are specified (this function is not called or called with no Accounts), then all
Groups of all accounts are included in the search.

A null Account may NOT be provided here because no group can exist without an account. Groups are
inextricably linked to an Account.

## Ordering

To order resulting Groups using one or more fields,

```kotlin
.orderBy(fieldOrder)
```

For example, to order groups by account name,

```kotlin
.orderBy(GroupsFields.AccountName.asc())
```

String comparisons ignores case by default. Each orderBys provides `ignoreCase` as an optional
parameter.

Use `GroupsFields` to construct the orderBys.

## Limiting and offsetting

To limit the amount of groups returned and/or offset (skip) a specified number of groups,

```kotlin
.limit(limit)
.offset(offset)
```

For example, to only get a maximum 20 groups, skipping the first 20,

```kotlin
.limit(20)
.offset(20)
```

This is useful for pagination =)

> Note that it is recommended to limit the number of groups when querying to increase performance
> and decrease memory cost.

## Executing the query

To execute the query,

```kotlin
.find()
```

## Cancelling the query

To cancel a query amid execution,

```kotlin
.find { returnTrueIfQueryShouldBeCancelled() }
```

The `find` function optionally takes in a function that, if it returns true, will cancel query
processing as soon as possible. The function is called numerous times during query processing to
check if processing should stop or continue. This gives you the option to cancel the query.

This is useful when used in multi-threaded environments. One scenario where this would be commonly
used is when performing queries as the user types a search text. You are able to cancel the current
query when the user enters new text.

For example, to automatically cancel the query inside a Kotlin coroutine when the coroutine is cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val groups = query.find { !isActive }
    }
}
```

## Performing the query asynchronously

Queries are executed when the `find` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/contacts-android/howto/howto-use-api-with-async-execution.html)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` permission. If not granted, the query will 
do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/contacts-android/howto/howto-use-api-with-permissions-handling.html)

You may, of course, use other permission handling libraries or just do it yourself =)

## Using the `where` function to specify matching criteria

Use the `contacts.core.GroupsFields` combined with the extensions from `contacts.core.Where` to form
WHERE clauses. 

> This howto page will not provide a tutorial on database where clauses. It assumes that you know the basics. 
> If you don't know the basics, then search for [sqlite where clause](https://www.google.com/search?q=sqlite+where+clause). 

For example, to find groups with a specific title,

```kotlin
.where(GroupsFields.Title equalToIgnoreCase "friends")
```

To get a list of groups by IDs,

```kotlin
.where(GroupsFields.Id `in` groupIds)
```

## Different groups with the same titles
  
Each account will have its own set of system and user-created groups. This means that there may be
multiple groups with the same title belonging to different accounts. This is not a bug. This is why
it is recommended to only get sets of groups per account, especially if there is more than one
account in the system.

## Groups from more than one account in the same list

When you perform a query that returns groups from more than one account, you will get everything
in the same `GroupsList`. This list is just like any other `List` except it also provides an extra
function that allows you to get a sublist with groups belonging only to a particular account.

```kotlin
val groupsFromAccount = groupsList.from(account)
```

This is equivalent to,

```kotlin
val groupsFromAccount = groupsList.filter { it.account == account }
```

It serves more as documentation and hint that you should really not be mixing groups from different
accounts in the same list as it could cause confusion. However, if you know what you are doing and
you are not confused, then do what you like :D 

This is also nice for Java users to not have to perform the filtering themselves.
