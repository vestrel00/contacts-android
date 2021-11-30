# How do I learn more about "blank" contacts?

Blank RawContacts and blank Contacts do not have any rows in the Data table. These do not have any 
non-blank data.

An entity is blank if the concrete implementation of `Entity.isBlank` returns true.

The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call them
"blanks") to exist. The native Contacts app does not allow insertion of new RawContacts without at
least one data row. It also deletes blanks on update. Despite seemingly not allowing blanks, the
native Contacts app shows them. 

> This library provides APIs that follows the native Contacts app behavior by default but also 
> allows you to override the default behavior.

There are two scenarios where blanks may exist.

1. Contact with RawContact(s) with no Data row(s).
    - In this case, the Contact is blank as well as its RawContact(s).
2. Contact that has RawContact with Data row(s) and a RawContact with no Data row.
    - In this case, the Contact and the RawContact with Data row(s) are not blank but the RawContact
    with no Data row is blank.

## Blanks in queries

A `where` clause that uses any fields from the Data table `Fields` will **exclude** blanks in the 
result (even if they are OR'ed) There are some joined fields that can be used to match blanks 
**as long as no other fields are in the where clause**;

- `Fields.Contact` enables matching blank Contacts. The result will include all RawContact(s)
  belonging to the Contact(s), including blank(s). Examples;

  - `Fields.Contact.Id equalTo 5`
  - `Fields.Contact.Id in listOf(1,2,3) and Fields.Contact.DisplayNamePrimary contains "a"`
  - `Fields.Contact.Options.Starred equalTo true`

- `Fields.RawContact` enables matching blank RawContacts. The result will include all Contact(s) 
  these belong to, including sibling RawContacts (blank and not blank). Examples;

  - `Fields.RawContact.Id equalTo 5`
  - `Fields.RawContact.Id notIn listOf(1,2,3)`

Blanks will not be included in the results even if they technically should **if** joined fields 
from other tables are in the `where`. In the below example, matching the `Contact.Id` to an 
existing blank Contact with Id of 5 will yield no results because it is joined by `Fields.Email`, 
which is not a part of `Fields.Contact`. It should technically return the blank Contact with Id of 
5 because the OR operator is used. However, because we internally need to query the Contacts table 
to match the blanks, a DB exception will be thrown by the Contacts Provider because 
`Fields.Email.Address` ("data1" and "mimetype") are columns from the Data table that do not exist 
in the Contacts table. The same applies to the `Fields.RawContact`.

- `Fields.Contact.Id equalTo 5 OR (Fields.Email.Address.isNotNull())`
- `Fields.RawContact.Id ... OR (Fields.Phone.Number...)


## Blank Contacts/RawContacts vs blank Data

Blank RawContacts and blank Contacts do not have any rows in the Data table. These do not have any 
non-blank data.

Blank data are data entities that have only null, empty, or blank primary value(s).

> For more info, read [How do I learn more about "blank" data?](/howto/howto-learn-more-about-blank-data.md)