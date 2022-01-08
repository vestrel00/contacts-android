# How do I redact entities and API input and output in production?

All of the entities and Create (Query), Read (Query), Update, and Delete APIs 
(a.k.a [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) APIs) provided in this
library are `Redactable`.

Redactables indicates that there could be sensitive private user data that could be redacted, for 
legal purposes such as upholding [GDPR](https://gdpr-info.eu) guidelines. If you are logging 
contact data in production to remote data centers for analytics or crash reporting, then it is 
important to redact certain parts of every contact's data.

> For more info on logging, read [How do I log API input and output?](/contacts-android/howto/howto-log-api-input-output.html)

## DISCLAIMER: This is NOT legal advice!

This library is written and maintained purely by software developers with no official education or
certifications in any facet of law. Please review the redacted outputs of the APIs and entities
within this library with your legal team! This library will not be held liable for any privacy
violations!

With that out of the way, let's move on to the good stuff =)

## Redactable entities

All `Entity` in this library are `Redactable`.

For example,

```
Contact: id=1, email { address="vestrel00@gmail.com" }, phone { number="(555) 555-5555" }, etc
```

when redacted,

```
Contact: id=1, email { address="*******************" }, phone { number="************" }, etc
```

Notice that all characters in private user data are replaced with "*". Redacted strings are not as
useful as the non-redacted counterpart. However, we still have the following valuable information;

- is the string null or not?
- how long is the string?

Database row IDs (and typically non-string properties) do not have to be redacted unless they 
contain sensitive information.

The `redactedCopy` function will return an actual copy of the entity, except with sensitive data
redacted. In addition to logging, this will allow consumers to do cool things like implementing a
redacted contact view! Imagine a button that the user can press to redact everything in their
contact form. Cool? Yes! Useful? Maybe? :grin:

Redacted copies have `isRedacted` set to true to indicate that data has already been redacted.

## Redactable APIs

All Create (Query), Read (Query), Update, and Delete APIs
(a.k.a [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) APIs) provided in this
library are `Redactable`.

For example,

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

when redacted,

```
Query {
    rawContactsWhere: (account_name LIKE '*******************' ESCAPE '\') AND (account_type LIKE '**********' ESCAPE '\')
    where: data1 LIKE '%**********%' ESCAPE '\' AND mimetype = 'vnd.android.cursor.item/email_v2'
    isRedacted: true
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
                addresses=[Address(id=329, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=WORK, label=null, formattedAddress=*************, street=*************, poBox=null, neighborhood=null, city=null, region=null, postcode=null, country=null, isRedacted=true)], 
                emails=[
                    Email(id=318, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=WORK, label=null, address=************************, isRedacted=true), 
                    Email(id=319, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=HOME, label=null, address=******************, isRedacted=true)
                ], 
                events=[
                    Event(id=317, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=BIRTHDAY, label=null, date=EventDate(year=null, month=0, dayOfMonth=1, isRedacted=true), isRedacted=true), 
                    Event(id=320, rawContactId=45, contactId=46, isPrimary=false, isSuperPrimary=false, type=ANNIVERSARY, label=null, date=EventDate(year=null, month=0, dayOfMonth=1, isRedacted=true), isRedacted=true)], 
                ],
                // the rest is omitted for brevity
            )
        ]
    )
    isRedacted: true
}
```

## Insert and update operations on redacted entities

This library will not stop you from using redacted copies in insert and update APIs. You could
build some cool stuff using it. I'll let your imagination take over from here =)

## Logging API input and output

TODO Create a howto for issue #144 and link it here.

## Developer notes

I know that we cannot prevent consumers of this API from violating privacy laws if they really
want to. BUT, the library should provide consumers an easy way to be GDPR-compliant! This is not
necessary for all libraries to implement but this library deals with sensitive, private user data.
Therefore, we need to be extra careful and provide consumers a GDPR-compliant way to log everything 
in this library!
