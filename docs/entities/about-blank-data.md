# Blank data

Blank data are data entities that have only null, empty, or blank primary value(s). 

An entity is blank if the concrete implementation of `Entity.isBlank` returns true.

For example, `Email` only has one primary value, which is the `address`...

```kotlin
val blankEmail1 = NewEmail()
val blankEmail2 = NewEmail(
    address = null
)
val blankEmail3 = NewEmail(
    address = ""
)
val blankEmail4 = NewEmail(
    address = "   "
)
val blankEmail5 = NewEmail(
    type = EmailEntity.Type.HOME
)

val emailThatIsNotBlank = NewEmail(
    address = "john.doe@gmail.com"
)
```

Query APIs in this library do not return null, empty, or blank data in results if they somehow 
exist in the Contacts Provider database. Insert APIs also ignore blanks and are not inserted.
Update APIs deletes blanks.

This is the same behavior as the native Contacts app. This library does not allow you to modify this
behavior.

## Blank Data vs blank Contacts/RawContacts

Blank data are data entities that have only null, empty, or blank primary value(s).

Blank RawContacts and blank Contacts do not have any rows in the Data table. These do not have any 
non-blank data.

> For more info, read about [Blank contacts](/docs/entities/about-blank-contacts.md).