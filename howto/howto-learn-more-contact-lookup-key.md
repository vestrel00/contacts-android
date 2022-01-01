# How do I learn more about the Contact lookup key vs ID?

The _Contact ID_ is a number in the Contacts table that serves as the _unique identifier for a row
in the **local** Contacts table_. These look like any number used as an ID in a database table. For
example; `4`, `8`, `15`, `16`, `23`, `42`, ...

The _Contact lookup key_ is a string that serves as the _unique identifier for a Contact in the
**local and remote** databases_. These look like randomly generated or hashed strings. For example;
`2059i4a27289d88a0a4e7`, `2059i7820d35c8f2c1b1a`, ...

The official documentation for the Contact lookup key is,

> An opaque value that contains hints on how to find the contact if its row id changed as a result
> of a sync or aggregation.

Let's dissect the documentation,

- "if its row id changed".
    - This means that a Contact's row ID can change!
- "as a result of a sync".
    - The Contacts Provider allows sync adapters to modify the local and remote Contacts databases
      to ensure that Contact data is synced per user account.
- "as a result of...aggregation".
    - Two or more Contacts (along with their constituent RawContacts) can be linked into a single
      Contact. When this happens, those Contacts will be consolidated into a single (existing)
      Contact row. Unlinking will result in the original Contacts prior to linking to have different
      IDs in the Contacts table because the previously deleted row IDs cannot be reused.

Unlike the Contact ID, the lookup key is the same across devices and will not change due to sync or
aggregation. The lookup key points to a Contact entity rather than just a row in a table. It is the
unique identifier used by local and remote sync adapters to identify an aggregate Contact.

> For more info on how to sync contact data, read [How do I sync contact data across devices?](/howto/howto-sync-contact-data.md)

## When to use lookup key vs ID?

TODO

## RawContacts do not have lookup key

Lookup key is specific to Contact entities, which can be made up of one or more RawContacts.

## Local (no Account) contacts and the lookup key

TODO

> For more info on local contacts, read [How do I learn more about "local" (device-only) contacts?](/howto/howto-learn-more-about-local-contacts.md)

## Linking/unlinking Contacts and the lookup key

TODO

> For more info on linking/unlinking, read [How do I link/unlink Contacts?](/howto/howto-link-unlink-contacts.md)
