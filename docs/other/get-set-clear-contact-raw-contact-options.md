# Get set contact options

This library provides several functions to interact with Contact and RawContact options; starred,
send to voicemail, and ringtone.

> ⚠️ The APIs for this have changed significantly since [version 0.3.0](https://github.com/vestrel00/contacts-android/releases/tag/0.3.0).
> For documentation for [version 0.2.4](https://github.com/vestrel00/contacts-android/releases/tag/0.2.4)
> and below, [visit this page (click me)](https://github.com/vestrel00/contacts-android/blob/0.2.4/docs/other/get-set-clear-contact-raw-contact-options.md).

## Getting contact options

To get Contact and RawContact options using query APIs provided in this library,

```kotlin
val contacts = Contacts(context)
    .query() // or broadQuery() and other query APIs
    // if you only want to include only options in the returned Contacts and RawContacts
    .include(Fields.Contact.Options.all)
    .includeRawContactsFields(RawContactsFields.Options.all)
    .find()

for (contact in contacts) {
    Log.d("Contact", "${contact.options}")
    for (rawContact in contacts.rawContacts) {
        Log.d("RawContact", "${rawContact.options}")
    }
}
```

> ℹ️ For more info on query APIs, read [Query contacts](./../basics/query-contacts.md) and
> [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

## Setting Contact options

To update Contact options using update APIs,

```kotlin
Contacts(context)
    .update()
    .contacts(
        contact.mutableCopy {
            setOptions {
                starred = true
                customRingtone = null
                sendToVoicemail = false
            }
        }
    )
    .commit()
```

To insert a new RawContact with options using insert APIs,

```kotlin
Contacts(context)
    .insert()
    .rawContact(
        setOptions {
            starred = true
            customRingtone = null
            sendToVoicemail = false
        }
    )
    .commit()
```

The inserted RawContact and parent Contact will contain the specified options.

> ℹ️ For more info on insert and APIs, read [Insert contacts](./../basics/insert-contacts.md) and
> [Update contacts](./../basics/update-contacts.md).

### Contact and RawContact options affect each other

Changes to the options of the parent Contact will be propagated to all child RawContact options.
Changes to the options of a RawContact may or may not affect the options of the parent Contact. This
propagation is done automatically by the Contacts Provider at the time the insert or update APIs
provided in this library are committed.

Typically, you should only read/write Contact options. Don't mind RawContact options, unless you
really want to. For example,

- the AOSP Contacts app only allows reading and writing Contact options.
- the Google Contacts app allows reading and writing Contact and RawContact options.

### Setting RawContact options

Due to the aforementioned relationship of Contact and RawContact options, the update APIs provided
in this library will prioritize Contact options over RawContact options. This means that any changes
you make to RawContact options will be overshadowed by Contact options.

If you want to set RawContact options, then you should pass in the RawContact directly using the
`rawContacts` function instead of passing in the Contact using the `contacts` function,

```kotlin
Contacts(context)
    .update()
    .rawContacts(
        rawContact.mutableCopy {
            setOptions {
                starred = true
                customRingtone = null
                sendToVoicemail = false
            }
        }
    )
    .commit()
```

If you must pass Contacts instead of RawContacts and still want to prioritize RawContact options
over Contact options, then you must exclude Contact options fields from the update operation,

```kotlin
Contacts(context)
    .update()
    // Include all fields except for Contact options.
    .include(Fields.all.minus(Fields.Contact.Options.all))
    .contacts(contacts)
    .commit()
```

> ℹ️ For more info on field includes, read
> [Include only certain fields for read and write operations](./../basics/include-only-desired-data.md).

### Starred in Android & Favorites Group Membership

When a Contact is starred, the Contacts Provider automatically adds a group membership to the
favorites group for all RawContacts linked to the Contact. Setting the Contact starred to false
removes all group memberships to the favorites group.

The Contact's "starred" value is interdependent with memberships to the favorites group. Adding a
membership to the favorites group results in starred being set to true. Removing the membership sets
it to false. This behavior can cause bugs and increased code complexity for API users. 

Thus, the update APIs provided in this library overshadows membership changes to the favorites group
with the value of `Options.starred`. In other words, the only way to star/favorite Contacts and
RawContacts is to set the value of `Options.starred`. If you really want to star/favorite
Contacts/RawContacts via membership to the favorites group (not recommended), then you must 
include `Fields.GroupMembership.GroupId` and also exclude `Fields.Contact.Options.Starred` and 
`RawContactsFields.Options.Starred` from the insert/update operations.

## Using the ui RingtonePicker extensions

The `contacts.ui.util.RingtonePicker.kt` in the `ui` module` provides extension functions to make
selecting existing ringtones easier. It provides you the same UX as the AOSP Contacts app.

To use it,

```kotlin
Activity {
    fun onSelectRingtoneClicked() {
        selectRingtone(contact.options?.customRingtone)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onRingtoneSelected(requestCode, resultCode, data) { ringtoneUri ->
            // set contact options customRingtone = ringtoneUri
        }
    }
}
```

Starting with Android 11 (API 30), you must include the following to your manifest in order to
successfully use the above functions.

```
<queries>
    <intent>
        <action android:name="android.intent.action.RINGTONE_PICKER" />
    </intent>
</queries>
```
