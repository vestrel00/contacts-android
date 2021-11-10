# How do I debug the Contacts Provider tables?

If you want to take a look at the contents of Contacts Provider database tables that this library
uses, then make sure that you set the `debug` module as a dependency to your project. It provides 
you functions that print relevant columns and all rows of a particular table to the Logcat.

This is useful if you are experiencing an issue and are trying to figure out if it is this library's 
fault or not. This is most useful for contributors of this library. It allows us to verify that the
work we are doing is correct. Consumers may also use it, especially if they are building their own
full-fledged contacts application.

| **ContactsContract Table** | **Function**                         |
|----------------------------|--------------------------------------|
| `Groups`                   | `Context.logGroupsTable()`           |
| `AggregationExceptions`    | `Context.logAggregationExceptions()` |
| `Profile`                  | `Context.logProfile()`               |
| `Contacts`                 | `Context.logContactsTable()`         |
| `RawContacts`              | `Context.logRawContactsTable()`      |
| `Data`                     | `Context.logDataTable()`             |

To log all of the above tables in a single call,

```kotlin
Context.logContactsProviderTables()
```

## Debug functions do not depend on the core library

Notice that the `debug` module does not depend on the `core` module, or any other modules in this
project. This is done to ensure that whatever is being logged is independent of the core API 
implementation! This is important for debugging the core APIs during development. We wouldn't exactly
want to debug the core APIs using the core APIs. That's just a recipe for disaster! 

## Debug functions assume that permissions have been granted

If the read permission `android.permission.READ_CONTACTS` is not granted, the debug functions will
not print any table rows.

