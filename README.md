# Contacts, Reborn!

![Android Contacts, Reborn banner](/banner.png)

[![JitPack](https://jitpack.io/v/vestrel00/contacts-android.svg)](https://jitpack.io/#vestrel00/contacts-android)

[![Android Weekly](https://img.shields.io/badge/AndroidWeekly-Issue%20%23491-2299cc.svg?style=flat&logo=android)](https://androidweekly.net/issues/issue-491)
[![Kotlin Weekly](https://img.shields.io/badge/KotlinWeekly-Issue%20%23275-7549b5.svg?style=flat&logo=kotlin)](https://us12.campaign-archive.com/?u=f39692e245b94f7fb693b6d82&id=c864ecd437)
[![Java Trends](https://img.shields.io/badge/JavaTrends-Newsletter%20%2358-f8981c.svg?style=flat&logo=java)](https://dormoshe.io/newsletters/ag/java/58)
[![Android Trends](https://img.shields.io/badge/AndroidTrends-Newsletter%20%2381-3ddc84.svg?style=flat&logo=android)](https://dormoshe.io/newsletters/ag/android/81)
[![Kotlin Trends](https://img.shields.io/badge/KotlinTrends-Newsletter%20%2364-7f51ff.svg?style=flat&logo=kotlin)](https://dormoshe.io/newsletters/ag/kotlin/64)
[![ProAndroidDev](https://img.shields.io/badge/ProAndroidDev-Contacts,%20Reborn!-f53e28.svg?style=flat&logo=medium)](https://proandroiddev.com/android-contacts-reborn-19985c73ad43)
[![Dev.to](https://img.shields.io/badge/Dev.to%20-3%20years%20to%20open%20source-0a0a0a.svg?style=flat&logo=devdotto)](https://dev.to/vestrel00/i-spent-3-years-writing-an-android-contacts-api-in-kotlin-with-java-interop-what-ive-learned-54hp)

Looking for the easiest way to get full-access to the breadth of Contacts in Android without having
to deal with the [Contacts Provider][contacts-provider] and cursors? Well, look no further =)

Whether you just need to get all or some Contacts for a small part of your app (written in Kotlin
or Java), or you are looking to create your own full-fledged Contacts app with the same capabilities
as the native Android Contacts app, this library has you covered!

Documentation and how-to guides are all available and linked in the repository. You can browse the
[Howto pages](/howto/) or visit the [GitHub Pages][github-pages]. Both contain the same info but
the GitHub pages are not guaranteed to be up-to-date. The GitHub wiki hosts the 
[project roadmap][project-roadmap]. It contains all planned work and release schedules, which are 
organized using issues, milestones, and projects.

You can also learn more about this library through the articles I wrote about it =) 
 
1. [Android Contacts, Reborn][medium-blog]
2. [I spent 3 years writing an Android Contacts API in Kotlin with Java interop. What I’ve learned…][devto-blog]

**Note: This repo was open-sourced on October 4, 2021. It was private prior to that.**

## Features

> All features below are links to howto pages. Click on each of them to learn more!

The core library supports;
- All of the kinds of Data in the Contacts Provider;
  [address, email, event, group membership, IM, name, nickname, note, organization, phone, photo, relation, SIP address, and website](/howto/howto-learn-more-about-api-entities.md).
- [Custom data integration](/howto/howto-integrate-custom-data.md).
- [Broad queries](/howto/howto-query-contacts.md) and [advanced queries](/howto/howto-query-contacts-advanced.md)
  of Contacts and RawContacts from zero or more Accounts. [Include only desired fields](/howto/howto-include-only-desired-data.md)
  in the results (e.g. name and phone number) to conserve CPU and memory. Specify matching criteria 
  in an SQL WHERE clause fashion using Kotlin infix functions. Order by contact table columns. 
  Limit and offset functions.
- [Insert](/howto/howto-insert-contacts.md) one or more RawContacts with an associated Account,
  causing automatic insertion of a new Contact subject to automatic aggregation by the Contacts Provider.
- [Update](/howto/howto-update-contacts.md) one or more Contacts, RawContacts, and Data.
- [Delete](/howto/howto-delete-contacts.md) one or more Contacts, RawContacts, and Data.
- [Query](/howto/howto-query-profile.md), [insert](/howto/howto-insert-profile.md),
  [update](/howto/howto-update-profile.md), and [delete](/howto/howto-delete-profile.md)
   Profile (device owner) Contact, RawContact, and Data.
- [Query](/howto/howto-query-groups.md), [insert](/howto/howto-insert-groups.md),
  [update](/howto/howto-update-groups.md), and [delete](/howto/howto-delete-groups.md) Groups per Account.
- [Query](/howto/howto-query-data-sets.md), [insert](/howto/howto-insert-data-sets.md)
  [update](/howto/howto-update-data-sets.md ), and [delete](/howto/howto-delete-data-sets.md) specific kinds of Data.
- [Query](/howto/howto-query-custom-data.md), [insert](/howto/howto-insert-custom-data.md), 
  [update](/howto/howto-update-custom-data.md), and [delete](/howto/howto-delete-custom-data.md) custom Data.
- [Query](/howto/howto-query-accounts.md) for Accounts in the system or RawContacts table.
- [Query](/howto/howto-query-raw-contacts.md) for just RawContacts.
- [Associate local RawContacts (no Account) to an Account](/howto/howto-associate-device-local-raw-contacts-to-an-account.md).
- [Join/merge/link and separate/unmerge/unlink two or more Contacts](/howto/howto-link-unlink-contacts.md).
- [Get and set Contact and RawContact Options](/howto/howto-get-set-clear-contact-raw-contact-options.md).
  starred (favorite), custom ringtone, send to voicemail.
- [Get, set, and remove Contacts/RawContact photo and thumbnail](/howto/howto-get-set-remove-contact-raw-contact-photo.md).
- [Get, set, and clear default (primary) Contact Data](/howto/howto-get-set-clear-default-data.md)
  (e.g. default/primary phone number, email, etc).
- [Miscellaneous convenience functions](/howto/howto-use-miscellaneous-extensions.md).
- [Contact data is synced automatically across devices](/howto/howto-sync-contact-data.md).

There are also extensions that add functionality to every core function;
- [Asynchronous work using Kotlin Coroutines](/howto/howto-use-api-with-async-execution.md).
- [Permissions request/handling using Kotlin Coroutines](/howto/howto-use-api-with-permissions-handling.md).

Also included are some pre-baked goodies to be used as is or just for reference;
- [Gender custom Data](/howto/howto-integrate-gender-custom-data.md).
- [Handle name custom Data](/howto/howto-integrate-handlename-custom-data.md).
- [Rudimentary contacts-integrated UI components](/howto/howto-integrate-rudimentary-contacts-integrated-ui-components.md).
- [Debug functions to aid in development](/howto/howto-debug-contacts-provider-tables.md)

There are also more features that are on the way!
1. [Blocked phone numbers](https://github.com/vestrel00/contacts-android/issues/24).
2. [SIM card query, insert, update, and delete](https://github.com/vestrel00/contacts-android/issues/26).
3. [Read/write from/to .VCF file](https://github.com/vestrel00/contacts-android/issues/26).
4. [Social media custom data (WhatsApp, Twitter, Facebook, etc)](https://github.com/vestrel00/contacts-android/issues/27).

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
     implementation 'com.github.vestrel00:contacts-android:0.1.8'
}
```

To import specific modules,

```groovy
dependencies {
     implementation 'com.github.vestrel00.contacts-android:core:0.1.8'
}
```

> Notice that when importing specific modules/subprojects, the first ":" comes after "contacts-android".

SNAPSHOTs of branches are also available,

```groovy
dependencies {
     implementation 'com.github.vestrel00:contacts-android:main-SNAPSHOT'
}
```

This library is a multi-module project published with JitPack;
[![JitPack](https://jitpack.io/v/vestrel00/contacts-android.svg)](https://jitpack.io/#vestrel00/contacts-android)

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
- is favorited (starred)
- has a nickname of "DarEdEvil" (case sensitive)
- works for Facebook
- has a note
- belongs to the account of "john.doe@gmail.com" or "john.doe@myspace.com"

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
        Account("john.doe@gmail.com", "com.google"),
        Account("john.doe@myspace.com", "com.myspace"),
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
val contact: Contact
Log.d(
    "Contact",
    """
        ID: ${contact.id}

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

> Each Contact may have more than one of the following data if the Contact is made up of 2 or more
> RawContacts; name, nickname, note, organization, sip address.

> For more info, read [How do I learn more about the API entities?](/howto/howto-learn-more-about-api-entities.md)

## Setup

There is no setup required. It's up to you how you want to create and retain instances of the
`contacts.core.Contacts(context)` API. For more info, read [How do I setup the Contacts API?](/howto/howto-setup-contacts-api.md)

It is also useful to read [How do I learn more about the API entities?](/howto/howto-learn-more-about-api-entities.md)

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
    .where(Fields.Email.Address endsWith  "gmail.com")
    .orderBy(Fields.Email.Address.desc(ignoreCase = true))
    .offset(0)
    .limit(20)
    .find()
```

It's not just for emails. It's for all data kinds (including custom data).

> For more info, read [How do I get a list of specific data kinds?](/howto/howto-query-data-sets.md)

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
    .contacts(johnDoe.mutableCopy().apply {
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
        .insert()
        ...
        .commitAsync()
    val result = deferredResult.await()
}
```

> For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)
> and [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

So, if we call the above function and we don't yet have permission. The user will be prompted to
give the appropriate permissions before the query proceeds. Then, the work is done in the coroutine
context of choice (default is Dispatchers.IO). If the user does not give permission, the query will
return no results.

Extensions for Kotlin Flow and RxJava are also in the v1 roadmap, which includes APIs for listening
to Contacts database changes.

## Full in-code documentation and Howto guides and samples

**The above examples barely scratches the surface of what this library provides.** For more in-depth
Howtos, visit the [howto directory](/howto/). For a sample app reference, take a look at and run the
`sample` module.

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

[project-roadmap]: https://github.com/vestrel00/contacts-android/wiki/Project-Roadmap
[github-pages]: https://vestrel00.github.io/contacts-android/
[medium-blog]: https://proandroiddev.com/android-contacts-reborn-19985c73ad43
[devto-blog]: https://dev.to/vestrel00/i-spent-3-years-writing-an-android-contacts-api-in-kotlin-with-java-interop-what-ive-learned-54hp
[contacts-provider]: https://developer.android.com/guide/topics/providers/contacts-provider
[coroutines-proguard]: https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/resources/META-INF/proguard/coroutines.pro
