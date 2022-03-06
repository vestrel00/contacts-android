# Debug the Blocked Number Provider tables

If you want to take a look at the contents of Blocked Number Provider database tables that this
library uses, then use the `debug` module functions to print relevant columns and all rows of a
particular table to the Logcat.

This is useful if you are experiencing an issue and are trying to figure out if it is this library's
fault or not. This is most useful for contributors of this library. It allows us to verify that the
work we are doing is correct. Consumers may also use it, especially if they are building their own
full-fledged contacts application.

```kotlin
Context.logBlockedNumbersTable()
```

## This is not meant to be used in production code!

DO NOT include usages of the `debug` module in your production code! It is only meant to be used as
a debugging tool **during development**!

## Debug functions do not depend on the core library

Notice that the `debug` module does not depend on the `core` module, or any other modules in this
project. This is done to ensure that whatever is being logged is independent of the core API
implementation! This is important for debugging the core APIs during development. We wouldn't
exactly want to debug the core APIs using the core APIs. That's just a recipe for disaster!

## Debug functions assume that privileges are acquired

There are no permissions required for blocked numbers. However, there are privileges that must be
acquired. For more info, read about [Blocked numbers](./../blockednumbers/about-blocked-numbers.md).

If privileges are not acquired, the debug functions will not print any table rows.

## Logging Contacts Provider tables

To log Contacts Provider tables, read [Debug the Contacts Provider tables](./../debug/debug-contacts-provider-tables.md).