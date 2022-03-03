# Get set remove full-sized and thumbnail contact photos

This library provides several functions to interact with Contact and RawContact full-sized and 
thumbnail photos.

## Contact and RawContact photos

The photo assigned to a Contact is just a reference to a photo assigned to a RawContact. If a 
Contact consists of more than one RawContact, only the photo from one of the RawContacts will be 
used by the Contact. 

Setting/removing the (main) RawContact's photo will in turn change the Contact photo because the 
Contact photo is just a reference to the RawContact photo. The inverse is also true.

RawContact photos are retained when linking and unlinking.

> For more info, read [Link unlink Contacts](/docs/other/link-unlink-contacts.md).

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

> For more info, read [Query contacts](/docs/basics/query-contacts.md)
> and [Query contacts (advanced)](/docs/basics/query-contacts-advanced.md).

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

> Keep in mind that the Contact photo is just a reference to one of its RawContact's photo.

## Setting contact photo

Setting the photo can only be done after the Contact or RawContact has been inserted. In other 
words, photo management can only be done for existing Contacts/RawContacts.

To set the Contact photo, use one of the extension functions in `contacts.core.util.ContactPhoto.kt`,

```kotlin
contact.setPhoto(contactsApi, photoInputStream)
contact.setPhoto(contactsApi, photoBytes)
contact.setPhoto(contactsApi, photoBitmap)
contact.setPhoto(contactsApi, photoBitmapDrawable)
```

Setting the full-sized photo will automatically set the thumbnail. The Contacts Provider 
automatically creates a downsized version of the full-sized photo.

To set a RawContact photo, use one of the extension functions in `contacts.core.util.RawContactPhoto.kt`,

```kotlin
rawContact.setPhoto(contactsApi, photoInputStream)
rawContact.setPhoto(contactsApi, photoBytes)
rawContact.setPhoto(contactsApi, photoBitmap)
rawContact.setPhoto(contactsApi, photoBitmapDrawable)
```

> Keep in mind that the Contact photo is just a reference to one of its RawContact's photo.

## Removing contact photo

To remove the Contact (and corresponding RawContact) photo (full-sized and thumbnail),

```kotlin
contact.removePhoto(contactsApi)
```

To remove a specific RawContact's photo (full-sized and thumbnail),

```kotlin
rawContact.removePhoto(contactsApi)
```

> Keep in mind that the Contact photo is just a reference to one of its RawContact's photo.
A few things to keep in mind.

## Changes are immediate and are not applied to the receiver

These apply to set and remove functions.

1. Changes are immediate.
   - These functions will make the changes to the Contacts Provider database immediately. You do
     not need to use update APIs to commit the changes.
2. Changes are not applied to the receiver.
   - This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
     query APIs or refresh extensions or process the result of this function call to get the most
     up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
     Provider database.
      
## Using the ui PhotoPicker extensions

The `contacts.ui.util.PhotoPicker.kt` in the `ui` module` provides extension functions to make 
selecting existing photos, taking new photos, and removing photos easier. It provides you the same
UX as the native Contacts app. To use it,

```kotlin
Activity {
    fun onPhotoViewClicked() {
        showPhotoPickerDialog(
            withRemovePhotoOption = true,
            removePhoto = {
                contact.removePhoto(contactsApi)
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onPhotoPicked(requestCode, resultCode, data,
            photoBitmapPicked = { photoBitmap ->
                contact.setPhoto(contactsApi, photoBitmap)
            },
            photoUriPicked = { uri ->
                // Note that bitmap decoding should be done in a non-UI thread. Threading has been
                // left out of this example for brevity.
                val photoBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                contact.setPhoto(contactsApi, photoBitmap)
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
For more info, read [Execute work outside of the UI thread using coroutines](/docs/async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing photo management with permission

Getting and setting photos require the `android.permission.READ_CONTACTS` and 
`android.permission.WRITE_CONTACTS` permissions respectively. If not granted, getting/setting photos
will fail.

TODO Update this section as part of issue [#119](https://github.com/vestrel00/contacts-android/issues/119).

## FAQs

### Can contacts be insert with photo?

> Related issues; [#116](https://github.com/vestrel00/contacts-android/issues/116) 
> and [#119](https://github.com/vestrel00/contacts-android/issues/119)

You cannot get/set/remove photos for Contacts/RawContacts that have not yet been inserted in the
Contacts Provider database. In other words, only Contacts/RawContacts retrieved via query or result
APIs can use the extension functions in `contacts.core.util.ContactPhoto.kt` and
`contacts.core.util.RawContactPhoto.kt`.

To insert a new contact "with photo", you should insert the contact first. Then, if the insert
succeeds, proceed to set the photo.

> For more info about insert, read [Insert contacts](/docs/basics/insert-contacts.md).

> Note for contributors; It is possible to include photo **thumbnail** data as part of the insertion
> of a new RawContact using `ContactsContract.CommonDataKinds.Photo.PHOTO`. The Contacts Provider
> will use the thumbnail as the full-sized photo as well. However, this is not good practice as the
> full-sized photo will have a really low resolution. Showing the full-sized photo in a big view
> will not look good. Therefore, this library does not allow this. Consumers must first insert their
> new RawContact so that they can set the full-sized photo.

### Can photo be set using a uri instead of bytes and bitmaps? 

> Related issues; [#109](https://github.com/vestrel00/contacts-android/issues/110)

No and yes. The core APIs provided in this library only provides functions that the Contacts 
Provider natively supports. This means setting Contact or RawContact photo only using bytes (and 
other similar types). See documentation in `ContactsContract.RawContacts.DisplayPhoto`.

Photos are stored and managed by the Contacts Provider, which in turn provides specific URIs for 
RawContacts and Contacts for read/write access to those photos. We cannot simply just pass in our 
own URIs. The Contacts Provider will not accept it. The Contacts Provider will only accept raw photo
data. It will then generate and manage URIs on its own automatically to enforce data integrity.

Consumers may write their own functions to convert a URI to a byte array or bitmap using whatever 
imaging libraries they want. Certain URIs/URLs may require networking and heavy image processing, 
which this **Contacts library** will not cover! URI/URL to image conversion simply does not belong 
in this library! 
