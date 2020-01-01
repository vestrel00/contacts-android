package com.vestrel00.contacts.util

import android.content.ContentProviderOperation
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.INVALID_ID
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.entities.cursor.ContactsCursor
import com.vestrel00.contacts.entities.cursor.PhotoCursor
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo
import java.io.InputStream

// SET PHOTO

/**
 * Sets the photo of this [Contact] (and the [com.vestrel00.contacts.entities.RawContact] that the
 * Contacts Provider has chosen to hold the primary photo). If a photo already exists, it will be
 * overwritten. The Contacts Provider automatically creates a downsized version of this as the
 * thumbnail.
 *
 * If a photo has not yet been set and the Contacts Provider has not yet chosen the RawContact that
 * will be used as the primary photo holder, then this will use the first RawContact in the list of
 * [Contact.rawContacts].
 *
 * Returns true if the operation succeeds.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 *
 * ## DEV NOTES
 *
 * The function body is mostly taken from the sample code from the [RawContacts.DisplayPhoto]
 * class documentation.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun Contact.setPhoto(photoBytes: ByteArray, context: Context): Boolean =
    setContactPhoto(id, rawContacts.firstOrNull()?.id, photoBytes, context)

/**
 * See [Contact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun Contact.setPhoto(photoInputStream: InputStream, context: Context): Boolean =
    setPhoto(photoInputStream.readBytes(), context)

/**
 * See [Contact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun Contact.setPhoto(photoBitmap: Bitmap, context: Context): Boolean =
    setPhoto(photoBitmap.bytes(), context)

/**
 * See [Contact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun Contact.setPhoto(photoDrawable: BitmapDrawable, context: Context): Boolean =
    setPhoto(photoDrawable.bitmap.bytes(), context)

/**
 * See [Contact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.setPhoto(photoBytes: ByteArray, context: Context): Boolean =
    setContactPhoto(id, rawContacts.firstOrNull()?.id, photoBytes, context)

/**
 * See [MutableContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.setPhoto(photoInputStream: InputStream, context: Context): Boolean =
    setPhoto(photoInputStream.readBytes(), context)

/**
 * See [MutableContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.setPhoto(photoBitmap: Bitmap, context: Context): Boolean =
    setPhoto(photoBitmap.bytes(), context)

/**
 * See [MutableContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.setPhoto(photoDrawable: BitmapDrawable, context: Context): Boolean =
    setPhoto(photoDrawable.bitmap.bytes(), context)

// REMOVE PHOTO

/**
 * Removes the photos of all the RawContacts associated with this [Contact], if any exists.
 *
 * Returns true if the operation succeeds.
 *
 * The native Contacts app actually does not provide the option to remove the photo of a Contact
 * with at least 2 associated RawContacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun Contact.removePhoto(context: Context): Boolean = removeContactPhoto(id, context)

/**
 * See [Contact.removePhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.removePhoto(context: Context): Boolean = removeContactPhoto(id, context)

// GET PHOTO

/**
 * Returns the full-sized photo as an [InputStream]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [Contact.rawContacts] depending on
 * query filters.
 *
 * The stream should be closed after use.
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
fun Contact.photoInputStream(context: Context): InputStream? = photoUriInputStream(id, context)

/**
 * Returns the full-sized photo as a [ByteArray]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [Contact.rawContacts] depending on
 * query filters.
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
fun Contact.photoBytes(context: Context): ByteArray? = photoInputStream(context)?.apply {
    it.readBytes()
}

/**
 * Returns the full-sized photo as a [Bitmap]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [Contact.rawContacts] depending on
 * query filters..
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
fun Contact.photoBitmap(context: Context): Bitmap? = photoInputStream(context)?.apply {
    BitmapFactory.decodeStream(it)
}

/**
 * Returns the full-sized photo as a [BitmapDrawable]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [Contact.rawContacts] depending on
 * query filters.
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
fun Contact.photoBitmapDrawable(context: Context): BitmapDrawable? =
    photoInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

/**
 * See [Contact.photoInputStream].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.photoInputStream(context: Context): InputStream? =
    photoUriInputStream(id, context)

/**
 * See [Contact.photoBytes].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.photoBytes(context: Context): ByteArray? =
    photoInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * See [Contact.photoBitmap].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.photoBitmap(context: Context): Bitmap? = photoInputStream(context)?.apply {
    BitmapFactory.decodeStream(it)
}

/**
 * See [Contact.photoBitmapDrawable].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.photoBitmapDrawable(context: Context): BitmapDrawable? =
    photoInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

// GET PHOTO THUMBNAIL

/**
 * Returns the photo thumbnail as an [InputStream]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [Contact.rawContacts] depending on
 * query filters.
 *
 * The stream should be closed after use.
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
fun Contact.photoThumbnailInputStream(context: Context): InputStream? =
    photoThumbnailUriInputStream(id, context)

/**
 * Returns the photo thumbnail as a [ByteArray]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [Contact.rawContacts] depending on
 * query filters.
 *
 * The stream should be closed after use.
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
fun Contact.photoThumbnailBytes(context: Context): ByteArray? =
    photoThumbnailInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * Returns the full-sized photo as a [Bitmap]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [Contact.rawContacts] depending on
 * query filters.
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
fun Contact.photoThumbnailBitmap(context: Context): Bitmap? =
    photoThumbnailInputStream(context)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * Returns the full-sized photo as a [BitmapDrawable]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [Contact.rawContacts] depending on
 * query filters.
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
fun Contact.photoThumbnailBitmapDrawable(context: Context): BitmapDrawable? =
    photoThumbnailInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

/**
 * See [Contact.photoThumbnailInputStream].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.photoThumbnailInputStream(context: Context): InputStream? =
    photoThumbnailUriInputStream(id, context)

/**
 * See [Contact.photoThumbnailBytes].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.photoThumbnailBytes(context: Context): ByteArray? =
    photoThumbnailInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * See [Contact.photoThumbnailBitmap].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.photoThumbnailBitmap(context: Context): Bitmap? =
    photoThumbnailInputStream(context)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * See [Contact.photoThumbnailBitmapDrawable].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.photoThumbnailBitmapDrawable(context: Context): BitmapDrawable? =
    photoThumbnailInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

// HELPERS

private fun setContactPhoto(
    contactId: Long, defaultRawContactId: Long?, photoBytes: ByteArray, context: Context
): Boolean {
    if (
        !ContactsPermissions(context).canInsertUpdateDelete()
        || contactId == INVALID_ID
        || defaultRawContactId == INVALID_ID
    ) {
        return false
    }

    val photoFileId = photoFileId(contactId, context)

    val rawContactId = if (photoFileId != null) {
        // A photo exists for the Contact. Get the ID of the RawContact in the Data table that holds
        // the photo row with the same photo file id. Keep in mind that there may be multiple
        // RawContacts associated with a single Contact.
        rawContactIdWithPhotoFileId(photoFileId, context)
    } else {
        // No photo exists for the Contact or any of its associated RawContacts. Use the
        // defaultRawContactId.
        defaultRawContactId ?: INVALID_ID
    }

    return setRawContactPhoto(rawContactId, photoBytes, context)
}


// Removing the photo data rows does the trick!
private fun removeContactPhoto(contactId: Long, context: Context): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || contactId == INVALID_ID) {
        return false
    }

    val deleteContactPhotosOperation = ContentProviderOperation.newDelete(Table.DATA.uri)
        .withSelection(
            "${(Fields.Contact.Id equalTo contactId)
                    and (Fields.MimeType equalTo MimeType.PHOTO)}",
            null
        )
        .build()

    // Delete returns the number of rows deleted, which doesn't indicate if the delete operation
    // succeeded or not because there may have not been a row to delete. Therefore, we use
    // applyBatch instead, which should indicate success or failure via exception throwing.
    try {
        context.contentResolver.applyBatch(
            ContactsContract.AUTHORITY,
            arrayListOf(deleteContactPhotosOperation)
        )
    } catch (exception: Exception) {
        return false
    }

    return true
}

private fun photoFileId(contactId: Long, context: Context): Long? {
    if (!ContactsPermissions(context).canQuery() || contactId == INVALID_ID) {
        return null
    }

    val cursor = context.contentResolver.query(
        Table.CONTACTS.uri,
        arrayOf(Fields.Contacts.PhotoFileId.columnName),
        "${Fields.Contacts.Id equalTo contactId}",
        null,
        null
    )

    var photoFileId: Long? = null
    if (cursor != null && cursor.moveToNext()) {
        photoFileId = ContactsCursor(cursor).photoFileId

        cursor.close()
    }

    return photoFileId
}

private fun rawContactIdWithPhotoFileId(photoFileId: Long, context: Context): Long {
    val cursor = context.contentResolver.query(
        Table.DATA.uri,
        arrayOf(Fields.RawContactId.columnName),
        "${Fields.Photo.PhotoFileId equalTo photoFileId}",
        null,
        null
    )

    var rawContactId: Long? = null
    if (cursor != null && cursor.moveToNext()) {
        rawContactId = PhotoCursor(cursor).rawContactId

        cursor.close()
    }

    return rawContactId ?: INVALID_ID
}

private fun photoUriInputStream(contactId: Long, context: Context): InputStream? {
    if (!ContactsPermissions(context).canQuery() || contactId == INVALID_ID) {
        return null
    }

    val cursor = context.contentResolver.query(
        Table.CONTACTS.uri,
        arrayOf(Fields.Contacts.PhotoUri.columnName),
        "${Fields.Contacts.Id equalTo contactId}",
        null,
        null
    )

    var photoUri: Uri? = null
    if (cursor != null && cursor.moveToNext()) {
        photoUri = ContactsCursor(cursor).photoUri

        cursor.close()
    }

    return uriInputStream(photoUri, context)
}

private fun photoThumbnailUriInputStream(contactId: Long, context: Context): InputStream? {
    if (!ContactsPermissions(context).canQuery() || contactId == INVALID_ID) {
        return null
    }

    val cursor = context.contentResolver.query(
        Table.CONTACTS.uri,
        arrayOf(Fields.Contacts.PhotoThumbnailUri.columnName),
        "${Fields.Contacts.Id equalTo contactId}",
        null,
        null
    )

    var photoThumbnailUri: Uri? = null
    if (cursor != null && cursor.moveToNext()) {
        photoThumbnailUri = ContactsCursor(cursor).photoThumbnailUri

        cursor.close()
    }

    return uriInputStream(photoThumbnailUri, context)
}

private fun uriInputStream(uri: Uri?, context: Context): InputStream? {
    if (uri == null) {
        return null
    }

    var inputStream: InputStream? = null
    try {
        val fd = context.contentResolver.openAssetFileDescriptor(uri, "r")
        inputStream = fd?.createInputStream()
    } finally {
        return inputStream
    }
}