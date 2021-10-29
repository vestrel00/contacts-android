# How do I include only the data that I want?

When using query APIs such as `Query`, `BroadQuery`, and `ProfileQuery`, you are able to specify
all or only some kinds of data that you want to be included in the matching contacts.

For example, to include only name, email, and phone number data,

```kotlin
query.include(mutableListOf<AbstractDataField>().apply {
    add(Fields.Contact.DisplayNamePrimary)
    addAll(Fields.Name.all)
    addAll(Fields.Email.all)
    addAll(Fields.Phone.all)
})
```

To explicitly include everything,

```kotlin
query.include(Fields.all)
```

> Note that not invoking the `include` function will default to including everything.

The matching contacts **may** have non-null data for each of the included fields. Fields that are
included will not guarantee non-null data in the returned contact instances because some data may
actually be null in the database.

If no fields are specified, then all fields are included. Otherwise, only the specified fields will
be included in addition to required API fields (e.g. IDs), which are always included.

> Note that this may affect performance. It is recommended to only include fields that will be used
> to save CPU and memory.

## Other fields may inadvertently be included

The Android contacts Data table uses generic column names (e.g. "data1", "data2", ...) using the
column 'mimetype' to distinguish between the different kinds of data it represents. For
example, the column name of `NameFields.DisplayName` is the same as
`AddressFields.FormattedAddress`, which is "data1". This means that including
`AddressFields.FormattedAddress` will inadvertently include `NameFields.DisplayName`. There is no
workaround for this because the `ContentResolver.query` function only takes in an array of column
names.

## Potential Data Loss

Do not perform updates on Contacts, RawContacts, or Data returned by a query where all fields are
not included as it may result in data loss! To include all fields, do one of the following;

- Do no call this `include` function.
- Call this `include` function with no fields (empty).
- Pass in `Fields.all`.