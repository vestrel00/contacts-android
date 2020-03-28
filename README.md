## TODO

1. Create Contacts().queryData (with permission and asynchronous extensions) that returns only one 
   data kind (eg emails, phones, etc). Useful for ordering and paginating of only one data kind.
2. Setup user profile  
    - `ContactsContract.ContactsColumns.IS_USER_PROFILE`
    - `ContactsContract.RawContactsColumns.RAW_CONTACT_IS_USER_PROFILE`
    - `ContactsContract.Profile`
3. Review all ContactsContract code and assess what else should be added to code, README, or DEV_NOTES.
    - SettingsColumns? (group visible / invisible)
4.  Support for adding custom mimetypes (e.g. vnd.com.whatsapp.profile).
5. Add Copyright to all non-sample files.

----------------------------------------------------------------------------------------------------

#### Complete sample app

Build the sample app as simple as possible, referencing new and older versions to ensure all
functionality is included (though may look different). Document that the sample app design is more 
based on older versions; API Nougat and below.

- View Contact
    - Star (favorite) contact
    - Edit
    - Join/Separate (API 22-), Merge/Unmerge (API 23), Link/unlink (API 24+) Contacts.
    - Delete
    - Share (post v1)
    - Create shortcut / Place on Home screen
    - Set ringtone
    - Route to voicemail / All calls to voicemail
    - Press action for each data
    - Long press options for each data
      - Data
      - Copy to clipboard
      - Set default / clear default (isSuperPrimary)
      
- Edit Contact / RawContact
    - Saving to which account (uneditable)
    - Combined edit mode (editing multiple linked raw contacts) available in API 24 but removed in
      API (28?) and not in API 22. Try all supported API levels! Choose behavior of simplest API. 
      Document this.
    - Save
    - Discard changes
    
- Create contact
    - Saving to which account (editable)
        - What happens to fields (groups) that have been filled out when different account is picked?
        
- Contacts list
    - Long press options
      - Share (post v1)
      - Delete
      - Link (when multiple selected)
                
- View starred (favorites only) contacts

- Set up user (my) profile
    
- No account & no contacts screen
    - Add account

- Contacts to display
    - All contacts
    - ... account(s) ...
    - Customize (Define custom view)
        - ... account(s) ...
            - ... groups ... 
                - Long press -> Remove sync group
                - More groups... (Add sync group)
                - All other contacts
                
- Manage accounts
    
----------------------------------------------------------------------------------------------------

#### Tidy up

1. Lint / code quality checks.
2. Unit test.
3. Espresso test.
    - Test RAM usage when paginating hundreds of thousands of contacts.

#### Final steps!

1. Review remaining TODOs and FIXMEs.
2. Provide usage documentation in README and other places as needed.
3. Update AS, Kotlin, Gradle, Dexter, and all other dependencies before releasing.
4. Publish artifact AND write/publish medium blog to AndroidPub AND promote via AndroidWeekly.
5. Immediately setup Travis CI (free for public repos).

----------------------------------------------------------------------------------------------------

# Contacts

An easy way to insert, query, update, delete contacts in idiomatic Kotlin (and Java).

## Usage

TODO


## Upcoming features!

These features didn't make the v1 release because I wanted to get this library out as soon as 
possible so that the community may benefit from it and contribute back!

Most, if not all, of these upcoming features are the missing components required to rebuild the
native Android Contacts app from the ground up. In other words, each of these features allow 
consumers to implement a specific part of the native Android Contacts app.

1. SIM card query, insert, update, and delete.
    - Enables importing from and exporting to SIM card.
    - Query will definitely be possible. I'm not sure if insert, update, and delete operations
      are possible. We will see.
2. Contacts read/write .VCF file.
    - Enables import from and export to .VCF file.
    - Enables sharing a contact.
    - Dev note: search ContactsContract for "vcard".
    
## Features that will not be implemented

This library aims to provide functions to read from and write to the Contacts Provider. Only 
functions that are directly related to the manipulation of Contacts Provider data are provided in
this library. Therefore, these features will not be implemented here.

1. Blocking phone numbers.
    - The Android 7.0 release introduces a BlockedNumberProvider content provider that stores a list
      of phone numbers the user has specified should not be able to contact them via telephony 
      communications (calls, SMS, MMS).
    - See https://source.android.com/devices/tech/connect/block-numbers

## Best Practices

#### Do not use `copy` function of `Entity` classes.

All `Entity` classes such as `Name` and `Email` are `data class`es whose constructor are `internal`.
The constructors are internal in order to prevent consumers from setting internal, private, or
read-only properties, which lessens the risks of unwanted side effects when inserting, updating, or
deleting entities. However, Kotlin data classes have a `copy` function that allows consumers to set
any of the properties that are meant to be hidden even if the constructor or the properties are 
private.

Until Kotlin allows for hiding or disabling the `copy` function, the only thing this library can do
is document this and hope that consumers follow this practice. We are "consenting adults" =)
