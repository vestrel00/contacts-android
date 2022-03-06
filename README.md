# Android Contacts, Reborn

> Written with ♥️ and 🔥 since December 2018. Open sourced since October 4, 2021. 

![Android Contacts, Reborn banner](/docs/assets/images/banner.gif)

[![JitPack](https://jitpack.io/v/vestrel00/contacts-android.svg)](https://jitpack.io/#vestrel00/contacts-android)
[![Monthly Downloads](https://jitpack.io/v/vestrel00/contacts-android/month.svg)](https://jitpack.io/#vestrel00/contacts-android)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20Contacts,%20Reborn-green.svg?style=flat&logo=android)](https://android-arsenal.com/details/1/8393)
[![Android Weekly](https://img.shields.io/badge/AndroidWeekly-Issue%20%23491-2299cc.svg?style=flat&logo=android)](https://androidweekly.net/issues/issue-491)
[![Kotlin Weekly](https://img.shields.io/badge/KotlinWeekly-Issue%20%23275-7549b5.svg?style=flat&logo=kotlin)](https://us12.campaign-archive.com/?u=f39692e245b94f7fb693b6d82&id=c864ecd437)
[![Java Trends](https://img.shields.io/badge/JavaTrends-Newsletter%20%2358-f8981c.svg?style=flat&logo=java)](https://dormoshe.io/newsletters/ag/java/58)
[![Android Trends](https://img.shields.io/badge/AndroidTrends-Newsletter%20%2381-3ddc84.svg?style=flat&logo=android)](https://dormoshe.io/newsletters/ag/android/81)
[![Kotlin Trends](https://img.shields.io/badge/KotlinTrends-Newsletter%20%2364-7f51ff.svg?style=flat&logo=kotlin)](https://dormoshe.io/newsletters/ag/kotlin/64)
[![droidcon](https://img.shields.io/badge/droidcon-Android%20Contacts,%20Reborn-blue.svg?style=flat&logo=android)](https://www.droidcon.com/2021/11/23/android-contacts-reborn/)
[![ProAndroidDev](https://img.shields.io/badge/ProAndroidDev-Contacts,%20Reborn!-f53e28.svg?style=flat&logo=medium)](https://proandroiddev.com/android-contacts-reborn-19985c73ad43)
[![Dev.to](https://img.shields.io/badge/Dev.to%20-3%20years%20to%20open%20source-0a0a0a.svg?style=flat&logo=devdotto)](https://dev.to/vestrel00/i-spent-3-years-writing-an-android-contacts-api-in-kotlin-with-java-interop-what-ive-learned-54hp)
[![Reddit](https://img.shields.io/badge/Reddit%20-Share%20with%20the%20community-ff4500.svg?style=flat&logo=reddit)](https://www.reddit.com/r/androiddev/comments/rz370s/i_dedicated_3_years_to_building_this_android/)
[![YouTube](https://img.shields.io/badge/YouTube%20-Contacts%20Reborn%20+%20SAO-ff0000.svg?style=flat&logo=youtube)](https://youtu.be/NlRve_B1RA0)

This library gives you a complete set of APIs to do everything you need with Contacts in Android. 
You no longer have to deal with the [Contacts Provider][contacts-provider], database operations, 
and cursors. 

Whether you just need to get all or some Contacts for a small part of your app (written in Kotlin
or Java), or you are looking to create your own full-fledged Contacts app with the same capabilities
as the native (AOSP) Android Contacts app and Google Contacts app, this library is for you!

## Please help support this project!

If you think that this library is helpful, please click the ⭐️. If you want to keep up-to-date, 
please click the 👁. If it has really helped you out and would recommend it, please share it with 
other folks and developer communities 🙏

Please visit this [discussion thread for more info and join the conversation](https://github.com/vestrel00/contacts-android/discussions/189).

## About this repository

📜 Documentation and how-to guides are all available and linked in the repository. You can browse the
[docs](/docs/) or visit the [GitHub Pages][github-pages]. Both contain the same info but
the GitHub pages are updated only for each release.

🚂 [Releases][releases] have a corresponding "Release Checklist", which are in the 
[discussions section][discussions]. The checklist contains the issues that will be included in the 
release and other relevant information. The current release that is being worked on is pinned at 
the top. Release Checklists are also used as discussion pages after releases have been published.

🗺 The GitHub wiki hosts the [project roadmap][project-roadmap]. It contains all planned work, which 
are organized using issues, milestones, and projects.

## Features

> All features below are links to documentation. Click on each of them to learn more!

The `core` module provides,

- ✅ [**All data kinds**](/docs/entities/about-api-entities.md) in the Contacts Provider;
  _address, email, event, group membership, IM, name, nickname, note, organization, phone, photo, relation, SIP address, and website_.
- ✅ [**Custom data** integration](/docs/customdata/integrate-custom-data.md).
- ✅ [**Broad queries**](/docs/basics/query-contacts.md) and [**advanced queries**](/docs/basics/query-contacts-advanced.md) of Contacts and RawContacts from zero or more Accounts and/or Groups. 
- ✅ [Contact **lookup keys**](/docs/entities/about-contact-lookup-key.md)
- ✅ [Include only desired fields](/docs/entities/include-only-desired-data.md) in read/write operations to **optimize CPU and memory**. 
- ✅ Powerful, **type-safe query DSL**.
- ✅ **Pagination** using order by, limit, and offset database functions.
- ✅ [**Insert**](/docs/basics/insert-contacts.md) one or more RawContacts with an associated Account,
  causing automatic insertion of a new Contact subject to automatic aggregation by the Contacts Provider.
- ✅ [**Update**](/docs/basics/update-contacts.md) one or more Contacts, RawContacts, and Data.
- ✅ [**Delete**](/docs/basics/delete-contacts.md) one or more Contacts, RawContacts, and Data.
- ✅ [Query](/docs/profile/query-profile.md), [insert](/docs/profile/insert-profile.md),
  [update](/docs/profile/update-profile.md), and [delete](/docs/profile/delete-profile.md)
   **Profile (device owner)** Contact, RawContact, and Data.
- ✅ [Query](/docs/groups/query-groups.md), [insert](/docs/groups/insert-groups.md),
  [update](/docs/groups/update-groups.md), and [delete](/docs/groups/delete-groups.md) **Groups**.
- ✅ [Query](/docs/data/query-data-sets.md), [insert](/docs/data/insert-data-sets.md)
  [update](/docs/data/update-data-sets.md ), and [delete](/docs/data/delete-data-sets.md) **specific kinds of data**.
- ✅ [Query](/docs/customdata/query-custom-data.md), [insert](/docs/customdata/insert-custom-data.md), 
  [update](/docs/customdata/update-custom-data.md), and [delete](/docs/customdata/delete-custom-data.md) **custom data**.
- ✅ [Query](/docs/blockednumbers/query-blocked-numbers.md), [insert](/docs/blockednumbers/insert-blocked-numbers.md),
  and [delete](/docs/blockednumbers/delete-blocked-numbers.md) **blocked numbers**.
- ✅ [Query](/docs/accounts/query-accounts.md) for Accounts in the system or RawContacts table.
- ✅ [Query](/docs/accounts/query-raw-contacts.md) for just RawContacts.
- ✅ [Associate **local RawContacts** (no Account) to an Account](/docs/accounts/associate-device-local-raw-contacts-to-an-account.md).
- ✅ [**Link/unlink**](/docs/other/link-unlink-contacts.md) two or more Contacts.
- ✅ [Get/set contact options](/docs/other/get-set-clear-contact-raw-contact-options.md);
  **_starred (favorite), custom ringtone, send to voicemail_**.
- ✅ [Get/set Contacts/RawContact **photo and thumbnail**](/docs/other/get-set-remove-contact-raw-contact-photo.md).
- ✅ [Get/set **default (primary) Contact Data**](/docs/other/get-set-clear-default-data.md)
  (e.g. default/primary phone number, email, etc).
- ✅ [Convenience functions](/docs/other/convenience-functions.md).
- ✅ [Contact data is synced automatically across devices](/docs/entities/sync-contact-data.md).
- ✅ [Support for **logging API input and output**](/docs/logging/log-api-input-output.md)
- ✅ [**Redactable entities** and API input and output](/docs/entities/redact-apis-and-entities.md)
  for production-safe logging that upholds user data privacy laws to meet GDPR guidelines 
  _(this is not legal advice)_.
- ✅ [Full **in-depth documentation/guides**][github-pages].
- ✅ Full **Java interoptibilty**.
- ✅ **Zero dependency**.
- ✅ Clean separation between **Contacts vs RawContacts**.
- ✅ Clear distinction between truly deeply immutable, mutable, new, and existing entities allowing for **thread safety and JetPack compose optimizations**.

There are also extensions that add functionality to every core function,

- 🧰 [**Asynchronous** work using **Kotlin Coroutines**](/docs/async/async-execution-coroutines.md).
- 🧰 [**Permissions** request/handling using **Kotlin Coroutines**](/docs/permissions/permissions-handling-coroutines.md).
- 🔜 [**Kotlin Flow** extensions](https://github.com/vestrel00/contacts-android/milestone/8)
- 🔜 [**RxJava** extensions](https://github.com/vestrel00/contacts-android/milestone/9)

Also included are some pre-baked goodies to be used as is or just for reference,

- 🍬 [Gender custom data](/docs/customdata/integrate-gender-custom-data.md).
- 🍬 [Google Contacts custom data](/docs/customdata/integrate-googlecontacts-custom-data.md).
- 🍬 [Handle name custom data](/docs/customdata/integrate-handlename-custom-data.md).
- 🍬 [Pokemon custom data](/docs/customdata/integrate-pokemon-custom-data.md)
- 🍬 [Role Playing Game (RPG) custom data](/docs/customdata/integrate-rpg-custom-data.md).
- 🍬 [Rudimentary contacts-integrated UI components](/docs/ui/integrate-rudimentary-contacts-integrated-ui-components.md).
- 🍬 [Debug functions to aid in development](/docs/debug/debug-contacts-provider-tables.md)

There are also more features that are on the way!

1. ☢️ [SIM card query, insert, update, and delete](https://github.com/vestrel00/contacts-android/issues/26).
2. ☢️ [Work profile contacts](https://github.com/vestrel00/contacts-android/issues/186)
3. ☢️ [Dynamically integrate custom data from other apps](https://github.com/vestrel00/contacts-android/issues/112)
4. ☢️ [Read/write from/to .VCF file](https://github.com/vestrel00/contacts-android/issues/26).

## Installation

First, include JitPack in the repositories list,

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

To import all modules,

```groovy
dependencies {
     implementation 'com.github.vestrel00:contacts-android:0.1.10'
}
```

To import specific modules,

```groovy
dependencies {
     implementation 'com.github.vestrel00.contacts-android:core:0.1.10'
}
```

> Notice that when importing specific modules/subprojects, the first ":" comes after "contacts-android".

SNAPSHOTs of branches are also available,

```groovy
dependencies {
     implementation 'com.github.vestrel00:contacts-android:main-SNAPSHOT'
}
```

> This library is a multi-module project published with JitPack
> [![JitPack](https://jitpack.io/v/vestrel00/contacts-android.svg)](https://jitpack.io/#vestrel00/contacts-android)

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

> For more info, read [Query contacts](/docs/basics/query-contacts.md).

Something a bit more advanced...

To retrieve the first 5 contacts (including only the contact id, display name, and phone numbers in
the results) ordered by display name in descending order, matching ALL of these rules;
- a first name starting with "leo"
- has emails from gmail or hotmail
- lives in the US
- has been born prior to making this query
- is favorited (starred)
- has a nickname of "DarEdEvil" (case sensitive)
- works for Facebook
- has a note
- belongs to the account of "john.doe@gmail.com" or "john.doe@myspace.com"

```kotlin
val contacts = Contacts(context)
    .query()
    .where {
        (Name.GivenName startsWith "leo") and
        (Email.Address { endsWith("gmail.com") or endsWith("hotmail.com") }) and
        (Address.Country equalToIgnoreCase "us") and
        (Event { (Date lessThan Date().toWhereString()) and (Type equalTo EventEntity.Type.BIRTHDAY) }) and
        (Contact.Options.Starred equalTo true) and
        (Nickname.Name equalTo "DarEdEvil") and
        (Organization.Company `in` listOf("facebook", "FB")) and
        (Note.Note.isNotNullOrEmpty())
    }
    .accounts(
        Account("john.doe@gmail.com", "com.google"),
        Account("john.doe@myspace.com", "com.myspace"),
    )
    .include { setOf(
        Contact.Id,
        Contact.DisplayNamePrimary,
        Phone.Number
    ) }
    .orderBy(ContactsFields.DisplayNamePrimary.desc())
    .offset(0)
    .limit(5)
    .find()
```

> Fore more info, read [Query contacts (advanced)](/docs/basics/query-contacts-advanced.md).

Once you have the contacts, you now have access to all of their data!

```kotlin
val contact: Contact
Log.d(
    "Contact",
    """
        ID: ${contact.id}
        Lookup Key: ${contact.lookupKey}

        Display name: ${contact.displayNamePrimary}
        Display name alt: ${contact.displayNameAlt}

        Photo Uri: ${contact.photoUri}
        Thumbnail Uri: ${contact.photoThumbnailUri}

        Last updated: ${contact.lastUpdatedTimestamp}

        Starred?: ${contact.options?.starred}
        Send to voicemail?: ${contact.options?.sendToVoicemail}
        Ringtone: ${contact.options?.customRingtone}

        Aggregate data from all RawContacts of the contact
        -----------------------------------
        Addresses: ${contact.addressList()}
        Emails: ${contact.emailList()}
        Events: ${contact.eventList()}
        Group memberships: ${contact.groupMembershipList()}
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
    // There are also aggregate data functions that return a sequence instead of a list.
)
```

> For more info, read about [API Entities](/docs/entities/about-api-entities.md).

## Setup

There is no setup required. It's up to you how you want to create and retain instances of the
`contacts.core.Contacts(context)` API. For more info, read [Contacts API Setup](/docs/setup/setup-contacts-api.md).

It is also useful to read about [API Entities](/docs/entities/about-api-entities.md).

## More than enough APIs that will allow you to build your own contacts app!

This library is capable of doing more than just querying contacts. Actually, you can build your own
full-fledged contacts app with it!

Let's take a look at a few other APIs this library provides...

To get the first 20 gmail emails ordered by email address in descending order,

```kotlin
val emails = Contacts(context)
    .data()
    .query()
    .emails()
    .where { Email.Address endsWith "gmail.com" }
    .orderBy(Fields.Email.Address.desc(ignoreCase = true))
    .offset(0)
    .limit(20)
    .find()
```

It's not just for emails. It's for all data kinds (including custom data).

> For more info, read [Query specific data kinds](/docs/data/query-data-sets.md).

To **CREATE/INSERT** a contact with a name of "John Doe" who works at Amazon with a work email of
"john.doe@amazon.com" (in Kotlin),

```kotlin
val insertResult = Contacts(context)
    .insert()
    .rawContacts(NewRawContact().apply {
        name = NewName().apply {
            givenName = "John"
            familyName = "Doe"
        }
        organization = NewOrganization().apply {
            company = "Amazon"
            title = "Superstar"
        }
        emails.add(NewEmail().apply {
            address = "john.doe@amazon.com"
            type = EmailEntity.Type.WORK
        })
    })
    .commit()
```

Or alternatively, in a more Kotlinized style using named arguments,

```kotlin
val insertResult = Contacts(context)
    .insert()
    .rawContacts(NewRawContact(
        name = NewName(
            givenName = "John",
            familyName = "Doe"
        ),
        organization = NewOrganization(
            company = "Amazon",
            title = "Superstar"
        ),
        emails = mutableListOf(NewEmail(
            address = "john.doe@amazon.com",
            type = EmailEntity.Type.WORK
        ))
    ))
    .commit()
```

Or alternatively, using extension functions,

```kotlin
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
            type = EmailEntity.Type.WORK
        }
    }
    .commit()
```

> For more info, read [Insert contacts](/docs/basics/insert-contacts.md).

If John Doe switches jobs and heads over to Microsoft, we can **UPDATE** his data,

```kotlin
Contacts(context)
    .update()
    .contacts(johnDoe.mutableCopy {
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

> For more info, read [Update contacts](/docs/basics/update-contacts.md).

If we no longer like John Doe, we can **DELETE** him from our life,

```kotlin
Contacts(context)
    .delete()
    .contacts(johnDoe)
    .commit()
```

> For more info, read [Delete Contacts](/docs/basics/delete-contacts.md).

## Threading and permissions

This library provides Kotlin coroutine extensions in the `permissions` module for all API functions
to handle permissions and `async` module for executing work in background threads.

```kotlin
launch {
    val contacts = Contacts(context)
        .queryWithPermission()
        ...
        .findWithContext()

    val deferredResult = Contacts(context)
        .insertWithPermission()
        ...
        .commitAsync()
    val result = deferredResult.await()
}
```

> For more info, read [Permissions handling using coroutines](/docs/permissions/permissions-handling-coroutines.md)
> and [Execute work outside of the UI thread using coroutines](/docs/async/async-execution-coroutines.md).

So, if we call the above function and we don't yet have permission. The user will be prompted to
give the appropriate permissions before the query proceeds. Then, the work is done in the coroutine
context of choice (default is Dispatchers.IO). If the user does not give permission, the query will
return no results.

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap, which includes APIs for listening
> to Contacts database changes.

## Full documentation, guides, and samples

**The above examples barely scratches the surface of what this library provides.** For more in-depth
documentation, visit the [docs](/docs/) or visit the [GitHub Pages][github-pages]. For a sample app 
reference, take a look at and run the `sample` module.

## All APIs in the library are optimized!

Some other APIs or util functions out there typically perform one internal database query per 
contact returned. They do this to fetch the data per contact. This means that if there are 
1,000 matching contacts, then an extra 1,000 internal database queries are performed! 
This is not cool!

To address this issue, the query APIs provided in the Contacts, Reborn library, perform only at 
least two and at most six or seven internal database queries no matter how many contacts are 
matched! Even if there are 100,000 contacts matched, the library will only perform two to seven 
internal database queries (depending on your query parameters).

Of course, if you don't want to fetch all hundreds of thousands of contacts, the query APIs support 
**pagination** with `limit` and `offset` functions :sunglasses:

**Cancellations** are also supported! To cancel a query amid execution,

```kotlin
.find { returnTrueIfQueryShouldBeCancelled() }
```

The find function optionally takes in a function that, if it returns true, will cancel query 
processing as soon as possible. The function is called numerous times during query processing to 
check if processing should stop or continue. This gives you the option to cancel the query.

This is useful when used in multi-threaded environments. One scenario where this would be frequently 
used is when performing queries as the user types a search text. You are able to cancel the current
query when the user enters new text.

For example, to automatically cancel the query inside a Kotlin coroutine when the coroutine is 
cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val contacts = query.find { !isActive }
    }
    // Or, using the coroutine extensions in the async module...
    val contacts = query.findWithContext()
}
```

## All core APIs are framework-agnostic and works well with Java and Kotlin

**The API does not and will not force you to use any frameworks (e.g. RxJava or Coroutines/Flow)!**
All core functions of the API live in the `core` module, which you can import to your project all by
itself. Don't believe me? Take a look at the dependencies in the `core/build.gradle` :D 

So, feel free to use the core API however you want with whatever libraries or frameworks you want, 
such as Reactive, Coroutines/Flow, AsyncTask (hope not), WorkManager, and whatever permissions 
handling APIs you want to use.

All other modules in this library are **optional** and are just there for your convenience or for
reference.

I also made sure that **all core functions and entities are interoperable with Java.** So, if 
you were wondering why I’m using a semi-builder pattern instead of using named arguments with 
default values, that is why. I’ve also made some other intentional decisions about API design to 
ensure the best possible experience for both Kotlin and Java consumers without sacrificing Kotlin 
language standards. It is Kotlin-first, Java-second (with love and care).

> Modules other than the core module are not guaranteed to be compatible with Java.

## Requirements

- Min SDK 19+

## Proguard

If you use Proguard and the `async` and/or `permissions`, you may need to add rules for
[Coroutines][coroutines-proguard].

## License

    Copyright 2022 Contacts Contributors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[project-roadmap]: https://github.com/vestrel00/contacts-android/wiki/Project-Roadmap
[discussions]: https://github.com/vestrel00/contacts-android/discussions
[releases]: https://github.com/vestrel00/contacts-android/releases
[github-pages]: https://vestrel00.github.io/contacts-android/
[medium-blog]: https://proandroiddev.com/android-contacts-reborn-19985c73ad43
[devto-blog]: https://dev.to/vestrel00/i-spent-3-years-writing-an-android-contacts-api-in-kotlin-with-java-interop-what-ive-learned-54hp
[reddit-blog]: https://www.reddit.com/r/androiddev/comments/rz370s/i_dedicated_3_years_to_building_this_android/
[youtube-sao]: https://youtu.be/NlRve_B1RA0
[contacts-provider]: https://developer.android.com/guide/topics/providers/contacts-provider
[coroutines-proguard]: https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/resources/META-INF/proguard/coroutines.pro
