# Move RawContacts across Accounts

TODO

------------------------

## Developer notes (or for advanced users)

TODO Multiple RawContacts linked to a single Contact

Let's take a look at what happens when **Google Contacts** moves RawContacts to different Accounts.
Other apps such as AOSP Contacts and Samsung Contacts may do things a bit differently. We will
focus on modelling API behavior after Google Contacts behavior because... it's Google.

| **Original Account** | **Target Account** |
|----------------------|--------------------|
| null (local)         | X                  |
| X                    | null (local)       |
| X                    | Y                  |

For all of the above scenarios, the behavior is as follows...

- New row in RawContacts table with same values as original
  - Original row is deleted
- New row in Contacts table as the parent of the new RawContact
  - Original row is deleted
- New rows in the Data table with same values as original, belonging to the new RawContact
  - Original rows are deleted

Memberships to Groups from the original Account are "carried over" for Groups in the target Account
that have a matching title (case-sensitive). Group memberships to Account X's
default ("My Contacts" - autoAdd is true) Group and favorites ("Starred in Android") Group are also
"carried over".