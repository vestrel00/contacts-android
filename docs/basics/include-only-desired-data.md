# Include only certain fields for read and write operations

The read (query) and write (insert, update) APIs in this library provides an `include` function that
allows you to specify all (default) or some fields to read or write in the Contacts Provider 
database.

The fields defined in `contacts.core.Fields.kt` specify what properties of entities to include in
read and write operations. For example, to include only the contact display name, organization
company, and all phone number fields in a query/insert/update operation,

```kotlin
queryInsertUpdate.include(mutableSetOf<AbstractDataField>().apply {
    add(Fields.Contact.DisplayNamePrimary)
    add(Fields.Organization.Company)
    addAll(Fields.Phone.all)
})
```

The following entity properties are used in the read/write operation,

```kotlin
Contact {
    displayNamePrimary
    
    RawContact {
        organization {
            company
        }
        phones {
            number
            normalizedNumber
            type
            label
        }
    }
}
```

> ℹ️ For more info, read about [API Entities](./../entities/about-api-entities.md).

## Including all fields

By default (not calling the `include` function) or passing in an empty list will result in the 
inclusion of all fields (including custom data fields) in the most optimal way,

```kotlin
query.include()
// or
query.include(emptyList())
```

If for some reason you want to include only all non-custom data fields explicitly,

```kotlin
.include(Fields.all)
```

If you want to also include custom data fields explicitly (not recommended),

```kotlin
.include(Fields.all + contactsApi.customDataRegistry.allFields())
```

If you want to include all fields, including custom data fields, in the read/write operation, then 
passing in an empty list or not invoking the `include` function is the most performant way to do it
because internal checks will be disabled (less lines of code executed).

## Using `include` in query APIs

When using query APIs such as `Query`, `BroadQuery`, `PhoneLookupQuery`, `RawContactsQuery`,
`ProfileQuery`, and `DataQuery`, you are able to specify all or only some kinds of data that you 
want to be included in the returned results.

When all fields are included in a query operation, all properties of Contacts, RawContacts, and Data
are populated with values from the database. Properties of fields that are included are not 
guaranteed to be non-null because the database may actually have no data for the corresponding 
field.

When only some fields are included, only those included properties of Contacts, RawContacts, and 
Data are populated with values from the database. Properties of fields that are not included are 
guaranteed to be null.

### Optimizing queries

Are you finding that your queries are slow or take too much memory? Are you looking to optimize
your queries? Well, you are in the right section!

When you are showing a list of Contacts using the `Query` and `BroadQuery` APIs, you typically only 
show their thumbnail photo and display name.

```kotlin
.include(Fields.Contact.PhotoThumbnailUri, Fields.Contact.DisplayNamePrimary)
```

If instead you are showing a list of RawContacts directly instead of Contacts using the 
`RawContactsQuery`, you typically only show their display name.

```kotlin
.includeRawContactsFields(RawContactsFields.DisplayNamePrimary)
```

In such cases, you should only include those fields in order to increase speed and lessen memory usage.

Here is a sample benchmark running on an M1 MacBook Pro using a Pixel 4 API 30 emulator in 
Android Studio. The Contacts Provider database contains 10,000 contacts each having exactly one 
address, email, event, im, name, nickname, note, organization, phone, relation, sip address, and 
website.

Getting all 10,000 contacts including only fields from `Fields.Contact` using the following query,

```kotlin
val contacts = Contacts(context)
    .broadQuery()
    .include(
        Fields.Contact.LookupKey,
        Fields.Contact.DisplayNamePrimary
    )
    .find()
```

takes between 99 to 144 **milliseconds** =)

> ℹ️ As of [version 0.2.4](https://github.com/vestrel00/contacts-android/releases/tag/0.2.4), 
> including only fields from `Fields.Contact` in `Query` and `BroadQuery` API calls will result in 
> the fastest and most memory efficient queries. Prior versions do not have this extra optimization, 
> though including less fields still result in faster queries. Prior versions will take on average
> 1334 milliseconds for the same query above.

Getting all 10,000 contacts including ALL fields using the following query,

```kotlin
val contacts = Contacts(context).broadQuery().find()
```

takes between 2953 to 3009 milliseconds =(

## Using `include` in insert APIs

When using insert APIs such as `Insert` and `ProfileInsert`, you are able to specify all or only 
some kinds of data that you want to be included in the insert operation.

When all fields are included in an insert operation, all properties of Contacts, RawContacts, and 
Data are inserted into the database. 

When only some fields are included, only those included properties of Contacts, RawContacts, and
Data are inserted into the database. Properties of fields that are not included are NOT inserted 
into the database.

## Using `include` in update APIs

When using update APIs such as `Update`, `ProfileUpdate`, and `DataUpdate`, you are able to specify 
all or only some kinds of data that you want to be included in the update operation.

### An "update" operation consists of insertion, updates, and deletions

To ensure that the database matches the data contained in the entities being passed into the update 
operation, a combination of insert, update, or delete operations are performed internally by the 
update API. The following is what constitutes an _"updated"_ event;

- A RawContact can have 0 or 1 name. 
    - If it is null or blank, then the update operation will...
        - delete the name row of the RawContact from the database, if it exist
    - If it is not null, then the update operation will do one of the following...
        - update an existing name row, if it exist
        - or insert a new name row, if one does not exist
- A RawContact can have 0, 1 or more emails.
    - If the list of emails is empty (or contains only blanks), then the update operation will...
        - delete all email rows of the RawContact from the database, if any exist
    - If the list of emails is not empty, then the update operation will do all of the following...
        - update email rows for emails that already exist in the database
        - insert new email rows for emails that do not yet exist in the database
        - delete email rows for emails that exist in the database but not in the (in-memory) entity

### Blank data are deleted

Blank data are deleted from the database, unless the the complete set of corresponding fields are 
not included in the update operation.

> ⚠️ Prior to [version 0.2.1](https://github.com/vestrel00/contacts-android/discussions/160), before 
> [`include` in update APIs have been overhauled](https://github.com/vestrel00/contacts-android/issues/209), 
> blank data are deleted from the database even if the corresponding fields are not included.

> ℹ️ For more info on blank data, read about [Blank data](./../entities/about-blank-data.md).

### Including complete field sets for "update"

When all fields are included in an update operation, all properties of Contacts, RawContacts, and
Data are _"updated"_ in the database.

When only some fields are included, only those included properties of Contacts, RawContacts, and
Data are _"updated"_ in the database. Properties of fields that are not included are NOT 
_"updated"_.

To get all contacts including all fields, then modify the emails, phones, and addresses and perform 
an update operation on all fields,

```kotlin
val contacts = query.find() 
val contactsWithModifiedEmailPhoneAddress = modifyEmailPhoneAddressIn(contacts)
update.contacts(contactsWithModifiedEmailPhoneAddress).commit()
```

To modify all emails of all contacts without updating anything else into the database,

```kotlin
val contactsWithOnlyEmailData = query.include(Fields.Email.all).find()
val contactsWithModifiedEmailData = modifyEmailsIn(contactsWithOnlyEmailData)
update.contacts(contactsWithModifiedEmailData).include(Fields.Email.all).commit()
```

To remove all emails from all contacts without updating anything else in the database,

```kotlin
val contactsWithAllData = query.find()
val contactsWithNoEmailData = removeEmailsFrom(contactsWithAllData)
update.contacts(contactsWithNoEmailData).include(Fields.Email.all).commit()
```

### Including a subset of field sets for "update"

Including only a subset of a set of fields results in,

- deletion of blanks (same as if the complete set of fields are included)
- update of properties corresponding to included fields
- no-op on properties corresponding to excluded fields

For example, the following set the given name and family name to the non-null values but does not
set all others (i.e. display name, middle name, prefix, suffix, phonetic given middle family name).

```kotlin
contacts
    .update()
    .include(
        Fields.Name.GivenName,
        Fields.Name.FamilyName,
    )
    .rawContacts(
        existingRawContact.mutableCopy {
            setName {
                displayName = "Mr. "
                prefix = "Mr."
                givenName = "First"
                middleName = "Middle"
                familyName = "Last"
                suffix = "Jr."
                phoneticGivenName = "fUHRst"
                phoneticMiddleName = "mIdl"
                phoneticFamilyName = "lAHst"
            }
        }
    )
    .commit()
```

If the name row for the RawContact did not exist before the update operation, then a new name row
will be inserted into the database for the RawContact. The given name and family name columns will
be set to the specified values. All other columns will be set to null.

If the name row for the RawContact already exists before the update operation, then the name row
will be updated. The given name and family name columns will be set to the specified values. All 
other columns will remain unchanged (the null or non-null values will remain null and non-null
respectively).