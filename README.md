# Contacts, Reborn!

Looking for the easiest way to get full-access to the breadth of Contacts in Android without having
to deal with the [Contacts Provider][contacts-provider] and cursors? Well, look no further =)

Whether you just need to get all or some Contacts for a small part of your app (written in Kotlin
or Java), or you are looking to create your own full-fledged Contacts app with the same capabilities
as the native Android Contacts app, this library has you covered!

For more context, read the [introductory, hype blog][medium-blog]!

The core library supports;
- All of the kinds of Data in the Contacts Provider;
  [address, email, event, group membership, IM, name, nickname, note, organization, phone, photo, relation, SIP address, and website](/howto/howto-get-familiar-with-api-entities.md).
- [Custom data](/howto/howto-integrate-custom-data.md).
- [Broad queries](/howto/howto-query-contacts.md) and [advanced queries](/howto/howto-query-contacts-advanced.md)
  of Contacts and RawContacts from zero or more Accounts. Include only desired fields in the results
  (e.g. name and phone number) for optimization. Specify matching criteria in an SQL WHERE clause
  fashion using Kotlin infix functions. Order by contact table columns. Limit and offset functions.
- [Insert](/howto/howto-insert-contacts.md) one or more RawContacts with an associated Account,
  which leads to the insertion of a new Contact subject to automatic aggregation by the Contacts Provider.
- [Update](/howto/howto-update-contacts.md) one or more Contacts, RawContacts, and Data.
- [Delete](/howto/howto-delete-contacts.md) one or more Contacts, RawContacts, and Data.
- [Query](/howto/howto-query-profile.md), [insert](/howto/howto-insert-profile.md),
  [update](/howto/howto-update-profile.md), and [delete](/howto/howto-delete-profile.md)
   Profile (device owner) Contact, RawContact, and Data.
- [Query](/howto/howto-query-groups.md), [insert](/howto/howto-insert-groups.md),
  [update](/howto/howto-update-groups.md), and [delete](/howto/howto-delete-groups.md) Groups per Account.
- [Query](/howto/howto-query-specific-data-kinds.md), [update](/howto/howto-update-data-sets.md ),
  and [delete](/howto/howto-delete-data-sets.md) specific types of Data, including custom Data.
- [Query](/howto/howto-query-accounts.md) Accounts in the system or RawContacts table.
- [Query](/howto/howto-query-blank-raw-contacts.md) for just RawContacts.
- [Associate local RawContacts (no Account) to an Account](/howto/howto-associate-device-local-raw-contacts-to-an-account.md).
- [Join/merge/link and separate/unmerge/unlink two or more Contacts](/howto/howto-link-unlink-contacts.md).
- [Get and set Contact and RawContact Options](/howto/howto-get-set-contact-raw-contact-options.md);
  starred (favorite), custom ringtone, send to voicemail.
- [Get, set, and remove Contacts/RawContact photo and thumbnail](/howto/howto-get-set-remove-contact-raw-contact-photo.md).
- [Get, set, and clear default (primary) Contact Data](/howto/howto-get-set-clear-default-data.md)
  (e.g. default/primary phone number, email, etc).
- [Miscellaneous convenience functions](/howto/howto-use-miscellaneous-extensions.md).
- [Contact data is synced automatically across devices](/howto/howto-sync-contact-data.md)

There are also extensions that add functionality to every core function;
- [Asynchronous work using Kotlin Coroutines](/howto/howto-use-api-with-async-execution.md).
- [Permissions request/handling using Kotlin Coroutines](/howto/howto-use-api-with-permissions-handling.md).

Also included are some pre-baked goodies to be used as is or just for reference;
- [Gender custom Data](/howto/howto-integrate-gender-custom-data.md).
- [Handle name custom Data](/howto/howto-integrate-handlename-custom-data.md).
- [Rudimentary contacts-integrated UI components](/howto/howto-integrate-rudimentary-contacts-integrated-ui-components.md).
- [Debug functions to aid in development](/howto/howto-debug-contacts-provider-tables.md   )

## Installation

First, include JitPack in the repositories list,

```
repositories {
    maven { url "https://jitpack.io" }
}
```

To import all modules,

```
dependencies {
     implementation 'com.github.vestrel00.contacts-android:0.1.2'
}
```

To import specific modules,

```
dependencies {
     implementation 'com.github.vestrel00.contacts-android:core:0.1.2'
}
```

SNAPSHOTs of branches are also available,

```
dependencies {
     implementation 'com.github.vestrel00.contacts-android:master-SNAPSHOT'
}
```

This library is a multi-module project published with [JitPack](https://jitpack.io/p/vestrel00/contacts-android).

## Quick Start

To retrieve all contacts containing all available contact data,

```kotlin
val contacts = Contacts(context).query().find()
```

To simply search for Contacts, yielding the exact same results as the native Contacts app,

```kotlin
val contacts = Contacts(context)
    .broadQuery()
    .whereAnyContactDataPartiallyMatches(searchText)
    .find()
 ```

> Note that for queries, you will need to add the `android.permission.READ_CONTACTS` permission
> to your app's AndroidManifest. Additionally, the user will have to have given your app that
> permission at runtime (starting with Android Marshmallow). Without permissions being granted,
> query functions will return empty results. To make permission handling much easier, Kotlin
> coroutine extensions are available in the `permissions` module.

That's it! BUT, THAT IS BORING! Let's take a look at something more advanced…

To retrieve the first 5 contacts (including only the contact id, display name, and phone numbers in
the results) ordered by display name in descending order, matching ALL of these rules;
- a first name starting with "leo" 
- has emails from gmail or hotmail
- lives in the US
- has been born prior to making this query
- is favorited
- has a nickname of "DarEdEvil" (case sensitive)
- works for Facebook
- has a note
- belongs to the account of "jerry@gmail.com" or "jerry@myspace.com"

```kotlin
val contacts = Contacts(context)
    .query()
    .where(
        (Fields.Name.GivenName startsWith "leo") and
                ((Fields.Email.Address endsWith "gmail.com") or (Fields.Email.Address endsWith "hotmail.com")) and
                (Fields.Address.Country equalToIgnoreCase "us") and
                ((Fields.Event.Date lessThan Date().toWhereString()) and (Fields.Event.Type equalTo Event.Type.BIRTHDAY)) and
                (Fields.Contact.Options.Starred equalTo true) and
                (Fields.Nickname.Name equalTo "DarEdEvil") and
                (Fields.Organization.Company `in` listOf("facebook", "FB")) and
                (Fields.Note.Note.isNotNullOrEmpty())
    )
    .accounts(
        Account("jerry@gmail.com", "com.google"),
        Account("jerry@myspace.com", "com.myspace"),
    )
    .include(Fields.Contact.Id, Fields.Contact.DisplayNamePrimary, Fields.Phone.Number, Fields.Phone.NormalizedNumber)
    .orderBy(ContactsFields.DisplayNamePrimary.desc())
    .offset(0)
    .limit(5)
    .find()
```

> For more info, read [How do I get a list of contacts in the simplest way?](/howto/howto-query-contacts.md)
> and [How do I get a list of contacts in a more advanced way?](/howto/howto-query-contacts-advanced.md)

Imagine what this would look like if you use ContactsContract directly. Now, you don't have to! The
above snippet is in Kotlin but, like I mentioned, all of the core APIs are usable in Java too
(though it won't look as pretty).

Once you have the contacts, you now have access to all of their data!

 ```kotlin
Log.d(
    "Contacts",
    contacts.joinToString("\n\n") { contact ->
        """
            Display name: ${contact.displayNamePrimary}
            Last updated: ${contact.lastUpdatedTimestamp}
            Starred?: ${contact.options?.starred}
            Send to voicemail?: ${contact.options?.sendToVoicemail}
            Ringtone: ${contact.options?.customRingtone}

            Aggregate data from all RawContacts
            -----------------------------------
            Addresses: ${contact.addressList()}
            Emails: ${contact.emailList()}
            Events: ${contact.eventList()}
            IMs: ${contact.imList()}
            Names: ${contact.nameList()}
            Nicknames: ${contact.nicknameList()}
            Notes: ${contact.noteList()}
            Organizations: ${contact.organizationList()}
            Phones: ${contact.phoneList()}
            Relations: ${contact.relationList()}
            SipAddresses: ${contact.sipAddressList()}
            Websites: ${contact.websiteList()}
            -----------------------------------
        """.trimIndent()
    }
)
// Groups and photos require separate blocking function calls.
// There are also aggregate data functions that return a sequence instead of a list.
 ```

> Each Contact may have more than one of the following data if the Contact is made up of 2 or more
> RawContacts; name, nickname, note, organization, sip address.

> For more info, read [How do I get familiarized with the Contacts entities?](/howto/howto-get-familiar-with-api-entities.md)

#### There's a lot more

This library is capable of doing more than just querying contacts. Let's take a look at a few of them here.

To get the first 20 gmail emails ordered by email address in descending order,

```kotlin
val emails = Contacts(context).data()
    .query()
    .emails()
    .where(Fields.Email.Address endsWith  "gmail.com")
    .orderBy(Fields.Email.Address.desc(ignoreCase = true))
    .offset(0)
    .limit(20)
    .find()
```

It's not just for emails. It's for all common data kinds (including custom data).

> For more info, read [How do I get a list of specific data kinds?](/howto/howto-query-specific-data-kinds.md)

To **CREATE/INSERT** a contact with a name of "John Doe" who works at Amazon with a work email of
"john.doe@amazon.com" (in Kotlin),

```kotlin
val insertResult = Contacts(context)
    .insert()
    .rawContacts(MutableRawContact().apply {
        name = MutableName().apply {
            givenName = "John"
            familyName = "Doe"
        }
        organization = MutableOrganization().apply {
            company = "Amazon"
            title = "Superstar"
        }
        emails.add(MutableEmail().apply {
            address = "john.doe@amazon.com"
            type = Email.Type.WORK
        })
    })
    .commit()
```

Or alternatively, in a more Kotlinized style,

```
val insertResult = Contacts(context)
    .insert()
    .rawContact {
        setName {
            givenName = "John"
            familyName = "Doe"
        }
        setOrganization {
            company = "Amazon"
            title = "Superstar"
        }
        addEmail {
            address = "john.doe@amazon.com"
            type = Email.Type.WORK
        }
    }
    .commit()
```

> For more info, read [How do I create/insert contacts?](/howto/howto-insert-contacts.md)

If John Doe switches jobs and heads over to Microsoft, we can **UPDATE** his data,

```kotlin
Contacts(context)
    .update()
    .contacts(johnDoe.toMutableContact().apply {
        setOrganization {
            company = "Microsoft"
            title = "Newb"
        }
        emails().first().apply {
            address = "john.doe@microsoft.com"
        }
    })
    .commit()
```

> For more info, read [How do I update contacts?](/howto/howto-update-contacts.md)

If we no longer like John Doe, we can **DELETE** him from our life,

```kotlin
Contacts(context)
    .delete()
    .contacts(johnDoe)
    .commit()
```

> For more info, read [How do I delete contacts?](/howto/howto-delete-contacts.md)

> Note that for insert, update, and delete functions, you will need to add the
> `android.permission.WRITE_CONTACTS` and `android.permission.GET_ACCOUNTS` permissions
> to your app's AndroidManifest. Additionally, the user will have to have given your app that
> permission at runtime (starting with Android Marshmallow). Without permissions being granted,
> these functions will do nothing and return a failed result. To make permission handling much
> easier, Kotlin coroutine extensions are available in the `permissions` module.

#### There's even more…

This library provides Kotlin coroutine extensions in the `permissions` module for all API functions
to handle permissions and `async` module for executing work in background threads.

```kotlin
launch {
    val contacts = Contacts(context)
        .queryWithPermission()
        ...
        .findWithContext()
}

val deferredResult = Contacts(context)
        .insert()
        ...
        .commitAsync()
launch {
    val result = deferredResult.await()
}
```

> For more info, read [How do I use the permissions extensions to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)
> and [How do I use the async extensions to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

So, if we call the above function and we don't yet have permission. The user will be prompted to
give the appropriate permissions before the query proceeds. Then, the work is done in the coroutine
context of choice (default is Dispatchers.IO). If the user does not give permission, the query will
return no results.

Extensions for Kotlin Flow and RxJava is also in the v1 roadmap, which includes APIs for listening 
to Contacts database changes.

#### There's too much…

**The above examples barely scratches the surface of what this library provides.** For more in-depth
Howtos, visit the [howto directory](/howto/). For a sample app reference, take a look at and run the
`sample` module.

#### Setup

There is no setup required. It's up to you how you want to create and retain instances of the
`contacts.core.Contacts(context)` API. For more info, read [howto setup](/howto/howto-setup-contacts-api.md).

It is also useful to read [How do I get familiarized with the Contacts entities?](/howto/howto-get-familiar-with-api-entities.md).

## Requirements

- Min SDK 19+
- Java 7+

## Proguard

If you use Proguard and the `async` and/or `permissions`, you may need to add rules for
[Coroutines][coroutines-proguard].

## License

    Copyright 2021 Contacts Contributors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Support

This is a newly open sourced library with only one contributor so far (me). Don’t expect much
support in the beginning. I am only able to work on this (and respond to issues) outside of work
hours. This means late nights, weekends, and maybe holidays.

As time passes, hopefully this library gets more and more contributors. At some point, I hope to
gain contributors that have become experts on this library to help me support the community by
maintaining it and making admin-level decisions.

In any case, create issues for any bugs found and I'll get to it when I get the chance depending on
severity of the issue.

## v1.0.0 Release Roadmap

These are the remaining work that is required for this library to be considered worthy of being
v1.0.0 and safe for production.

To be clear, all core functions have been implemented and manually tested by me. As far as I'm
concerned, this library is production-ready as is, especially for daring/experimental consumers ;P
However, I cannot _professionally_ proclaim this to be production-ready without doing things that I
consider are essential for any API to do. So, here is my v1.0.0 release checklist!

- **(v0.1.0) Ready for public alpha development (current version)**
  - All v1 functions have been implemented and _manually tested_, with full documentation.
- **(v0.2.0) Sample app complete**
  - Purpose:
    - Prove that library produces same results as native Contacts app in actual usage
    - Prove that 0, 1, or more accounts are handled properly
    - Ensure all v1 functions are used in the sample app
    - All native Contacts app features are implemented in the sample app
      - Not including features that require post v1 functions
    - Find bugs in API
  - Use Pixel 3a API 30 native Contacts app as reference
  - Remaining work to be done for this version;
    - Create/edit/view contact screen
      - Select group memberships
      - Choose account to associate with the new (raw) contact (create-only)
      - Long press actions
        - Primary action (e.g. call phone number, send email, etc)
        - Copy to clipboard
        - Set default
    - Share contacts (export) (placeholder only - show toast saying it's coming post v1 release)
    - Link contacts
    - Create contact shortcut
    - Delete multiple contacts
    - Show all contacts (no filter)
    - Filter contacts by label (group memberships)
    - Create, update, delete non-system labels (group memberships)
    - Filter contacts by account
    - Settings
      - Profile create, read, update, delete
      - Show Accounts
      - Default account for new contacts
      - Contacts to display
      - Sort by first or last name
      - Name format
      - Phonetic name
      - Import (placeholder only - show toast saying it's coming post v1 release)
      - Export (placeholder only - show toast saying it's coming post v1 release)
      - Blocked numbers (placeholder only - show toast saying it's coming post v1 release)
      - About Contacts
- **(v0.3.0) Sample app enhanced**
  - Purpose:
    - Prove that additional (advanced) functions not in native Contacts app works
    - Find bugs in API
  - Remaining work to be done for this version;
    - Integrate Query (for more advance querying compared to BroadQuery).
    - Integrate DataQuery, DataUpdate, and DataDelete functions
    - Integrate handlename custom data
    - Integrate gender custom data
- **(v0.4.0) Sample app reuse**
  - Purpose:
    - Move/refactor as much code as possible from the sample app into reusable modules
    - Promote code reuse, including rudimentary code (that couldbe refined)
  - Remaining work to be done for this version;
    - Move view page to new module; ui-async
- **(0.5.0) Code quality and integrity**
  - Purpose:
    - Prevent new code from breaking existing API
    - Ensure consumers have an easy way to mock the API in both white box unit tests and black box ui tests
    - Ensure that code follows language and community standards
    - Ensure that code is covered with automated tests
  - Remaining work to be done for this version;
    - Create test version of the Contacts API for consumer use in both white box unit tests and black box ui tests
      - Ensure that test versions do not actually read from or write to the underlying database.
    - androidTest/ for all core functions
    - Static analysis; lint / code quality checks (checkstyle, lint, etc)
    - Code coverage reporting tool
    - SonarQube?
- **(0.6.0) Kotlin Flow extensions**
  - Purpose:
    - Provide alternative asynchronous extensions for consumers prefer to use Flow over withContext
      and async await functions
    - Provide a way to proactively listen for changes in the Contacts database
  - Remaining work to be done for this version;
    - Add Flow function extensions in existing async module
    - Listen for changes in the Contacts database
- **(0.7.0) Reactive extensions**
  - Purpose:
    - Provide alternative asynchronous extensions for consumers that are not using Kotlin coroutines
    - Provide a way to proactively listen for changes in the Contacts database
  - Remaining work to be done for this version;
    - Create rx module
    - Implement rx-equivalent of async module functions
    - Listen for changes in the Contacts database
- **(0.8.0) Update dependencies and tools**
  - Purpose:
    - Ensure that the library and all of its dependencies and tools are up-to-date
  - Remaining work to be done for this version;
    - Update Kotlin stdlib and coroutines
      - Use sealed interface in Fields when upgrading to Kotlin 1.5+
    - Remove/replace the no-longer maintained Dexter
    - Update Gradle
    - Update Android Studio
- **(0.9.0) v1 release prep**
  - Purpose:
    - Ensure that the library is ready for v1.0.0 production release
  - Remaining work to be done for this version;
    - Setup continuous integration (CI/CD) pipeline (if not yet done)
    - Review remaining TODOs and FIXMEs, if any
    - Ensure all functions are covered with automated tests
    - Ensure all functions are documented with samples / howtos

## Post v1.0.0 Release Roadmap

These functions/features will not make the v1 release because I want to get the production-ready v1
version library out as soon as possible so that the community may benefit from it =)

Most, if not all, of these functions are the missing components required to rebuild the native
Android Contacts app from the ground up. In other words, each of these functions allow consumers to
implement a specific part of the native Android Contacts app.

1. Blocked phone numbers.
    - The Android 7.0 release introduces a BlockedNumberProvider content provider that stores a list
      of phone numbers the user has specified should not be able to contact them via telephony
      communications (calls, SMS, MMS).
    - See https://source.android.com/devices/tech/connect/block-numbers
2. SIM card query, insert(?), update(?), and delete(?).
    - Enables importing from and exporting(?) to SIM card.
    - Query will definitely be possible. I'm not sure if insert, update, and delete operations
      are possible. We will see.
3. Contacts read/write .VCF file.
    - Enables import from and export to .VCF file.
    - Enables sharing a contact.
    - Dev note: search ContactsContract for "vcard".
4. Social media custom data.
    - WhatsApp (vnd.com.whatsapp.profile)
    - Facebook Messenger
    - Twitter
    - Etc
    - I will need help from the community to implement these because I don't participate in
      social media. Use the `debug` module to look into the data table and look for social media
      mimetypes. Then, create a new custom data module using the existing `customdata-x` modules as
      reference. A separate module should be created for different social media.
      E.G. customdata-whatsapp, customdata-messenger, customdata-twitter
        - Read more in the DEV_NOTES "Custom Data / MimeTypes" section.

[medium-blog]: https://proandroiddev.com/android-contacts-reborn-19985c73ad43
[contacts-provider]: https://developer.android.com/guide/topics/providers/contacts-provider
[coroutines-proguard]: https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/resources/META-INF/proguard/coroutines.pro
