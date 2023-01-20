# Get set remove full-sized and thumbnail contact photos

This library provides several functions to interact with Contact and RawContact full-sized and 
thumbnail photos.

> ⚠️ The APIs for this have changed significantly since [version 0.3.0](https://github.com/vestrel00/contacts-android/discussions/218).
> For documentation for [version 0.2.4](https://github.com/vestrel00/contacts-android/releases/tag/0.2.4) 
> and below, [visit this page (click me)](https://github.com/vestrel00/contacts-android/blob/0.2.4/docs/other/get-set-remove-contact-raw-contact-photo.md).

## Contact and RawContact photos

The photo assigned to a Contact is just a reference to a photo assigned to a RawContact. If a 
Contact consists of more than one RawContact, only the photo from one of the RawContacts will be 
used by the Contact. 

Setting/removing the (main) RawContact's photo will in turn change the Contact photo because the 
Contact photo is just a reference to the RawContact photo. The inverse is also true.

RawContact photos are retained when linking and unlinking.

> ℹ️ For more info, read [Link unlink Contacts](./../other/link-unlink-contacts.md).

## Full-sized photos and thumbnails

Each RawContact may be assigned one photo. The thumbnail is just a downsized version of the 
full-sized photo. The full-sized photo is typically displayed in a large view, such as in a contact
detail screen. The thumbnail is typically displayed in small views, such as in a contacts list view.

Setting the full-sized photo will automatically set the thumbnail. The Contacts Provider 
automatically creates a downsized version of the full-sized photo.

## Getting contact photo

There are several ways to do this.

Using query APIs to get a list of `Contact`s with photo uris,

```kotlin
val contacts = Contacts(context)
    .query()
    // if you only want to include photo data in the returned Contacts
    .include(
        Fields.Contact.PhotoUri,
        Fields.Contact.PhotoThumbnailUri
    ) 
    .find()

for (contact in contacts) {
    Log.d(
        "Contact",
        """
            Photo Uri: ${contact.photoUri}
            Thumbnail Uri: ${contact.photoThumbnailUri}
        """.trimIndent()
    )
}
```

> ℹ️ For more info, read [Query contacts](./../basics/query-contacts.md)
> and [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

Using one of the extension functions in `contacts.core.util.ContactPhoto.kt` to get photo data,

```kotlin
val photoInputStream = contact.photoInputStream(contactsApi)
val photoBytes = contact.photoBytes(contactsApi)
val photoBitmap = contact.photoBitmap(contactsApi)
val photoBitmapDrawable = contact.photoBitmapDrawable(contactsApi)

val photoThumbnailInputStream = contact.photoThumbnailInputStream(contactsApi)
val photoThumbnailBytes = contact.photoThumbnailBytes(contactsApi)
val photoThumbnailBitmap = contact.photoThumbnailBitmap(contactsApi)
val photoThumbnailBitmapDrawable = contact.photoThumbnailBitmapDrawable(contactsApi)
```

To get RawContact photos directly, use one of the extension functions in `contacts.core.util.RawContactPhoto.kt`,

```kotlin
val photoInputStream = rawContact.photoInputStream(contactsApi)
val photoBytes = rawContact.photoBytes(contactsApi)
val photoBitmap = rawContact.photoBitmap(contactsApi)
val photoBitmapDrawable = rawContact.photoBitmapDrawable(contactsApi)

val photoThumbnailInputStream = rawContact.photoThumbnailInputStream(contactsApi)
val photoThumbnailBytes = rawContact.photoThumbnailBytes(contactsApi)
val photoThumbnailBitmap = rawContact.photoThumbnailBitmap(contactsApi)
val photoThumbnailBitmapDrawable = rawContact.photoThumbnailBitmapDrawable(contactsApi)
```

> ℹ️ The Contact photo is just a reference to one of its RawContact's photo.

## Setting contact photo

There are two ways to set Contact or RawContact photo.

### Using extension functions

This can only be done for existing Contacts/RawContacts.

To set the Contact photo, use one of the extension functions in `contacts.core.util.ContactPhoto.kt`,

```kotlin
contact.setPhotoDirect(contactsApi, PhotoData.from(inputStream))
contact.setPhotoDirect(contactsApi, PhotoData.from(byteArray))
contact.setPhotoDirect(contactsApi, PhotoData.from(bitmap))
contact.setPhotoDirect(contactsApi, PhotoData.from(bitmapDrawable))
```

Setting the full-sized photo will automatically set the thumbnail. The Contacts Provider 
automatically creates a downsized version of the full-sized photo.

To set a RawContact photo, use one of the extension functions in `contacts.core.util.RawContactPhoto.kt`,

```kotlin
rawContact.setPhotoDirect(contactsApi, PhotoData.from(inputStream))
rawContact.setPhotoDirect(contactsApi, PhotoData.from(byteArray))
rawContact.setPhotoDirect(contactsApi, PhotoData.from(bitmap))
rawContact.setPhotoDirect(contactsApi, PhotoData.from(bitmapDrawable))
```

> ℹ️ Prior to [version 0.3.0](https://github.com/vestrel00/contacts-android/discussions/218), these
> functions were named `setPhoto`.

### As part of an insert or update API call

> ℹ️ Setting photo as part of insert or update API calls was not possible prior to
> [version 0.3.0](https://github.com/vestrel00/contacts-android/discussions/218).

To insert a new RawContact with a photo,

```kotlin
Contacts(this)
    .insert()
    .rawContact {
        setPhoto(PhotoData.from(...))
    }
    .commit()
```

> ℹ️ For more info on insert APIs, read [Insert contacts](../basics/insert-contacts.md).

To update an existing Contact or RawContact with a photo,

```kotlin
Contacts(this)
    .update()
    .contacts(
        contact.mutableCopy {
            setPhoto(PhotoData.from(...))
        }
    )
    .rawContacts(
        rawContact.mutableCopy {
            setPhoto(PhotoData.from(...))
        }
    )
    .commit()
```

> ℹ️ For more info on update APIs, read [Update contacts](../basics/update-contacts.md).

## Removing contact photo

There are two ways to remove Contact or RawContact photo.

### Using extension functions

This can only be done for existing Contacts/RawContacts.

To remove the Contact (and corresponding RawContact) photo (full-sized and thumbnail),

```kotlin
contact.removePhotoDirect(contactsApi)
```

To remove a specific RawContact's photo (full-sized and thumbnail),

```kotlin
rawContact.removePhotoDirect(contactsApi)
```

> ℹ️ Prior to [version 0.3.0](https://github.com/vestrel00/contacts-android/discussions/218), these
> functions were named `removePhoto`.

### As part of an update API call

> ℹ️ Removing photo as part of insert or update API calls was not possible prior to
> [version 0.3.0](https://github.com/vestrel00/contacts-android/discussions/218).

To update an existing Contact or RawContact without a photo,

```kotlin
Contacts(this)
    .update()
    .contacts(
        contact.mutableCopy {
            removePhoto()
        }
    )
    .rawContacts(
        rawContact.mutableCopy {
            removePhoto()
        }
    )
    .commit()
```

> ℹ️ For more info on update APIs, read [Update contacts](../basics/update-contacts.md).

## Using the ui PhotoPicker extensions

The `contacts.ui.util.PhotoPicker.kt` in the `ui` module` provides extension functions to make 
selecting existing photos, taking new photos, and removing photos easier. It provides you the same
UX as the AOSP Contacts app. To use it,

```kotlin
Activity {
    fun onPhotoViewClicked() {
        showPhotoPickerDialog(
            withRemovePhotoOption = true,
            removePhoto = {
                // remove contact photo
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onPhotoPicked(requestCode, resultCode, data,
            photoBitmapPicked = { photoBitmap ->
                // set contact photo
            },
            photoUriPicked = { uri ->
                // Note that bitmap decoding should be done in a non-UI thread. Threading has been
                // left out of this example for brevity.
                val photoBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                // set contact photo
            }
        )
    }
}
```

Starting with Android 11 (API 30), you must include the following to your manifest in order to
successfully use the above functions.

```
<queries>
   <intent>
      <action android:name="android.media.action.IMAGE_CAPTURE" />
   </intent>
   <intent>
      <action android:name="android.intent.action.PICK" />
   </intent>
</queries>
```

## Performing photo management asynchronously

All of the code shown in this guide are done in the same thread as the call-site. This may result 
in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing photo management with permission

Getting and setting photos require the `android.permission.READ_CONTACTS` and 
`android.permission.WRITE_CONTACTS` permissions respectively. If not granted, getting/setting photos
will fail.

To perform the get/set photo with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)