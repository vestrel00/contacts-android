# How do I learn more about "local" (device-only) contacts?

Contacts, or more specifically RawContacts, that are not associated with an
`android.accounts.Account` are local to each device and will not be synced across devices.

This means that any RawContacts you create, update, or delete will NOT be synced on any device or
remote service as it is not associated with any account.

> For more info, read [How do I sync contact data across devices?](/contacts-android/howto/howto-sync-contact-data.html)

## Data kinds Account restrictions

Entries of some data kinds should not be allowed to exist for local RawContacts.

These data kinds are;

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
