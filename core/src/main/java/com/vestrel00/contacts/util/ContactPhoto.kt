package com.vestrel00.contacts.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.ContactEntity
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.TempRawContact
import com.vestrel00.contacts.entities.cursor.contactsCursor
import com.vestrel00.contacts.entities.cursor.dataCursor
import com.vestrel00.contacts.entities.mapper.tempRawContactMapper
import com.vestrel00.contacts.entities.operation.newDelete
import com.vestrel00.contacts.entities.operation.withSelection
import com.vestrel00.contacts.entities.table.Table
import java.io.InputStream

// region GET PHOTO

/**
 * Returns the full-sized photo as an [InputStream]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * It is up to the caller to close the [InputStream].
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
fun ContactEntity.photoInputStream(context: Context): InputStream? {
    val contactId = id

    if (!ContactsPermissions(context).canQuery() || contactId == null) {
        return null
    }

    return context.contentResolver.query(
        Table.Contacts,
        Include(ContactsFields.PhotoUri),
        ContactsFields.Id equalTo contactId
    ) {
        val photoUri = it.getNextOrNull { it.contactsCursor().photoUri }
        uriInputStream(context, photoUri)
    }
}

/**
 * Returns the full-sized photo as a [ByteArray]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
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
fun ContactEntity.photoBytes(context: Context): ByteArray? = photoInputStream(context)?.apply {
    it.readBytes()
}

/**
 * Returns the full-sized photo as a [Bitmap]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters..
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
fun ContactEntity.photoBitmap(context: Context): Bitmap? = photoInputStream(context)?.apply {
    BitmapFactory.decodeStream(it)
}

/**
 * Returns the full-sized photo as a [BitmapDrawable]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
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
fun ContactEntity.photoBitmapDrawable(context: Context): BitmapDrawable? =
    photoInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

private fun uriInputStream(context: Context, uri: Uri?): InputStream? {
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

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Returns the photo thumbnail as an [InputStream]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * It is up to the caller to close the [InputStream].
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
fun ContactEntity.photoThumbnailInputStream(context: Context): InputStream? {
    val contactId = id

    if (!ContactsPermissions(context).canQuery() || contactId == null) {
        return null
    }

    return context.contentResolver.query(
        Table.Contacts,
        Include(ContactsFields.PhotoThumbnailUri),
        ContactsFields.Id equalTo contactId
    ) {
        val photoThumbnailUri = it.getNextOrNull { it.contactsCursor().photoThumbnailUri }
        uriInputStream(context, photoThumbnailUri)
    }
}

/**
 * Returns the photo thumbnail as a [ByteArray]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
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
fun ContactEntity.photoThumbnailBytes(context: Context): ByteArray? =
    photoThumbnailInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * Returns the full-sized photo as a [Bitmap]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
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
fun ContactEntity.photoThumbnailBitmap(context: Context): Bitmap? =
    photoThumbnailInputStream(context)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * Returns the full-sized photo as a [BitmapDrawable]. Returns null if a photo has not yet been set.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
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
fun ContactEntity.photoThumbnailBitmapDrawable(context: Context): BitmapDrawable? =
    photoThumbnailInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

// endregion

// region SET PHOTO

/**
 * Sets the photo of this [ContactEntity] (and the [com.vestrel00.contacts.entities.RawContact] that
 * the Contacts Provider has chosen to hold the primary photo). If a photo already exists, it will
 * be overwritten. The Contacts Provider automatically creates a downsized version of this as the
 * thumbnail.
 *
 * If a photo has not yet been set and the Contacts Provider has not yet chosen the RawContact that
 * will be used as the primary photo holder, then this will use the first RawContact in the list of
 * [ContactEntity.rawContacts].
 *
 * Returns true if the operation succeeds.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 *
 * ## DEV NOTES
 *
 * The function body is mostly taken from the sample code from the
 * [ContactsContract.RawContacts.DisplayPhoto] class documentation.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.setPhoto(context: Context, photoBytes: ByteArray): Boolean {
    val contactId = id

    if (!ContactsPermissions(context).canUpdateDelete() || contactId == null) {
        return false
    }

    val photoFileId = photoFileId(context, contactId)

    val rawContact = if (photoFileId != null) {
        // A photo exists for the Contact. Get the RawContact in the Data table that holds the photo
        // row with the same photo file id. Keep in mind that there may be multiple RawContacts
        // associated with a single Contact.
        rawContactWithPhotoFileId(context, photoFileId)
    } else {
        // No photo exists for the Contact or any of its associated RawContacts. Use the
        // first RawContact as the default or fail if also not available.
        rawContacts.firstOrNull()
    }

    return rawContact?.doSetPhoto(context, photoBytes) == true
}

/**
 * See [ContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.setPhoto(context: Context, photoInputStream: InputStream): Boolean =
    setPhoto(context, photoInputStream.readBytes())

/**
 * See [ContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.setPhoto(context: Context, photoBitmap: Bitmap): Boolean =
    setPhoto(context, photoBitmap.bytes())

/**
 * See [ContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.setPhoto(context: Context, photoDrawable: BitmapDrawable): Boolean =
    setPhoto(context, photoDrawable.bitmap.bytes())

private fun photoFileId(context: Context, contactId: Long): Long? = context.contentResolver.query(
    Table.Contacts,
    Include(ContactsFields.PhotoFileId),
    ContactsFields.Id equalTo contactId
) {
    it.getNextOrNull { it.contactsCursor().photoFileId }
}

private fun rawContactWithPhotoFileId(context: Context, photoFileId: Long): TempRawContact? =
    context.contentResolver.query(
        Table.Data,
        Include(Fields.RawContact.Id),
        Fields.Photo.PhotoFileId equalTo photoFileId
    ) {
        it.getNextOrNull { it.dataCursor().tempRawContactMapper().value }
    }

// endregion

// region REMOVE PHOTO

/**
 * Removes the photos of all the RawContacts associated with this [ContactEntity], if any exists.
 *
 * Returns true if the operation succeeds.
 *
 * The native Contacts app actually does not provide the option to remove the photo of a Contact
 * with at least 2 associated RawContacts.
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
fun ContactEntity.removePhoto(context: Context): Boolean {
    val contactId = id

    if (!ContactsPermissions(context).canUpdateDelete() || contactId == null) {
        return false
    }

    val isSuccessful = context.contentResolver.applyBatch(
        newDelete(Table.Data)
            .withSelection(
                (Fields.Contact.Id equalTo contactId)
                        and (Fields.MimeType equalTo MimeType.PHOTO)
            )
            .build()
    ) != null

    if (isSuccessful) {
        // Assume that all photo Data rows have been deleted and remove the photo instances from all
        // RawContacts so that it will all be marked as blank if it has no other Data rows.
        for (rawContact in rawContacts) {
            rawContact.photo = null
        }
    }

    return isSuccessful
}

// endregion