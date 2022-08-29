# Local (device-only) contacts

Contacts, or more specifically RawContacts, that are not associated with an
`android.accounts.Account` are local to each device and will not be synced across devices.

This means that any RawContacts you create, update, or delete will NOT be synced on any device or
remote service as it is not associated with any account.

> ℹ️ For more info, read [Sync contact data across devices](./../entities/sync-contact-data.md).

## Associating a local RawContact to an Account

Local RawContacts can be associated to an Account to enable syncing.

For more info, read [Associate local RawContacts to an Account](./../accounts/associate-device-local-raw-contacts-to-an-account.md).

## Adding an Account to the device

Depending on the API level, the Contacts Provider behaves differently when the user adds an account
to the device.

**Lollipop (API 22) and below**

When an Account is added, from a state where no accounts have yet been added to the system, the
Contacts Provider automatically sets all of the null `accountName` and `accountType` in the
RawContacts table to that Account's name and type.

RawContacts inserted without an associated account will automatically get assigned to an account if
there are any available. This may take a few seconds, whenever the Contacts Provider decides to do
it. Dissociating RawContacts from Accounts will result in the Contacts Provider associating those
back to an Account.

**Marshmallow (API 23) and above**

The Contacts Provider no longer associates local RawContacts to an account when an account is or
becomes available. Local contacts remain local.

**Account removal**

Removing the Account will remove all of the associated rows in the RawContact, Data, and Groups 
tables **locally**. This includes user Profile data in those tables.

> ℹ️ When all RawContacts of a Contact is removed, the Contact is also automatically removed by the 
> Contacts Provider.

## Data kinds Account restrictions

The native Contacts app hides the following UI fields when inserting or updating local raw contacts. 
To enforce this behavior, this library ignores the following fields during inserts and updates for 
local raw contacts.

- `GroupMembership`
    - Groups can only exist if it is associated with an Account. Therefore, memberships to groups is
      not possible when there is no associated Account.
- `Event`
    - It is not clear why this requires an associated Account. Maybe because these are typically
      birth dates that users expect to be synced with their calendar across devices?
- `Relation`
    - It is not clear why this requires an associated Account...

The Contacts Provider may or may not enforce these Account restrictions. However, the native
Contacts app imposes these restrictions. Therefore, this library also imposes these restrictions and
disables consumers from violating them.

> ⚠️ As of [version 0.3.0](https://github.com/vestrel00/contacts-android/discussions/218), these
> [restrictions will be removed](https://github.com/vestrel00/contacts-android/issues/167). You 
> will be able insert or update all of the above fields for local raw contacts.

## Samsung Phone contacts

In most flavors of Android, a local (device-only) RawContact have null Account name and type in
the RawContacts table. However, Samsung phones use `vnd.sec.contact.phone` to fill the Account
name and type in the RawContacts table for local RawContacts (referred to as "Phone" in the 
Samsung Contacts app).

The `vnd.sec.contact.phone` does NOT refer to an actual `android.accounts.Account`. It is not 
returned by the `android.accounts.AccountManager`. 

In short, Samsung devices use `vnd.sec.contact.phone` instead of null for local RawContacts.

In Samsung devices, RawContacts that are inserted with a null account will, immediately or at a 
later time, be automatically associated with the `vnd.sec.contact.phone`.

In order to query for local RawContacts on Samsung devices, you do not have to do anything 
different. Just pass in null as usual;

```kotlin
query.accounts(null)
```

When a null is provided, all query APIs will internally additionally add 
`Account("vnd.sec.contact.phone", "vnd.sec.contact.phone")`.

> ⚠️ This internal fix is available as of [version 0.2.4](https://github.com/vestrel00/contacts-android/discussions/248).
> Prior versions will require you to pass in `Account("vnd.sec.contact.phone", "vnd.sec.contact.phone")`
> in addition to `null` when using `accounts` for matching local contacts.