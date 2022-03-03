# Log API input and output

By default the all APIs provided in this library does not log anything at all. To enable logging all
API input/output using the `android.util.Log`, specify the `Logger` when constructing an instance
of `Contacts`;

```kotlin
val contactsApi = Contacts(
    context,
    logger = AndroidLogger()
)
```

> For more info on `Contacts` API setup, read [Contacts API Setup](/docs/setup/setup-contacts-api.md).

Invoking the `find` or `commit` functions in query, insert, update, and delete APIs will result in
the following output in the Logcat,

```
Query {
    rawContactsWhere: (account_name LIKE 'test@gmail.com' ESCAPE '\') AND (account_type LIKE 'com.google' ESCAPE '\')
    where: data1 LIKE '%@gmail.com%' ESCAPE '\' AND mimetype = 'vnd.android.cursor.item/email_v2'
    isRedacted: false
    // the rest is omitted for brevity
}
Query.Result {
    Number of contacts found: 2
    First contact: Contact(
        id=46, 
        rawContacts=[
            RawContact(
                id=45, 
                contactId=46, 
                addresses=[Address(id=329, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=WORK, label=null, formattedAddress=1200 Park Ave, street=1200 Park Ave, poBox=null, neighborhood=null, city=null, region=null, postcode=null, country=null, isRedacted=false)], 
                emails=[
                    Email(id=318, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=WORK, label=null, address=buzz.lightyear@pixar.com, isRedacted=false), 
                    Email(id=319, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=HOME, label=null, address=buzz@lightyear.net, isRedacted=false)
                ], 
                events=[
                    Event(id=317, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=BIRTHDAY, label=null, date=EventDate(year=1995, month=10, dayOfMonth=22, isRedacted=false), isRedacted=false), 
                    Event(id=320, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=ANNIVERSARY, label=null, date=EventDate(year=2022, month=0, dayOfMonth=1, isRedacted=false), isRedacted=false)
                ], 
                // the rest is omitted for brevity
            )
        ]
    )
    isRedacted: false
}
```

This is very useful during development. If you have any issues with the library, maintainers will
most likely ask you for these logs to help debug your issues.

## Custom loggers

The library provides the `AndroidLogger`. However, if you want to use your own logging/tracking
functions, you may create your own logger by providing an implementation of `Logger`.

For example, to use [`Timber`](https://github.com/JakeWharton/timber) instead of `android.util.Log`,

```kotlin
class TimberLogger : Logger {

    override val redactMessages: Boolean = true

    override fun log(message: String) {
        Timber.d(message)
    }
}

val contactsApi = Contacts(
    context,
    logger = AndroidLogger()
)
```

## Redacting log messages

The messages that are logged may contain private user data (contact data). Depending on how you log
these messages in production, you may end up violating privacy laws such as
[GDPR](https://gdpr-info.eu) guidelines.

To ensure that you are not violating any privacy laws in your production apps when using this
library, make sure to set `Logger.redactMessages` to `true`.

```kotlin
val contactsApi = Contacts(
    context,
    logger = AndroidLogger(redactMessages = true)
)
```

Redacted messages are not as useful when debugging so you should set it to `false` during
development. A common way to redact messages in release builds but not debug builds is to,

```kotlin
AndroidLogger(redactMessages = !BuildConfig.DEBUG)
```

For more info on redaction, read [Redact entities and API input and output in production](/docs/entities/redact-apis-and-entities.md).
