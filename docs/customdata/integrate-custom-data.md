# Integrate custom data

If you are looking to integrate custom data from other apps, 
read [Integrate custom data from other apps](/docs/customdata/integrate-custom-data-from-other-apps.md).

If you are looking to create and integrate your own custom data, you are in the right place!

There are two parts to "integrating custom data";

1. Providing create (insert), read (query), update, and delete (CRUD) APIs for custom data 
   associated with RawContacts.
2. Providing sync adapters to sync custom data across devices.

This library only handles the first part. If you want to sync your custom data, then you need to 
implement a sync adapter to interface with your remote server. That is out of scope of this library.

In order to create and integrate your own custom data for use in your own apps, there is a bit of
boilerplate code that needs to be written. Thankfully none of this stuff is difficult! 

Here are the steps, in chronological order, on how to define and use your own custom data,

1. Define the mimetype
2. Define the entities
3. Define the fields
4. Implement the cursor
5. Implement the mapper
6. Implement the operation
7. Define the count restriction
8. Define RawContact getters and setters
9. Define Contact getters and setters
10. Define exceptions
11. Implement the field mapper
12. Define the data query function
13. Define the custom data entry
14. Define the custom data entry registration
15. Register your custom data with the Contacts API instance
16. Use your custom data in queries, inserts, updates, and deletes

> Maybe someday someone with code generation experience (or I'll learn how to do it), will create
> annotations and annotation processors to eliminate having to manually write this stuff =)

To help illustrate the above steps, we'll use the `HandleName` and `Gender` custom data provided in
this library's `customdata-handlename` and `customdata-gender` respectively as an example.

> For more specifics on these custom data, read
> [Integrate the gender custom data](/docs/customdata/integrate-gender-custom-data.md) and
> [Integrate the handle name custom data](/docs/customdata/integrate-handlename-custom-data.md).

At the bottom of this page, we'll also discuss,

- Consider adding your custom data to this library
- Custom data without sync adapters will not be synced
- Displaying your custom data in other Contacts apps
- Summary of limitations

> Some of the code used in these examples are in Kotlin. If you would like a Java version of this
> page, create an issue in GitHub. You are also free to file a pull request with your own page. In
> the event that a Java version of this page is created, this quote block should be replaced with
> a link to that page.

## 1. Define the mimetype

The mimetype is a string that describes what kind of data a row in the Data table represents.

For `Gender`,

```kotlin
internal object GenderMimeType : MimeType.Custom() {

    // Following Contacts Provider convention of "vnd.android.cursor.item/<package>.<mimetype>"
    override val value: String = "vnd.android.cursor.item/contacts.entities.custom.gender"
}
```

For `HandleName`,

```kotlin
internal object HandleNameMimeType : MimeType.Custom() {

    // Following Contacts Provider convention of "vnd.android.cursor.item/<package>.<mimetype>"
    override val value: String = "vnd.android.cursor.item/contacts.entities.custom.handlename"
}
```

**Do not change the mimetype value!** If you have already deployed apps to production that use these
mimetype values, then changing them could result in "data loss". Old rows in the Data table will not
be compatible if the mimetype value changes. You can certainly perform migrations by creating a new
custom data altogether and migrating your old custom data to your new one.

**Do not use built-in mimetypes!** The Contacts Provider has predefined the mimetypes for all of the
common data kinds it supports (e.g. email). Make sure that your custom data does not use any of
those. You can take a look at built-in mimetypes in `contacs.core.entities.MimeType.kt`. But, here
they are for your convenience =)

| **Builtin data kind** | **mimetype**                                |
|-----------------------|---------------------------------------------|
| Address               | "vnd.android.cursor.item/postal-address_v2" |
| Email                 | "vnd.android.cursor.item/email_v2"          |
| Event                 | "vnd.android.cursor.item/contact_event"     |
| GroupMembership       | "vnd.android.cursor.item/group_membership"  |
| Im                    | "vnd.android.cursor.item/im"                |
| Name                  | "vnd.android.cursor.item/name"              |
| Nickname              | "vnd.android.cursor.item/nickname"          |
| Note                  | "vnd.android.cursor.item/note"              |
| Organization          | "vnd.android.cursor.item/organization"      |
| Phone                 | "vnd.android.cursor.item/phone_v2"          |
| Photo                 | "vnd.android.cursor.item/photo"             |
| Relation              | "vnd.android.cursor.item/relation"          |
| SipAddress            | "vnd.android.cursor.item/sip_address"       |
| Website               | "vnd.android.cursor.item/website"           |

## 2. Define the entities

The entities are the main code that users of your custom data will be exposed to. The properties
model/represent the fields/columns in the Data table.

Due to the length of the `Gender.kt` and `HandleName.kt` files, I will not be copy-pasting them
here. Please take a look at those files instead.

A few things to note,

- Either inherit from `CustomDataEntity` or `CustomDataEntityWithTypeAndLabel`.
- Implement the `mimeType` using the mimetype you defined in the previous step.
- Implement the `isBlank` using the `contacts.core.entities.propertiesAreAllNullOrBlank` function.
    - Put the properties that you consider to be important such that if they are null, then the data
      is useless (blank).
- Define an immutable class so that instances can be returned on queries.
    - These would also need to inherit from `ExistingCustomDataEntity` and
      `ImmutableCustomDataEntityWithMutableType` (or
      `ImmutableCustomDataEntityWithNullableMutableType`).
    - All properties and types defined here must be immutable (`val`).
- Define a mutable class so that instances can be updated.
    - These would also need to inherit from `ExistingCustomDataEntity`.
    - Only modifiable fields should have properties and types defined as mutable (`var`).
- Define a "new" class so that instances can be inserted.
    - These would also need to inherit from `NewCustomDataEntity`.
    - Only modifiable fields should have properties and types defined as mutable (`var`).
- Properties that map to your custom data fields should be nullable (`?`).
- The following properties should always be immutable (`val`);
    - `id`, `rawContactId`, `contactId`, `isPrimary`, `isSuperPrimary`, and `isRedacted`.
- Be mindful of what properties should be redacted when implementing the `redactedCopy` function.
- All entity class must implement `Parecelable`.

## 3. Define the fields

Fields (or columns) represent (or map to) one of the properties you defined in the previous step.
These are used in queries, inserts, and update operations.

For `Gender`,

```kotlin
data class GenderField internal constructor(private val columnName: ColumnName) :
    AbstractCustomDataField(columnName) {

    override val customMimeType: MimeType.Custom = GenderMimeType
}

object GenderFields : AbstractCustomDataFieldSet<GenderField>() {

    @JvmField
    val Type = GenderField(ColumnName.TYPE)

    @JvmField
    val Label = GenderField(ColumnName.LABEL)

    override val all: Set<GenderField> = setOf(Type, Label)
    override val forMatching: Set<GenderField> = emptySet()
}
```

For `HandleName`,

```kotlin
data class HandleNameField internal constructor(private val columnName: ColumnName) :
    AbstractCustomDataField(columnName) {

    override val customMimeType: MimeType.Custom = HandleNameMimeType
}

object HandleNameFields : AbstractCustomDataFieldSet<HandleNameField>() {

    @JvmField
    val Handle = HandleNameField(ColumnName.DATA)

    override val all: Set<HandleNameField> = setOf(Handle)
    override val forMatching: Set<HandleNameField> = setOf(Handle)
}
```

A few things to note,

- You need to define a `AbstractCustomDataField` and a `AbstractCustomDataFieldSet`.
- Annotate your field instances with `@JvmField` to make it more accessible for Java users.
    - This is only helpful if you are writing code for other people to use.
- Carefully choose what to put in `all` and `forMatching`.
    - If you are using `ColumnName.BLOB`, do not put it in `all` or `forMatching`! For more info,
      read the in-code documentation on it.

## 4. Implement the cursor

Cursors read the values from the Data table and convert them into the types you want (e.g. String).

For `Gender`,

```kotlin
internal class GenderDataCursor(cursor: Cursor, includeFields: Set<GenderField>) :
    AbstractCustomDataCursor<GenderField>(cursor, includeFields) {

    val type: GenderEntity.Type? by type(
        GenderFields.Type,
        typeFromValue = GenderEntity.Type::fromValue
    )

    val label: String? by string(GenderFields.Label)
}
```

For `HandleName`,

```kotlin
internal class HandleNameDataCursor(cursor: Cursor, includeFields: Set<HandleNameField>) :
    AbstractCustomDataCursor<HandleNameField>(cursor, includeFields) {

    val handle: String? by string(HandleNameFields.Handle)
}
```

A few things to note,

- Inheritors of `AbstractCustomDataCursor` have access to several regular and delegate functions
  that extract data. All of them are defined in `contacts.core.entities.cursor.AbstractEntityCursor`
  .
- If you are using Java, you are only able to use the regular functions.
- The delegate functions are prettier but use Kotlin reflection, which could slightly affect runtime
  performance.
- You can either extract nullable or non-nullable values using these functions.

## 5. Implement the mapper

Mappers use the cursors implemented in the previous step in order to create instances of your custom
data entities.

For `Gender`,

```kotlin
internal class GenderMapperFactory :
    AbstractCustomDataEntityMapper.Factory<GenderField, GenderDataCursor, Gender> {

    override fun create(
        cursor: Cursor, includeFields: Set<GenderField>
    ): AbstractCustomDataEntityMapper<GenderField, GenderDataCursor, Gender> =
        GenderMapper(GenderDataCursor(cursor, includeFields))
}

private class GenderMapper(cursor: GenderDataCursor) :
    AbstractCustomDataEntityMapper<GenderField, GenderDataCursor, Gender>(cursor) {

    override fun value(cursor: GenderDataCursor) = Gender(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        type = cursor.type,
        label = cursor.label,

        isRedacted = false
    )
}
```

For `HandleName`,

```kotlin
internal class HandleNameMapperFactory :
    AbstractCustomDataEntityMapper.Factory<HandleNameField, HandleNameDataCursor, HandleName> {

    override fun create(
        cursor: Cursor, includeFields: Set<HandleNameField>
    ): AbstractCustomDataEntityMapper<HandleNameField, HandleNameDataCursor, HandleName> =
        HandleNameMapper(HandleNameDataCursor(cursor, includeFields))
}

private class HandleNameMapper(cursor: HandleNameDataCursor) :
    AbstractCustomDataEntityMapper<HandleNameField, HandleNameDataCursor, HandleName>(cursor) {

    override fun value(cursor: HandleNameDataCursor) = HandleName(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        handle = cursor.handle,

        isRedacted = false
    )
}
```

A few things to note,

- This requires definitions and implementations done in the previous steps.
    - If you are having compile-time issues at this point, make sure that you did not skip a step!
- Ensure that `isRedacted` is set to `false` (unless you are already performing the redaction) here.

## 6. Implement the operation

Operations are used for inserts and updates from in-memory instances of your entities to the
database.

For `Gender`,

```kotlin
internal class GenderOperationFactory :
    AbstractCustomDataOperation.Factory<GenderField, GenderEntity> {

    override fun create(
        isProfile: Boolean, includeFields: Set<GenderField>
    ): AbstractCustomDataOperation<GenderField, GenderEntity> =
        GenderOperation(isProfile, includeFields)
}

private class GenderOperation(isProfile: Boolean, includeFields: Set<GenderField>) :
    AbstractCustomDataOperation<GenderField, GenderEntity>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = GenderMimeType

    override fun setCustomData(
        data: GenderEntity, setValue: (field: GenderField, value: Any?) -> Unit
    ) {
        setValue(GenderFields.Type, data.type?.value)
        setValue(GenderFields.Label, data.label)
    }
}
```

For `HandleName`,

```kotlin
internal class HandleNameOperationFactory :
    AbstractCustomDataOperation.Factory<HandleNameField, HandleNameEntity> {

    override fun create(
        isProfile: Boolean, includeFields: Set<HandleNameField>
    ): AbstractCustomDataOperation<HandleNameField, HandleNameEntity> =
        HandleNameOperation(isProfile, includeFields)
}

private class HandleNameOperation(isProfile: Boolean, includeFields: Set<HandleNameField>) :
    AbstractCustomDataOperation<HandleNameField, HandleNameEntity>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = HandleNameMimeType

    override fun setCustomData(
        data: HandleNameEntity, setValue: (field: HandleNameField, value: Any?) -> Unit
    ) {
        setValue(HandleNameFields.Handle, data.handle)
    }
}
```

A few things to note,

- You just need to use your custom data fields and the corresponding data property it maps to in
  the `setValue` function provided in the `setCustomData` function.

## 7. Define the count restriction

The count restriction defines whether a RawContact can have 0 or 1 of your custom data or if it can
have 0, 1, or more.

For `Gender`,

```kotlin
/**
 * A RawContact may have at most 1 gender.
 */
internal val GENDER_COUNT_RESTRICTION = CustomDataCountRestriction.AT_MOST_ONE
```

For `HandleName`,

```kotlin
/**
 * A RawContact may have 0, 1, or more handle names.
 */
internal val HANDLE_NAME_COUNT_RESTRICTION = CustomDataCountRestriction.NO_LIMIT
```

## 8. Define RawContact getters and setters

In order for you or your consumers to be able to get and set your custom data in instances of
RawContacts they belong to, you must define a set of getters and setters.

Due to the length of the `RawContactGender.kt` and `RawContactHandleName.kt` files, I will not be
copy-pasting them here. Please take a look at those files instead.

A few things to note,

- For getters, use the `Contacts.customDataRegistry.customDataEntitiesFor` function to extract the
  custom data instance(s) for the RawContact with your custom mimetype.
    - Consider returning `Sequence` for the getters for optimizations in Kotlin.
- For setters use,
    - the `Contacts.customDataRegistry.putCustomDataEntityInto` function to set the custom data
      instance into the RawContact.
    - the `Contacts.customDataRegistry.removeAllCustomDataEntityFrom` function to remove the custom
      data instance from the RawContact.
- Define getters and setters for `RawContact`, `MutableRawContact`, and `NewRawContact`.
    - Ensure to match the type of RawContact with the type of the custom data. For example,
        - `RawContact` -> `Gender`, `HandleName`
        - `MutableRawContact` -> `MutableGenderEntity`, `MutableHandleNameEntity`
            - When setting/adding a new custom data entity,
              `MutableRawContact` -> `NewGender`, `NewHandleName`
        - `NewRawContact` -> `NewGender`, `NewHandleName`
- Setters for custom data with count restriction of `AT_MOST_ONE` should use `setXXX` for the 
  function name.
- Setters for custom data with count restriction of `NO_LIMIT` should use `addXXX` and `removeXXX` 
  for the function names.

## 9. Define Contact getters and setters

Defining getters and setters for RawContacts is the bare minimum. However, if you want to add some
convenience functions so that you can access RawContact getters and setters from a Contact, then you
are free (and recommended) to do so.

Due to the length of the `ContactGender.kt` and `ContactHandleName.kt` files, I will not be
copy-pasting them here. Please take a look at those files instead.

A few things to note,

- For getters, consider returning `Sequence` for optimizations in Kotlin.
- For setters, use the first RawContact (in case there are more than one).
- Consider returning `Sequence` for the getters for optimizations in Kotlin.
- Define getters and setters for `Contact` and `MutableContact`.
    - Ensure to match the type of Contact with the type of the custom data. For example,
        - `Contact` -> `Gender`, `HandleName`
        - `MutableContact` -> `MutableGenderEntity`, `MutableHandleNameEntity`
            - When setting/adding a new custom data entity,
              `MutableContact` -> `NewGender`, `NewHandleName`

## 10. Define exceptions

Whether you are building this custom data just for your own app or for others to use, it is useful
to define a subclass of `CustomDataException` to help identify errors in certain custom data
integrations.

For `Gender`,

```kotlin
class GenderDataException(message: String) : CustomDataException(message)
```

For `HandleName`,

```kotlin
class HandleNameDataException(message: String) : CustomDataException(message)
```

## 11. Implement the field mapper

A field mapper maps your custom data field to the corresponding property in your custom data entity.

For `Gender`,

```kotlin
internal class GenderFieldMapper : CustomDataFieldMapper<GenderField, GenderEntity> {

    override fun valueOf(field: GenderField, customDataEntity: GenderEntity): String? =
        when (field) {
            GenderFields.Type -> customDataEntity.type?.ordinal?.toString()
            GenderFields.Label -> customDataEntity.label
            else -> throw GenderDataException("Unrecognized gender field $field")
        }
}
```

For `HandleName`,

```kotlin
internal class HandleNameFieldMapper : CustomDataFieldMapper<HandleNameField, HandleNameEntity> {

    override fun valueOf(field: HandleNameField, customDataEntity: HandleNameEntity): String? =
        when (field) {
            HandleNameFields.Handle -> customDataEntity.handle
            else -> throw HandleNameDataException("Unrecognized handle name field $field")
        }
}
```

A few things to note,

- You should throw an instance of your custom data exception in the case that there is no mapping
  from a field to a property. This ensures that your custom data integration will fail and fail-fast
  in case you forget to add a mapping to a property.

## 12. Define the data query function

These (extension) functions on the `DataQueryFactory` allows you and your consumers to use the
`DataQuery` API to specifically query for only your custom data kind instead of Contacts.

For `Gender`,

```kotlin
fun DataQueryFactory.genders(): DataQuery<GenderField, GenderFields, Gender> =
    customData(GenderMimeType)
```

For `HandleName`,

```kotlin
fun DataQueryFactory.handleNames(): DataQuery<HandleNameField, HandleNameFields, HandleName> =
    customData(HandleNameMimeType)
```

For more info on the `DataQuery` API, read
[Query specific data kinds](/docs/data/query-data-sets.md) and
[Query custom data](/docs/customdata/query-custom-data.md).

## 13. Define the custom data entry

The entry puts everything together so that it can be handed off to the custom data registry to
integrate your custom data with all of the APIs provided in the library.

For `Gender`,

```kotlin
internal class GenderEntry : Entry<GenderField, GenderDataCursor, GenderEntity, Gender> {
    override val mimeType = GenderMimeType
    override val fieldSet = GenderFields
    override val fieldMapper = GenderFieldMapper()
    override val countRestriction = GENDER_COUNT_RESTRICTION
    override val mapperFactory = GenderMapperFactory()
    override val operationFactory = GenderOperationFactory()
}
```

For `HandleName`,

```kotlin
internal class HandleNameEntry : Entry<HandleNameField, HandleNameDataCursor, HandleNameEntity, HandleName> {
    override val mimeType = HandleNameMimeType
    override val fieldSet = HandleNameFields
    override val fieldMapper = HandleNameFieldMapper()
    override val countRestriction = HANDLE_NAME_COUNT_RESTRICTION
    override val mapperFactory = HandleNameMapperFactory()
    override val operationFactory = HandleNameOperationFactory()
}
```

## 14. Define the custom data entry registration

The entry registration provides a way for you to keep your Entry `internal` to your library module.

> In Java, the closes thing to this is package-private.

This is not necessary to implement. Feel free to make your `Entry` public so that it can be handed
off to the custom data registry. 

For `Gender`,

```kotlin
class GenderRegistration : CustomDataRegistry.EntryRegistration {
    override fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(GenderEntry())
    }
}
```

For `HandleName`,

```kotlin
class HandleNameRegistration : CustomDataRegistry.EntryRegistration {
    override fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(HandleNameEntry())
    }
}
```

## 15. Register your custom data with the Contacts API instance

There are two ways to register your custom data. Either using the entry registration defined in the
previous step or the entry itself defined in the step prior.

Using `Gender` and `HandleName` entry registration,

```kotlin
val contactsApi = Contacts(
    context,
    customDataRegistry = CustomDataRegistry().register(
        GenderRegistration(),
        HandleNameRegistration()
    )
)
```

Alternatively,

```kotlin
val contactsApi = Contacts(context)
GenderRegistration().registerTo(contactsApi.customDataRegistry)
HandleNameRegistration().registerTo(contactsApi.customDataRegistry)
```

Using `Gender` and `HandleName` entry,

> Note that this is not possible with `Gender` and `HandleName` as their entries are internal.
> This is for demonstration purposes only.

```kotlin
val contactsApi = Contacts(
    context,
    customDataRegistry = CustomDataRegistry().register(
        GenderEntry(),
        HandleNameEntry()
    )
)
```

## 16. Use your custom data in queries, inserts, updates, and deletes

Once you have registered your custom data with the `Contacts` API instance, the API instance is now
able to perform read and write operations on it.

- [Query custom data](/docs/customdata/query-custom-data.md)
- [Insert custom data into new or existing contacts](/docs/customdata/insert-custom-data.md)
- [Update custom data](/docs/customdata/update-custom-data.md)
- [Delete custom data](/docs/customdata/delete-custom-data.md)

## Consider adding your custom data to this library

Let's say that you have created your own custom data in your own app. That's great and all but your
app will be the only app that will be able to perform operations on it (unless the mimetype value
you are using is also used by others). This is definitely something you want to do if you don't
really want others to mess with your custom data (though you can't really stop others).

If you want to add your custom data to this library so that other people using this library can
optionally integrate it into their own apps, please create a GitHub issue and file a pull request!

## Custom data without sync adapters will not be synced

Custom data provided by this library such as those in those in the `customdata-gender`,
`customdata-handlename`, `customdata-pokemon`, and `customdata-rpg` modules are not synced because 
there are no sync adapters and a remote service to store those data. Therefore, they are not synced 
across devices and will remain local to the device regardless of Account sync settings. It is up to
you to implement your own sync adapters for your own custom data. 

For more info, read [Sync contact data across devices](/docs/entities/sync-contact-data.md).

## Displaying your custom data in other Contacts apps

If you want your custom data to be visible in the Android Open Source Project (AOSP) Contacts app 
(the default Contacts app that comes with a vanilla version of Android) and the 
[Google Contacts app][google-contacts], then read this section. This is optional. If you only want
your custom data to be visible in your application, then you should NOT do the things described in
this part of the guide.

> Note that the [Google Contacts app][google-contacts] keeps its "File as" custom data invisible
> to other Contacts apps such as the AOSP Contacts app. However, it exposes the "Custom field+label"
> custom data by doing the things described in this section.

**Important!** The first criteria for being able to show your custom data in the Contacts app is to
define and implement your own sync adapter. If you do not have a sync adapter implementation, your
custom data will not be shown in the Contacts app! Again, this library does not provide any sync
adapters. That is for you to implement based on your account services. This library provides you
and users of your library an easy, uniform way to perform read and write operations on your custom
data. The act of syncing is up to you.

The [official documentation on custom data rows](https://developer.android.com/guide/topics/providers/contacts-provider#CustomData)
is as follows,

> By creating and using your own custom MIME types, you can insert, edit, delete, and retrieve your 
> own data rows in the `ContactsContract.Data` table. Your rows are limited to using the column 
> defined in `ContactsContract.DataColumns`, although you can map your own type-specific column 
> names to the default column names. In the device's contacts application, the data for your rows 
> is displayed but can't be edited or deleted, and users can't add additional data. To allow users 
> to modify your custom data rows, you must provide an editor activity in your own application.
> 
> To display your custom data, provide a `contacts.xml` file containing a `<ContactsAccountType>` 
> element and one or more of its `<ContactsDataKind>` child elements. This is described in more 
> detail in the section `<ContactsDataKind>` element.

Let's break down the official documentation.

- Contacts applications such as the Android Open Source Project (AOSP) Contacts app (the default 
  Contacts app that comes with a vanilla version of Android) and the [Google Contacts app][google-contacts] 
  (and other Contacts app that support this feature) shows custom data from other apps when viewing 
  contact details. 
- Custom data from other apps are viewable but not editable in order to preserve and respect the 
  rules surrounding those custom data managed by other apps. 
  
This library allows you to read (query) and write (insert, update, delete) custom data from other 
apps. It is up to you whether you want to follow the same limitations imposed by the AOSP and 
Google Contacts app.

In order to show your custom data in the AOSP Contacts app and Google Contacts app (and other 
Contacts app that support this feature), you must add an xml file in your app;
[`res/xml/contacts.xml`][contacts-xml]. The [`res/xml/contacts.xml`][contacts-xml] template looks 
like this,

```xml
<ContactsAccountType
        xmlns:android="http://schemas.android.com/apk/res/android"
        inviteContactActivity="activity_name"
        inviteContactActionLabel="invite_command_text"
        viewContactNotifyService="view_notify_service"
        viewGroupActivity="group_view_activity"
        viewGroupActionLabel="group_action_text"
        viewStreamItemActivity="viewstream_activity_name"
        viewStreamItemPhotoActivity="viewphotostream_activity_name">
    <ContactsDataKind
        android:mimeType="MIMEtype"
        android:icon="icon_resources"
        android:summaryColumn="column_name"
        android:detailColumn="column_name" />
</ContactsAccountType>
```

The full official documentation for each of those tags and attributes within each tag are available
by [clicking this link][contacts-xml]. 

For example, the bare-minimum `contacts.xml` for showing `Gender` and `HandleName` custom data in
the AOSP and Google Contacts app is the following,

```xml
<ContactsAccountType xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Gender -->
    <ContactsDataKind
        android:mimeType="vnd.android.cursor.item/contacts.entities.custom.gender"
        android:summaryColumn="data2"
        android:detailColumn="data3" />

    <!-- HandleName -->
    <ContactsDataKind
        android:mimeType="vnd.android.cursor.item/contacts.entities.custom.handlename"
        android:summaryColumn="data1" />

</ContactsAccountType>
```

A few things to note,

- The value of `android:mimeType` corresponds to the String value defined in `GenderMimeType` and
  `HandleNameMimeType` as seen in the previous sections of this guide.
- The value of `android:summaryColumn` and `android:detailColumn` corresponds to the 
  values defined in `contacts.core.Fields.kt#AbstractCustomDataField.ColumnName` that are used by
  `GenderFields` and `HandleNameFields`.
    - These values, as raw strings, are; `data1`, `data2`, `data3`,...`data15`
    
Again, in order for your custom data to be shown in the Contacts app, you must also provide a sync
adapter implementation. For more info, read [Sync contact data across devices](/docs/entities/sync-contact-data.md).

## Summary of limitations

To reiterate, this library does not provide a remote server or sync adapters to interface with
that server. This library provides create (insert), read (query), update, and delete (CRUD) APIs
for pretty, type-safe, and well-documented read and write operations on all data kinds, including
custom data. 

This means that if you do not implement your own sync adapter for your custom data, then your 
custom data...

- will NOT be synced across devices
- will NOT be shown in AOSP and [Google Contacts][google-contacts] apps, and other Contacts apps
  that show custom data from other apps
  
You may still do creative things with custom data without sync adapters as long as you understand
these limitations.

This library provides CRUD API integration with custom data with no sync adapters;

- [`customdata-gender`](/docs/customdata/integrate-gender-custom-data.md)
- [`customdata-handlename`](/docs/customdata/integrate-handlename-custom-data.md)
- [`customdata-pokemon`](/docs/customdata/integrate-pokemon-custom-data.md)
- [`customdata-rpg`](/docs/customdata/integrate-rpg-custom-data.md)

Also provided are CRUD API integration with custom data from other apps that do have sync adapters;

- [`customdata-googlecontacts`](/docs/customdata/integrate-googlecontacts-custom-data.md)

> Please update the above list whenever adding new custom data modules.

[google-contacts]: https://play.google.com/store/apps/details?id=com.google.android.contacts
[contacts-xml]: https://developer.android.com/guide/topics/providers/contacts-provider#ContactsFile