# Blocked numbers

The [Android 7.0 (API 24) release introduced the Blocked Numbers](https://source.android.com/devices/tech/connect/block-numbers)
content provider that stores a list of phone numbers the user has specified should not be able to
contact them via telephony communications (calls, SMS, MMS).

This library provides the following APIs that allow you to read/write blocked numbers;

- [`BlockedNumbersQuery`](./../blockednumbers/query-blocked-numbers.md)
- [`BlockedNumbersInsert`](./../blockednumbers/insert-blocked-numbers.md)
- [`BlockedNumbersDelete`](./../blockednumbers/delete-blocked-numbers.md)

## Blocked number data

Blocked number data consists of the `number` and `normalizedNumber`.

The `BlockedNumber.number` is the phone number to block as the user entered it. It may or may not be
formatted (e.g. (012) 345-6789).

> ℹ️ Other than regular phone numbers, the blocked number provider can also store addresses (such
> as email) from which a user can receive messages, and calls.

The `BlockedNumber.normalizedNumber` is the `number`'s E164 representation (e.g. +10123456789). This
value can be omitted in which case the provider will try to automatically infer it. (It'll be left
null if the provider fails to infer.) If present, `number` has to be set as well (it will be ignored
otherwise). If you want to set this value yourself, you may want to look
at `android.telephony.PhoneNumberUtils`.

> ℹ️ This may contain an email if `number` is an email.

## Privileges to read/write blocked numbers directly

Reading and writing directly to the Blocked Numbers database table can only be done by certain
privileged apps. The Blocked Number APIs this library provides will only work if all of the 
following requirements are met;

- your app must is a system app and/or the default dialer/phone app and/or the default SMS/messaging
  app
- the current user (if in a multi-user environment) must be allowed to read/write blocked numbers
- the runtime OS version is at least Android 7.0 (N) (API 24)

To check if all of the requirements specified above are met,

```kotlin
val canReadAndWriteBlockedNumbers = Contacts(context).blockedNumbers().privileges.canReadAndWrite()
```

Starting with Android 11 (API 30), you must include the following to your app's manifest in order to
successfully use this function **and therefore the bocked number APIs provided in this library**.

```xml
<queries>
    <intent>
        <action android:name="android.provider.Telephony.SMS_DELIVER" />
    </intent>
</queries>
```

> ℹ️ The above is required to be able to check if your app is the default SMS/messaging app.

## Use the builtin Blocked Numbers activity

If your app does not have the privilege to read/write directly to the blocked number provider, you
may instead launch the builtin system Blocked numbers activity. It provides a fully functional UI
allowing users to see, add, and remove blocked numbers. It is the same activity used by the AOSP 
Contacts app and Google Contacts app when accessing the "Blocked numbers".

```kotlin
Contacts(context).blockedNumbers().startBlockedNumbersActivity(activity)
```

If the `activity` is null, the builtin blocked numbers activity will be launched as a new task,
separate from the current application instance. If it is provided, then the activity will be part of
the current application's stack/history.

Blocked numbers have been introduced in Android 7.0 (N) (API 24). Therefore, this will do nothing
for versions lower than API 24.

## Using the DefaultDialerRequest extensions

The most common way for 3rd party apps (apps that don't come pre-installed by the OEM) to get direct
read/write access to the blocked numbers table is to be set as the default dialer/phone or
SMS/messaging app.

The `contacts.ui.util.DefaultDialerRequest.kt` in the `ui` module` provides extension functions that
allow you to prompt the user to set your app as the default dialer/phone app.

To use it,

```kotlin
Activity {
    fun onRequestToBeTheDefaultDialerAppClicked() {
        requestToBeTheDefaultDialerApp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onRequestToBeDefaultDialerAppResult(requestCode, resultCode) {
            // You are now able to use the BlockedNumbersQuery, BlockedNumbersInsert, and 
            // BlockedNumbersDelete APIs.
        }
    }
}
```

Your app must have an activity with following intent filters in your manifest. Otherwise, this will
do nothing.

```
<intent-filter>
     <action android:name="android.intent.action.VIEW" />
     <action android:name="android.intent.action.DIAL" />

     <category android:name="android.intent.category.DEFAULT" />
     <category android:name="android.intent.category.BROWSABLE" />

     <data android:scheme="tel" />
</intent-filter>
<intent-filter>
     <action android:name="android.intent.action.DIAL" />

     <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

The above intent filters do NOT need to be added to the activity where the extension functions are
invoked. It can be placed in any activity within the application.

To check if your app is the default dialer/phone app,

```kotlin
Context.isDefaultDialerApp()
```

**If your app is not a dialer/phone app**, then you should not set it as the default dialer/phone
app. Otherwise, users of your app may get confused as to why you are prompting them for this
privilege. If you still want to read/write blocked numbers directly, you may still use this method.
However, make it clear to your users as to why you are doing this despite your app not being a
dialer/phone app.

## Update an existing blocked number entry

Update operations are not supported by the Blocked Number provider. Use delete and insert instead.

## Debugging

To look at all of the rows in the Blocked Numbers table, use the `Context.logBlockedNumbersTable`
function in the `debug` module.

For more info, read [Debug the Blocked Number Provider tables](../debug/debug-blockednumber-provider-tables.md).