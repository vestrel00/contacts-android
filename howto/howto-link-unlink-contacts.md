# How do I link/unlink Contacts?

TODO

Note for people looking at this file, the code and documentation within the code is already
complete. I'm in the process of writing these howto pages to provide examples and more explanations
on how to use the APIs provided in this library.

Make sure to take from and update documentation in the DEV_NOTES section
"Behavior of linking/merging/joining contacts (AggregationExceptions)"!

## Effects of linking/unlinking Contact ID

When two or more Contacts (along with their constituent RawContacts) are linked into a single
Contact those Contacts will be merged into one of the existing Contact row. The Contacts that have
been merged into the single Contact will have their entries/rows in the Contacts table deleted.

Unlinking will result in the original Contacts prior to linking to have new rows in the Contacts
table with different IDs because the previously deleted row IDs cannot be reused.

Getting Contacts that have been linked into a single Contact or Contacts whose row IDs have change
after unlinking is still possible using the Contact lookup key.

For more info, read [How do I learn more about the Contact lookup key vs ID?](/howto/howto-learn-more-contact-lookup-key.md)