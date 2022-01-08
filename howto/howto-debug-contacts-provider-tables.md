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

## This is not meant to be used in production code!

DO NOT include usages of the `debug` module in your production code! It is only meant to be used as
a debugging tool **during development**!

There are several reasons why you should only use this for debugging.

First, Contacts database tables may be very lengthy. Imagine trying to print thousands of contact
data! It would slow down your app significantly if you log in the UI thread.

Second, Contacts database tables will most likely contain sensitive, private information about your
users. If you are working on a contacts app and you are logging your user's Contacts database table
rows into remote tracking services for analytics or crash reporting, you could be violating
[GDPR](https://gdpr-info.eu) depending on how you use that information. Be careful. This is why
logging functions in the `debug` module are not customizable and are not part of the core API.

Other forms of logging outside of the `debug` module implemented by this library allows consumers to
uphold privacy laws.

**The `debug` module is a power tool that should only be used for local debugging purposes!**

## Debug functions do not depend on the core library

Notice that the `debug` module does not depend on the `core` module, or any other modules in this
project. This is done to ensure that whatever is being logged is independent of the core API
implementation! This is important for debugging the core APIs during development. We wouldn't
exactly want to debug the core APIs using the core APIs. That's just a recipe for disaster!

## Debug functions assume that permissions have been granted

If the read permission `android.permission.READ_CONTACTS` is not granted, the debug functions will
not print any table rows.

