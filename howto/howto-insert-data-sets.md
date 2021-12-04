# How do I create/insert data into new or existing contacts?

Data can only be created/inserted into the database whenever inserting or updating new or existing 
contacts.

When using insert and update APIs such as `Insert`, `ProfileInsert`, `Update`, and `ProfileUpdate`,
you are able to create/insert data into new or existing RawContacts respectively.

For example, to insert an email into a new contact using the `Insert` API,

```kotlin
Contacts(context)
    .insert()
    .rawContact {
        addEmail(email)
    }
    .commit()
```

> For more info, read [How do I create/insert contacts?](/howto/howto-insert-contacts.md)

To insert an email into a new Profile contact using the `ProfileInsert` API,

```kotlin
Contacts(context)
    .profile()
    .insert()
    .rawContact {
        addEmail(email)
    }
    .commit()
```

> For more info, read [How do I create/insert the device owner Contact profile?](/howto/howto-insert-profile.md)

To insert an email into an existing contact using the `Update` API,
 
 ```kotlin
Contacts(context)
    .update()
    .contacts(existingContact.mutableCopy().apply {
        addEmail(email)
    })
    .commit()
 ```
 
 > For more info, read [How do I update contacts?](/howto/howto-update-contacts.md)

To insert an email into an the existing Profile Contact using the `ProfileUpdate` API,
 
 ```kotlin
Contacts(context)
    .profile()
    .update()
    .contact(existingProfileContact.mutableCopy().apply {
        addEmail(email)
    })
    .commit()
 ```
 
 > For more info, read [How do I update the device owner Contact profile?](/howto/howto-update-profile.md)


## Blank data are not inserted

Blank data are data entities that have only null, empty, or blank primary value(s). Blanks are 
ignored and are not inserted by insert APIs.

For more info, read [How do I learn more about "blank" data?](/howto/howto-learn-more-about-blank-data.md)