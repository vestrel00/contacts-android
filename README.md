Move async UI code in sample to new ui-async module. Don’t forget to remove the documentation about it being in the sample module because of async. Also rename stings and dimens.

#### Complete sample app

Build the sample app as simple as possible. Use API 30 as reference- study behavior when there are
0, 1, or more accounts

----------------------------------------------------------------------------------------------------

#### Include in sample app

1. Add extra activities that showcase unused library features.

#### Tidy up

1. Lint / code quality checks (checkstyle, lint, etc)
2. Tests (androidTest).
    - Code coverage tool?

#### Final steps!

1. Update AS, Kotlin stdlib + coroutines, Gradle, Dexter, and all other dependencies before releasing.
    - Use sealed interface in Fields when upgrading to Kotlin 1.5+
2. Review remaining TODOs and FIXMEs.
3. Add consumerProguardFiles?
4. Create howto folder containing an md file for each;
    - “Querying contacts”, “Creating contacts”, “Updating contacts”, “Deleting contacts”,
      “Linking contacts”, etc mention parts of the sample app that demonstrates each function
    - “Create your own customdata”
5. Add CONTRIBUTING.md and CHANGELOG.md.
6. Complete README.
7. Draft medium blog.
8. Publish artifact starting at version 1.0.0-beta1.
9. Try out pubished artifact in a new project.
10. Immediately setup Travis CI (free for public repos).
11. Publish medium blog.
12. Promote via AndroidWeekly.

----------------------------------------------------------------------------------------------------

# Contacts

TODO

## Download

TODO

## Quick Start

TODO

## Requirements

- Min SDK 19+
- Java 7+

## Proguard

If you use Proguard and the `async` and/or `permissions`, you may need to add rules for
[Coroutines][coroutines-proguard].

## Upcoming features!

These features didn't make the v1 release because I wanted to get this library out as soon as 
possible so that the community may benefit from it and contribute back!

Most, if not all, of these upcoming features are the missing components required to rebuild the
native Android Contacts app from the ground up. In other words, each of these features allow 
consumers to implement a specific part of the native Android Contacts app.

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
4. Custom data from social media.
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

## Support

This is a newly open sourced library with only one contributor so far (me). Don’t expect much
support in the beginning. I am only able to work on this (and respond to issues) outside of work
hours. This means late nights, weekends, and maybe holidays.

As time passes, hopefully this library gets more and more contributors. At some point, I hope to
gain contributors that have become experts on this library to help me support the community by
maintaining it and making admin-level decisions.

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

[coroutines-proguard]: https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/resources/META-INF/proguard/coroutines.pro
