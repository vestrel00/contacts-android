# Get set clear default Contact data

Default contact data are instances of common data kinds that are marked as the default.

The two most common data kinds that use this mechanism are emails and phones. In the native 
Contacts app Contact details activity, long pressing an email or phone shows a popup menu with an 
option to set it as default. When a particular email or phone is set as default, sending an email
and making a phone call to that contact will use that default email and phone respectively.

> For more info on the common data kinds, read about [API Entities](./../entities/about-api-entities.md).

## Getting default data

To get the default Contact email and phone from all RawContacts,

```kotlin
val defaultContactEmail: Email? = contact.emails().default()
val defaultContactPhone: Phone? = contact.phones().default()
```

To get the default RawContact email and phone,

```kotlin
val defaultRawContactEmail: Email? = rawContact.emails.default()
val defaultRawContactPhone: Phone? = rawContact.phones.default()
```

To get the first default data out of a generic list of data,

```kotlin
val defaultData = dataList.default()
```

Note that the most common use of defaults is with Contacts, not RawContacts. You typically do not 
need to worry about defaults at a RawContact level.

## Setting default data

To set a particular data as the default for the set of data of the same type (e.g. email) for the 
aggregate Contact,

```kotlin
email.setAsDefault(contactsApi)
```

If a default data of the same type for the aggregate Contact already exist before this call, then 
it will no longer be the default.

For example, these emails belong to the same aggregate Contact;

- x@x.com (default)
- y@y.com
- z@z.com

Calling this function on a non-default data (e.g. y@y.com) will remove the default status for data
that was previously set as the default. This data will then be set as the default. This results in;

- x@x.com
- y@y.com (default)
- z@z.com

## Clearing default data

To remove the default status of any data of the same type (e.g. email), if any, for the aggregate 
Contact,

```kotlin
email.clearDefault(contactsApi)
```

For example, these emails belong to the same aggregate Contact;

- x@x.com
- y@y.com (default)
- z@z.com

Calling this function on any data of the same kind for the aggregate contact (default or not) will 
remove the default status on all data of the same kind for the aggregate Contact. This results in;

- x@x.com
- y@y.com
- z@z.com

## Changes are immediate and are not applied to the receiver

These apply to set and clear functions.

1. Changes are immediate.
    - These functions will make the changes to the Contacts Provider database immediately. You do
      not need to use update APIs to commit the changes.
2. Changes are not applied to the receiver.
    - This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
      query APIs or refresh extensions or process the result of this function call to get the most
      up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
      Provider database.

## Performing default data management asynchronously

Setting or clearing default data is done in the same thread as the call-site. This may result in a 
choppy UI. 

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing default data management with permission

Getting and setting/clearing default data require the `android.permission.READ_CONTACTS` and
`android.permission.WRITE_CONTACTS` permissions respectively. If not granted, getting and 
setting/clearing default data will fail.

------------------------

## Developer notes (or for advanced users)

> The following section are note from developers of this library for other developers. It is copied
> from the [DEV_NOTES](/DEV_NOTES.md). You may still read the following as a consumer of the library
> in case you need deeper insight.

As per documentation, for a set of data rows with the same mimetype (e.g. a set of emails), there
should only be one primary data row (e.g. email) per RawContact and one super primary data row per
Contact. Furthermore, a data row that is super primary must also be primary.

Unfortunately, the Contacts Provider does not do any data set validation for the Data columns
`IS_PRIMARY` and `IS_SUPER_PRIMARY`. This means that it is possible to set more than one data row of
the same mimetype as primary for the same RawContact and super primary for the same aggregate
Contact. It is also possible to set a data row as super primary but not primary. Upholding the the
contract is left to the us (the library).

For example, given this relationship;

- Contact
    - RawContact X
        - Email A
        - Email B
    - RawContact Y
        - Email C
        - Email D

When Emails A, B, C, and D are inserted with the RawContacts or after the RawContacts have been
created, we get the following state;

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 0           | 0                 |
| C         | 0           | 0                 |
| D         | 0           | 0                 |


The state does not change when RawContact X is linked with RawContact Y.

After setting Email A as the "default" email, it becomes primary and super primary;

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 1           | 1                 |
| B         | 0           | 0                 |
| C         | 0           | 0                 |
| D         | 0           | 0                 |

Then setting Email B as the default email, it becomes primary and super primary. Email A is no
longer primary or super primary.

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 1           | 1                 |
| C         | 0           | 0                 |
| D         | 0           | 0                 |

Then setting Email C as the default email, it becomes primary and super primary. Email B is still
primary because it belongs to a different RawContact than Email C. However, Email B is no longer the
super primary as there can only be one per aggregate Contact.

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 1           | 0                 |
| C         | 1           | 1                 |
| D         | 0           | 0                 |

Then setting Email D as the default email, it becomes primary and super primary. Email C is no
longer primary or super primary.

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 1           | 0                 |
| C         | 0           | 0                 |
| D         | 1           | 1                 |

Then clearing the default email D, removes its primary and super primary status. However, email B
remains a primary but not a super primary.

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 1           | 0                 |
| C         | 0           | 0                 |
| D         | 0           | 0                 |

The above behavior is observed from the native Contacts app. The "super primary" data of an
aggregate Contact is referred to as the "default".