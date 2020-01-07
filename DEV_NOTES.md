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

#### Contacts; Display Name

The Contacts display name may be different than the Data `StructuredName` display name! If a 
structured name in the Data table is not provided, then other kinds of data will be used as the 
`Contacts` row display name. For example, if an email is provided but no structured name then the
display name will be the email. When a structured name is inserted, the Contacts Provider 
automatically updates the Contacts row display name.

If no data rows suitable to be a display name are available, then the Contacts row display name will
be null. Data suitable to be a Contacts row display name are enumerated in `DisplayNameSources`;
 
- company name
- email address
- nickname
- phone number
- structured name

Data not suitable to be a display name are;

- address
- event
- group
- im
- note
- relation
- sip
- website

The kind of data used as the display for the Contact is set in 
`ContactNameColumns.DISPLAY_NAME_SOURCE`.

#### Sync Adapters

This library does not add any custom sync adapters to keep it short and sweet. This relies on the
default system sync settings and functions.

## Java Support

This library is intended to be Java-friendly. The policy is that we should attempt to write 
Java-friendly code that does not increase lines of code by much or add external dependencies to 
cater exclusively to Java consumers.

## Creating Entities

Entities (e.g. `MutableContact`) are done my constructing an instance using the default no-parameter
constructor. Then, the individual attributes / properties are set afterwards (Kotlin's `apply` 
comes in handy here). The constructor is made internal so that consumers do not have the option
to set read-only and private or internal variables such as `id` and `rawId`. 

> FIXME? Make constructors public if Kotlin ever supports private setter for an attribute in the 
> constructor. 
> See https://discuss.kotlinlang.org/t/private-setter-for-var-in-primary-constructor/3640.

HOWEVER! Consumers are still able to set read-only and private or internal variables though because 
all `Entity` classes are data classes. Data classes provide a `copy` function that allows for 
setting any property no matter their visibility and even if the constructor is private. As a matter
of fact, setting the constructor of a `data class` as `private` gives this warning by Android
Studio: "Private data class constructor is exposed via the 'copy' method.

There is currently no way to disable the `copy` function of data classes (that I know of). The only
thing we can do is to provide documentation to consumers, insisting against the use of the `copy`
method as it may lead to unwanted modifications when updating and deleting contacts.

> We could just use regular classes instead of data classes but entities should be data classes
> because it is what they are (know what I mean?!).
> FIXME? Hide / disable data class `copy` function if kotlin ever allows it. 

## Mutable Entities? 

Why not use a more conventional builder pattern? Why "mutable"? Because it's the simplest way to 
implement a form of the builder pattern without having the write builder code or adding a dependency
on Google's `AutoValue`. Furthermore, having "mutable" entities as data classes with all properties
defined in the constructor allows it to be `Parcelize`d. Besides, one of the main benefits of the
conventional builder pattern really only benefits Java consumers. That is function chaining. Kotlin 
consumers may just use `apply` (and other similar ones).

## Why Not Add Android X / Support Library Dependency?

I want to keep the dependency list of this library to a minimum. The Contacts Provider is native to
Android since the beginning. I want to honor that fact by avoiding adding dependencies here. I made
a bit of an exception by adding the [Dexter library][2] for permissions handling for the permissions
modules (not in the core modules). I'm tempted to remove the Dexter dependency and implement 
permissions handling myself because Dexter brings in a lot of other dependencies with it. However,
it is not part of the core `contacts` module so I'm able to live with this.

[1]: https://developer.android.com/guide/topics/providers/contacts-provider
[2]: https://github.com/Karumi/Dexter
