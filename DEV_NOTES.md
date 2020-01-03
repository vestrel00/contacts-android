# Developer Notes

This document contains useful developer notes that should be kept in mind during development. It 
serves as a memory of all the quirks and gotcha's of things like Android's `ContactsContract`.

> I thought about putting this in CONTRIBUTING.md but I think it's better that it is a separate 
document.

## Contacts Provider

It is important to know about the ins and outs of Android's Contacts Provider. After all, this API 
is just a wrapper around it. 

> A very sweet, sugary wrapper! Sugar. Spice. And everything nice. :D

It is important to get familiar with the [official documentation of the Contact's Provider]{1]. 

Here is a summary;

There are 3 database tables used in dealing with contacts;

1. Contacts
2. RawContacts
3. Data

All of these tables and their fields are enumerated and documented in
`android.provider.ContactsContract`. 

Each table serves a different purpose;

1. Contacts
    - Rows representing different people.
2. RawContacts
    - Rows that link Contacts rows to specific Accounts.
3. Data
    - Rows containing data (e.g. name, email) for a RawContacts row.
    
These tables contain the following (notable) information (columns);

1. `Contacts`
    - `_ID`
    - `DISPLAY_NAME_PRIMARY`
2. `RawContacts`
    - `_ID`: the `Contacts._ID`
    - `ACCOUNT_NAME`: the `Account.name`
    - `ACCOUNT_TYPE` the `Account.type`
3. `Data`
    - `RAW_CONTACT_ID`: the `RawContacts._ID`
    - `CONTACT_ID`: the `Contacts._ID`
    - `DATA_1` to `DATA_15`: contains a piece of contact data 
      (e.g. first and last name, email address and type) determined by 
      the `MIMETYPE`
    - `MIMETYPE`: the type of data that this row's `DATA_X` columns contain
      (e.g. name and email data)
      
The tables are connected the following way;

- RawContacts contains a reference to the Contacts row Id.
- Data contains a reference to the RawContacts row Id and Contacts row Id. 

[1]: https://developer.android.com/guide/topics/providers/contacts-provider
