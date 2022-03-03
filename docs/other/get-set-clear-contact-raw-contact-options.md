# Get set Contact options

This library provides several functions to interact with Contact and RawContact options;
starred, send to voicemail, and ringtone.

## Contact and RawContact options affect each other

Changes to the options of the parent Contact will be propagated to all child RawContact options.
Changes to the options of a RawContact may or may not affect the options of the parent Contact.

## Getting contact options

To get Contact options,

```kotlin
val options = contact.options(contactsApi)
```

To get RawContact options,

```kotlin
val options = rawContact.options(contactsApi)
```

## Setting contact options

To set Contact options,

```kotlin
contact.setOptions(contactsApi, mutableOptions)
```

To set RawContact options,

```kotlin
rawContact.setOptions(contactsApi, mutableOptions)
```

For example, to set a contact to be starred (favorited),

```kotlin
contact.setOptions(contactsApi, mutableOptions.apply {
    starred = true
})
```

The `setOption` function takes in an arbitrary `Options` instance. If you instead want to modify
the options of a Contact or RawContact retrieved from the database,

```kotlin
contact.updateOptions(contactsApi) {
    starred = true
}
```

This is useful if you only want to set certain properties and keep other properties the same.

## Changes are immediate and are not applied to the receiver

These apply to set and update functions.

1. Changes are immediate.
    - These functions will make the changes to the Contacts Provider database immediately. You do
      not need to use update APIs to commit the changes.
2. Changes are not applied to the receiver.
    - This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
      query APIs or refresh extensions or process the result of this function call to get the most
      up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
      Provider database.

## Using the ui RingtonePicker extensions

The `contacts.ui.util.RingtonePicker.kt` in the `ui` module` provides extension functions to make
selecting existing ringtones easier. It provides you the same UX as the native Contacts app. 

To use it,

```kotlin
Activity {
    fun onSelectRingtoneClicked() {
        selectRingtone(contact.options?.customRingtone)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onRingtoneSelected(requestCode, resultCode, data) { ringtoneUri -> 
            contact.updateOptions(contactsApi) {
                customRingtone = ringtoneUri
            }
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

## Performing options management asynchronously

All of the code shown in this guide are done in the same thread as the call-site. This may result
in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing options management with permission

Getting and setting options require the `android.permission.READ_CONTACTS` and
`android.permission.WRITE_CONTACTS` permissions respectively. If not granted, getting and setting 
options will fail.

TODO Update this section as part of issue [#120](https://github.com/vestrel00/contacts-android/issues/120).
      
## Starred in Android (Favorites)

When a Contact is starred, the Contacts Provider automatically adds a group membership to the
favorites group for all RawContacts linked to the Contact. Setting the Contact starred to false
removes all group memberships to the favorites group.

The Contact's "starred" value is interdependent with group memberships to the favorites group.
Adding a group membership to the favorites group results in starred being set to true. Removing
the membership sets it to false.

Raw contacts that are not associated with an account do not have any group memberships. Even
though these RawContacts may not have a membership to the favorites group, they may still be
"starred" (favorited), which is not dependent on the existence of a favorites group membership.

**Refresh RawContact instances after changing the starred value.** Otherwise, performing an
update on the RawContact with a stale set of group memberships may revert the star/unstar
operation. For example,

-> query returns a starred RawContact
-> set starred to false
-> update RawContact (still containing a group membership to the favorites group)
-> starred will be set back to true.

## FAQs

### Can contacts be inserted with options

> Related issues; #120

You cannot get/set/update options for Contacts/RawContacts that have not yet been inserted in the
Contacts Provider database. In other words, only Contacts/RawContacts retrieved via query or result
APIs can use the extension functions in `contacts.core.util.ContactOptions.kt` and
`contacts.core.util.RawContactOptions.kt`.

To insert a new contact "with options", you should insert the contact first. Then, if the insert
succeeds, proceed to set the options.

> For more info about insert, read [Insert contacts](./../basics/insert-contacts.md).
