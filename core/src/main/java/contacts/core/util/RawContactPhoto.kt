package contacts.core.util

import android.content.ContentProviderOperation.newDelete
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract.RawContacts
import contacts.core.*
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.MimeType
import contacts.core.entities.MutableRawContact
import contacts.core.entities.NewRawContact
import contacts.core.entities.cursor.photoCursor
import contacts.core.entities.operation.withSelection
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

// region GET PHOTO

/**
 * Returns the full-sized photo as an [InputStream].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * RawContact no longer exists.
 *
 * It is up to the caller to close the [InputStream].
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.photoInputStream(contacts: Contacts): InputStream? {
    if (!contacts.permissions.canQuery()) {
        return null
    }

    val photoUri = Uri.withAppendedPath(
        // This is also used to set Profile photos along with non-Profile photos.
        ContentUris.withAppendedId(RawContacts.CONTENT_URI, id),
        RawContacts.DisplayPhoto.CONTENT_DIRECTORY
    )

    var inputStream: InputStream? = null
    try {
        val fd = contacts.contentResolver.openAssetFileDescriptor(photoUri, "r")
        inputStream = fd?.createInputStream()
    } catch (ioe: IOException) {
        // do nothing
    }
    return inputStream
}

/**
 * Returns the full-sized photo as a [ByteArray].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * RawContact no longer exists.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.photoBytes(contacts: Contacts): ByteArray? =
    photoInputStream(contacts)?.apply {
        it.readBytes()
    }

/**
 * Returns the full-sized photo as a [Bitmap].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * RawContact no longer exists.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.photoBitmap(contacts: Contacts): Bitmap? =
    photoInputStream(contacts)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * Returns the full-sized photo as a [BitmapDrawable].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * RawContact no longer exists.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.photoBitmapDrawable(contacts: Contacts): BitmapDrawable? =
    photoInputStream(contacts)?.apply {
        BitmapDrawable(contacts.resources, it)
    }

internal inline fun <T> InputStream.apply(block: (InputStream) -> T): T {
    val t = block(this)
    close()
    return t
}

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Returns the photo thumbnail as an [InputStream].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * RawContact no longer exists.
 *w
 * It is up to the caller to close the [InputStream].
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.photoThumbnailInputStream(
    contacts: Contacts
): InputStream? {
    if (!contacts.permissions.canQuery()) {
        return null
    }

    return contacts.contentResolver.query(
        if (isProfile) ProfileUris.DATA.uri else Table.Data.uri,
        Include(Fields.Photo.PhotoThumbnail),
        (Fields.RawContact.Id equalTo id)
                and (Fields.MimeType equalTo MimeType.Photo)
    ) {
        val photoThumbnail = it.getNextOrNull { it.photoCursor().photoThumbnail }
        photoThumbnail?.let(::ByteArrayInputStream)
    }
}

/**
 * Returns the photo thumbnail as a [ByteArray].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * RawContact no longer exists.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.photoThumbnailBytes(contacts: Contacts): ByteArray? =
    photoThumbnailInputStream(contacts)?.apply {
        it.readBytes()
    }

/**
 * Returns the photo thumbnail as a [Bitmap].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * RawContact no longer exists.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.photoThumbnailBitmap(contacts: Contacts): Bitmap? =
    photoThumbnailInputStream(contacts)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * Returns the photo thumbnail as a [BitmapDrawable].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * RawContact no longer exists.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.photoThumbnailBitmapDrawable(
    contacts: Contacts
): BitmapDrawable? = photoThumbnailInputStream(contacts)?.apply {
    BitmapDrawable(contacts.resources, it)
}

// endregion

// region SET PHOTO

/**
 * Set the photo of this [MutableRawContact], pending an update API call.
 *
 * The given [photoData] will not be set until the update API call is committed successfully.
 *
 * If you want to directly set the photo into the database, without an update API call, use
 * [MutableRawContact.setPhotoDirect].
 *
 * ## No includes required
 *
 * When using update APIs, there is is no field required to be passed into the `include` function
 * to make sure the photo is set as long as this function is invoked.
 *
 * ## Not parcelable
 *
 * The [photoData] is ignored on parcel. The [photoData] will not be carried over across activities,
 * fragments, or views during creation/recreation.
 */
fun MutableRawContact.setPhoto(photoData: PhotoData) {
    photoDataOperation = PhotoDataOperation.SetPhoto(photoData)
}

/**
 * Set the photo of this [NewRawContact], pending an insert API call.
 *
 * The given [photoData] will not be set until the insert API call is committed successfully.
 *
 * ## No includes required
 *
 * When using insert APIs, there is is no field required to be passed into the `include` function
 * to make sure the photo is set as long as this function is invoked.
 *
 * ## Not parcelable
 *
 * The [photoData] is ignored on parcel. The [photoData] will not be carried over across activities,
 * fragments, or views during creation/recreation.
 */
fun NewRawContact.setPhoto(photoData: PhotoData) {
    photoDataOperation = PhotoDataOperation.SetPhoto(photoData)
}

// endregion

// region SET PHOTO DIRECT

/**
 * Sets the photo of this [ExistingRawContactEntity] directly to the database. If a photo already
 * exists, it will be overwritten. The Contacts Provider automatically creates a downsized version
 * of this as the thumbnail.
 *
 * If this [ExistingRawContactEntity] is the only one that make up a
 * [contacts.core.entities.ContactEntity], then the photo set here will also be used by the
 * Contacts Provider as the contact photo. Otherwise, it may or may not be the photo picked by the
 * Contacts Provider as the contact photo.
 *
 * Returns true if the operation succeeds.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Changes are immediate
 *
 * This function will make the changes to the Contacts Provider database immediately. You do not
 * need to use update APIs to commit the changes.
 *
 * Note that the Contact photo columns may not be set immediately after setting the photo for the
 * RawContact. It is probably done asynchronously by the Contacts Provider.
 *
 * If you want to set the photo lazily, upon and update API call, use [MutableRawContact.setPhoto].
 *
 * ## Changes are not applied to the receiver
 *
 * This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
 * query APIs or refresh extensions or process the result of this function call to get the most
 * up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
 * Provider database.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 *
 * ## Developer notes
 *
 * The function body is mostly taken from the sample code from the [RawContacts.DisplayPhoto] class
 * documentation.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.setPhotoDirect(
    contacts: Contacts,
    photoData: PhotoData
): Boolean = contacts.setRawContactPhotoDirect(id, photoData)

/**
 * Performs the actual setting of the photo.
 */
internal fun Contacts.setRawContactPhotoDirect(
    rawContactId: Long,
    photoData: PhotoData
): Boolean {
    if (!permissions.canUpdateDelete()) {
        return false
    }

    var isSuccessful = false
    try {
        val photoUri = Uri.withAppendedPath(
            // This is also used to set Profile photos along with non-Profile photos.
            ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
            RawContacts.DisplayPhoto.CONTENT_DIRECTORY
        )

        // Didn't want to force unwrap because I'm trying to keep the codebase free of it.
        // I wanted to fold the if-return using ?: but it results in a lint error about unreachable
        // code (it's not unreachable).
        val fd = contentResolver.openAssetFileDescriptor(photoUri, "rw")
        if (fd != null) {
            val os = fd.createOutputStream()

            os.write(photoData.bytes())

            os.close()
            fd.close()

            isSuccessful = true
        }
    } catch (ioe: IOException) {
        // do nothing
    }
    return isSuccessful
}

// endregion

// region REMOVE PHOTO

/**
 * Removes the photo of this [MutableRawContact], pending an update API call.
 *
 * The photo will not be removed until the update API call is committed successfully.
 *
 * If you want to directly remove the photo from the database, without an update API call, use
 * [ExistingRawContactEntity.removePhotoDirect].
 *
 * ## No includes required
 *
 * When using update APIs, there is is no field required to be passed into the `include` function
 * to make sure the photo is removed as long as this function is invoked.
 *
 * ## Not parcelable
 *
 * This action is ignored on parcel. This action will not be carried over across activities,
 * fragments, or views during creation/recreation.
 */
fun MutableRawContact.removePhoto() {
    photoDataOperation = PhotoDataOperation.RemovePhoto
}

/**
 * Removes the pending photo of this [NewRawContact], pending an insert API call.
 *
 * Any photo set prior will not be included in the insert API call.
 *
 * ## Not parcelable
 *
 * This action is ignored on parcel. This action will not be carried over across activities,
 * fragments, or views during creation/recreation.
 */
fun NewRawContact.removePhoto() {
    photoDataOperation = PhotoDataOperation.RemovePhoto
}

// endregion

// region REMOVE PHOTO DIRECT

/**
 * Removes the photo of this [ExistingRawContactEntity] directly from the database, if one exists.
 *
 * If this [ExistingRawContactEntity] is the only one that make up a
 * [contacts.core.entities.ContactEntity], then the contact photo will also be removed. Otherwise,
 * it may or may not affect the contact photo.
 *
 * Returns true if the operation succeeds.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Changes are immediate
 *
 * This function will make the changes to the Contacts Provider database immediately. You do not
 * need to use update APIs to commit the changes.
 *
 * If you want to remove the photo lazily, upon and update API call, use
 * [MutableRawContact.removePhoto].
 *
 * ## Changes are not applied to the receiver
 *
 * This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
 * query APIs or refresh extensions or process the result of this function call to get the most
 * up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
 * Provider database.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.removePhotoDirect(contacts: Contacts): Boolean =
    contacts.removeRawContactPhotoDirect(id)

/**
 * Performs the actual removal of the photo.
 */
internal fun Contacts.removeRawContactPhotoDirect(rawContactId: Long): Boolean {
    if (!permissions.canUpdateDelete()) {
        return false
    }

    return contentResolver.applyBatch(
        newDelete(if (rawContactId.isProfileId) ProfileUris.DATA.uri else Table.Data.uri)
            .withSelection(
                (Fields.RawContact.Id equalTo rawContactId)
                        and (Fields.MimeType equalTo MimeType.Photo)
            )
            .build()
    ) != null
}
// endregion