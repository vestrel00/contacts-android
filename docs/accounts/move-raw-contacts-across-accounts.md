# Move RawContacts across Accounts

TODO

------------------------

## Developer notes (or for advanced users)

TODO Multiple RawContacts linked to a single Contact

Let's take a look at what happens when **Google Contacts** moves RawContacts to different Accounts.
Other apps such as AOSP Contacts and Samsung Contacts may do things a bit differently. We will
focus on modelling API behavior after Google Contacts behavior because... it's Google. For testing,
I used a Pixel 4 API 30 emulator and a Samsung Galaxy A71 API 33.

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

**Group memberships** (which are Account-based) are "carried over" on a best-effort basis;

- Groups with matching title (case-sensitive)
- Default Group (autoAdd is true)
- Favorites Group (if starred is true)

**Default/primary** flags of Data rows are not retained. For example, if a phone number is set
as the default (isPrimary: 1, isSuperPrimary: 1), after this move operation it will no longer
be a default data (isPrimary: 0,	isSuperPrimary: 0). _Yes, like all other behaviors of this API,
this is the same as Google Contacts._

Contact **IDs** and **lookup keys** may change. This means that references to Contact IDs and
lookup keys may become invalid. For example, shortcuts may break after performing this
operation.

**(Raw)Contact links** (AggregationExceptions) are also retained, in some cases. For example,
if there are two RawContacts linked to the same Contact and one RawContact is moved to device 
(no Account), a new RawContact is created, the original RawContact is deleted, and the two 
RawContacts are still linked together. However, in the case where there are two RawContacts (one 
having no Account) linked to the same Contact and the device RawContact is moved to the same Account
as the sibling RawContact, the two RawContacts are no longer linked to the same Contact. This may
or may not be intentional. Regardless, we should fix it =)