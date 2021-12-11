package contacts.core.util

import android.content.ContentProviderOperation.newDelete
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract.RawContacts
import contacts.core.*
import contacts.core.entities.MimeType
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.cursor.photoCursor
import contacts.core.entities.operation.withSelection
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
        val fd = contacts.applicationContext.contentResolver.openAssetFileDescriptor(photoUri, "r")
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
        BitmapDrawable(contacts.applicationContext.resources, it)
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
fun ExistingRawContactEntity.photoThumbnailInputStream(contacts: Contacts): InputStream? {
    if (!contacts.permissions.canQuery()) {
        return null
    }

    return contacts.applicationContext.contentResolver.query(
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
fun ExistingRawContactEntity.photoThumbnailBitmapDrawable(contacts: Contacts): BitmapDrawable? =
    photoThumbnailInputStream(contacts)?.apply {
        BitmapDrawable(contacts.applicationContext.resources, it)
    }

// endregion

// region SET PHOTO

/**
 * Sets the photo of this [ExistingRawContactEntity]. If a photo already exists, it will be
 * overwritten. The Contacts Provider automatically creates a downsized version of this as the
 * thumbnail.
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
fun ExistingRawContactEntity.setPhoto(contacts: Contacts, photoBytes: ByteArray): Boolean =
    contacts.setRawContactPhoto(id, photoBytes)

/**
 * See [ExistingRawContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.setPhoto(contacts: Contacts, photoInputStream: InputStream): Boolean =
    setPhoto(contacts, photoInputStream.readBytes())

/**
 * See [ExistingRawContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.setPhoto(contacts: Contacts, photoBitmap: Bitmap): Boolean =
    setPhoto(contacts, photoBitmap.bytes())

/**
 * See [ExistingRawContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingRawContactEntity.setPhoto(contacts: Contacts, photoDrawable: BitmapDrawable): Boolean =
    setPhoto(contacts, photoDrawable.bitmap.bytes())

/**
 * Performs the actual setting of the photo.
 */
internal fun Contacts.setRawContactPhoto(
    rawContactId: Long,
    photoBytes: ByteArray
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
        val fd = applicationContext.contentResolver
            .openAssetFileDescriptor(photoUri, "rw")
        if (fd != null) {
            val os = fd.createOutputStream()

            os.write(photoBytes)

            os.close()
            fd.close()

            isSuccessful = true
        }
    } catch (ioe: IOException) {
        // do nothing
    }
    return isSuccessful
}

internal fun Bitmap.bytes(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

// endregion

// region REMOVE PHOTO

/**
 * Removes the photo of this [ExistingRawContactEntity], if one exists.
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
fun ExistingRawContactEntity.removePhoto(contacts: Contacts): Boolean {
    if (!contacts.permissions.canUpdateDelete()) {
        return false
    }

    return contacts.applicationContext.contentResolver.applyBatch(
        newDelete(if (isProfile) ProfileUris.DATA.uri else Table.Data.uri)
            .withSelection(
                (Fields.RawContact.Id equalTo id)
                        and (Fields.MimeType equalTo MimeType.Photo)
            )
            .build()
    ) != null
}
// endregion