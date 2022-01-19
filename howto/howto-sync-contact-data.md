# How do I sync contact data across devices?

Syncing contact data, including groups, are done automatically by the Contacts Provider depending on
the account sync settings. You can typically find these account sync settings via
_Settings -> Accounts -> <account> -> Account sync -> "Contacts"_. Of course, in addition to having
Contacts syncing enabled in settings, you must also have network connection to sync between the
device and remote servers.

![Account sync settings](/media/account-sync-settings.png)

When you have Contacts syncing enabled, as long as the `android.accounts.Account` has active sync
adapters and remote services and you have network connection, data belonging to that account (e.g.
"vestrel00@gmail.com" is a Google account) are synced across devices and online. This means that any
contacts you create, update, or delete will be synced on all devices and services associated with
that account.

> Besides Google Accounts, there is also Samsung, Yahoo, MSN/Hotmail, etc.

Syncing contacts across devices is possible with sync adapters and Contacts' lookup key.

> For more info, read [How do I learn more about the Contact lookup key vs ID?](/howto/howto-learn-more-contact-lookup-key.md)

## Adding or removing Accounts

When an Account is added to the system and Contacts syncing is enabled and there is network 
connection, the Contacts Provider will automatically fetch all Contacts, RawContacts, Data, and
Groups that belong to that Account.

Similarly, when an Account is removed from the system though regardless of Contacts syncing enabled
or network availability, the Contacts Provider will automatically remove Contacts, RawContacts, 
Data, and Groups that belong to that Account.

## Only contacts that are associated with an Account are synced

More specifically, RawContacts that are not associated with an Account (local, device-only) are not
synced. Syncing is account specific, which is why you must turn on Contact syncing in the system
settings.

For example, data belonging to a RawContact that is associated with a Google account (e.g. Gmail)
will be available anywhere the Google account is used; in any Android or iOS device, a web browser,
etc... Data is synced by Google’s sync adapters between devices and their remote servers. Syncing
depends on the account sync settings, which can be configured in the system settings app and
possibly through some remote configuration.

> For more info, read [How do I learn more about "local" (device-only) contacts?](/howto/howto-learn-more-about-local-contacts.md)

## Some custom data provided in this library are not synced

The `Gender` and `HandleName` custom data will not be synced because they are not account specific
and they have no sync adapters and no remote service to interface with.

> For more info, read [How do I integrate custom data?](/howto/howto-integrate-custom-data.md)

## Custom data from other apps may be synced

This library does not sync contact data that belongs to other apps and services. For example, 
[Google Contacts][google-contacts], WhatsApp, and other apps define their own set of custom data 
that their own sync adapters sync with their own remote services, which requires authentication.

> For more info, read [How do I integrate custom data from other apps?](/howto/howto-integrate-custom-data-from-other-apps.md)

## This library does not provide sync adapters

This library does not have any APIs related to syncing. It is considered out of scope of this
library as it requires access to remote databases and account-specific data. Let's talk about it
though. However, it is good to know how it works if you just want more insight :grin:.

https://developer.android.com/guide/topics/providers/contacts-provider#SyncAdapters

> The Contacts Provider is specifically designed for handling synchronization of contacts data
> between a device and an online service. This allows users to download existing data to a new
> device and upload existing data to a new account. Synchronization also ensures that users have
> the latest data at hand, regardless of the source of additions and changes. Another advantage of
> synchronization is that it makes contacts data available even when the device is not connected to
> the network.
>
> Although you can implement synchronization in a variety of ways, the Android system provides a
> plug-in synchronization framework that automates the following tasks:
>
> - Checking network availability.
> - Scheduling and executing synchronization, based on user preferences.
> - Restarting synchronizations that have stopped.
>
> To use this framework, you supply a sync adapter plug-in. Each sync adapter is unique to a service
> and content provider, but can handle multiple account names for the same service. The framework
> also allows multiple sync adapters for the same service and provider.

This library does not provide any sync adapters. Instead, it relies on existing sync adapters to do
the syncing. Sync adapters and syncing are really out of scope of this library. Syncing is its own
thing that typically happens outside of an application UI. This library is focused on Create, Read,
Update, and Delete (CRUD) operations on native and custom data to and from the local database.
Syncing the local database to and from a remote database in the background is a totally different
story altogether :grin:

> For more info, read [How do I integrate custom data?](/howto/howto-integrate-custom-data.md)

[google-contacts]: https://play.google.com/store/apps/details?id=com.google.android.contacts