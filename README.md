#### Complete sample app

Build the sample app as simple as possible, referencing new and older versions to ensure all
functionality is included (though may look different).

- View Contact
    - Star (favorite) contact
    - Edit
    - Join/Separate (API 22-), Merge/Unmerge (API 23), Link/unlink (API 24+) Contacts.
    - Delete
    - Share (post v1)
    - Create shortcut / Place on Home screen
    - Set ringtone
    - Route to voicemail / All calls to voicemail
    - Press action for each data
    - Long press options for each data
      - Data
      - Copy to clipboard
      - Set default / clear default (isSuperPrimary)
      
- Edit Contact / RawContact
    - Saving to which account (uneditable)
    - Combined edit mode (editing multiple linked raw contacts) available in API 24 but removed in
      API (28?) and not in API 22. Try all supported API levels! Choose behavior of simplest API. 
      Document this.
    - Save
    - Discard changes
    
- Create contact
    - Saving to which account (editable)
        - What happens to fields (groups) that have been filled out when different account is picked?
        - Preference for default account for new contacts
        
- Contacts list
    - Long press options
      - Share (post v1)
      - Delete
      - Link (when multiple selected)
                
- View starred (favorites only) contacts

- No account & no contacts screen
    - Add account

- Settings (see native Contacts App pre Nougat and post Nougat)
  - User profile
  - Accounts
  - Default account for new contacts
    - Contacts to display
        - All contacts
        - ... account(s) ...
        - Customize (Define custom view)
            - ... account(s) ...
                - ... groups ...
                    - Long press -> Remove sync group
                    - More groups... (Add sync group)
                    - All other contacts
  - Sort by first or last name
  - Name format first name first or last name first

----------------------------------------------------------------------------------------------------

#### Include in sample app

1. Add extra activities that showcase unused library features.
2. Ensure all groups of library features are used in both Kotlin and Java.
    - This ensures that Java is also supported.

#### Tidy up

1. Lint / code quality checks (checkstyle, lint, etc).
2. Unit test.
3. Espresso test.
    - Test RAM usage when paginating hundreds of thousands of contacts.

#### Final steps!

1. Update AS, Kotlin stdlib + coroutines, Gradle, Dexter, and all other dependencies before releasing.
2. Review remaining TODOs and FIXMEs.
3. Add consumerProguardFiles?
4. Add CONTRIBUTING.md and CHANGELOG.md.
5. Complete README.
6. Draft medium blog.
7. Publish artifact.
8. Try out pubished artifact in a new project.
9. Immediately setup Travis CI (free for public repos).
10. Publish medium blog.
11. Promote via AndroidWeekly.

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

## License

    Copyright 2020 Contacts Contributors

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
