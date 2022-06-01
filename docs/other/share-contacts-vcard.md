# Share contacts vCard (.VCF)

This library provides several functions to create `Intent`s that allow you to share contacts from 
the Contacts Provider database.

> ℹ️ These APIs are available since [version 0.3.0](https://github.com/vestrel00/contacts-android/discussions/160)
> upon implementing [issue #211](https://github.com/vestrel00/contacts-android/issues/211).

## Share an existing contact

To share a single existing contact, use the extensions in `contacts.core.util.ContactShare`,

```kotlin
val contact: ExistingContactEntity
val shareIntent = contact.shareVCardIntent()
if (shareIntent != null) {
    activity.startActivity(Intent.createChooser(shareIntent, null))
}
```

The above code will open up a share sheet that will allow you to send the _.VCF_ file (a vCard) 
containing the contact data. Opening this file in any OS (iOS, OSX, Windows) typically prompts the
addition of the contact contained in the vCard.

## Share multiple existing contacts

To share multiple existing contacts, use the extensions in `contacts.core.util.ContactShare`,

```kotlin
val contacts: Collection<ExistingContactEntity>
val shareIntent = contacts.shareMultiVCardIntent()
if (shareIntent != null) {
    activity.startActivity(Intent.createChooser(shareIntent, null))
}
```

The above code will open up a share sheet that will allow you to send the _.VCF_ file (a vCard)
containing all contacts' data. Opening this file in any OS (iOS, OSX, Windows) typically prompts the
addition of all contact(s) contained in the vCard.

> ⚠️ The `shareMultiVCardIntent` function is only supported for API 21+. A null `Intent` will be
> returned for lower API levels.

## Excluding photo data

By default, photo (thumbnail) data are included in the vCard. To exclude photo data to minimize
file size, set the `includePhoto` parameter to false,

```kotlin
contact.shareVCardIntent(includePhoto = false)
contacts.shareMultiVCardIntent(includePhoto = false)
```

> ⚠️ This optional parameter is only supported for API 23 and above. It does nothing for lower 
> API levels.

> ⚠️ This optional parameter does not seem to do anything for `shareMultiVCardIntent`. Photo data is
> still included in the output vCard even if this is set to false. The `shareVCardIntent` does not 
> have this issue.

## Custom data support

Custom data are not supported by any of these functions. Custom data will not be included in the 
output vCard.

## Advance contact sharing via customizable vCard exports

Currently, the share functions provided use the builtin vCard export functions of the Contacts 
Provider. **In the future**, when 
[Read/write from/to .VCF file (issue #26)](https://github.com/vestrel00/contacts-android/issues/26)
is implemented, you will be able to...

- share (export) new contacts that are not in the database
- share (export) existing contacts with changes that are not in the database
- include only specified fields to export

Be excited for the future!
