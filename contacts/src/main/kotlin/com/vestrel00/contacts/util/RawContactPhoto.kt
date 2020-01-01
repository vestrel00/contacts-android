package com.vestrel00.contacts.util

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.INVALID_ID
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.cursor.PhotoCursor
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

// SET PHOTO

/**
 * Sets the photo of this [RawContact]. If a photo already exists, it will be overwritten.
 * The Contacts Provider automatically creates a downsized version of this as the thumbnail.
 *
 * If this [RawContact] is the only one that make up a [com.vestrel00.contacts.entities.Contact],
 * then the photo set here will also be used by the Contacts Provider as the contact photo.
 * Otherwise, it may or may not be the photo picked by the Contacts Provider as the contact photo.
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
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.setPhoto(photoBytes: ByteArray, context: Context): Boolean =
    setRawContactPhoto(id, photoBytes, context)

/**
 * See [RawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.setPhoto(photoInputStream: InputStream, context: Context): Boolean =
    setPhoto(photoInputStream.readBytes(), context)

/**
 * See [RawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.setPhoto(photoBitmap: Bitmap, context: Context): Boolean =
    setPhoto(photoBitmap.bytes(), context)

/**
 * See [RawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.setPhoto(photoDrawable: BitmapDrawable, context: Context): Boolean =
    setPhoto(photoDrawable.bitmap.bytes(), context)

/**
 * See [RawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.setPhoto(photoBytes: ByteArray, context: Context): Boolean =
    setRawContactPhoto(id, photoBytes, context)

/**
 * See [MutableRawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.setPhoto(photoInputStream: InputStream, context: Context): Boolean =
    setPhoto(photoInputStream.readBytes(), context)

/**
 * See [MutableRawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.setPhoto(photoBitmap: Bitmap, context: Context): Boolean =
    setPhoto(photoBitmap.bytes(), context)

/**
 * See [MutableRawContact.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.setPhoto(photoDrawable: BitmapDrawable, context: Context): Boolean =
    setPhoto(photoDrawable.bitmap.bytes(), context)

// REMOVE PHOTO

/**
 * Removes the photo of this [RawContact], if one exists.
 *
 * If this [RawContact] is the only one that make up a [com.vestrel00.contacts.entities.Contact],
 * then the contact photo will also be removed. Otherwise, it may or may not affect the contact
 * photo.
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
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContact.removePhoto(context: Context): Boolean = removeRawContactPhoto(id, context)

/**
 * See [RawContact.removePhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.removePhoto(context: Context): Boolean = removeRawContactPhoto(id, context)

// GET PHOTO

/**
 * Returns the full-sized photo as an [InputStream]. Returns null if a photo has not yet been set.
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
fun RawContact.photoInputStream(context: Context): InputStream? =
    photoInputStream(id, context)

/**
 * Returns the full-sized photo as a [ByteArray]. Returns null if a photo has not yet been set.
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
fun RawContact.photoBytes(context: Context): ByteArray? = photoInputStream(context)?.apply {
    it.readBytes()
}

/**
 * Returns the full-sized photo as a [Bitmap]. Returns null if a photo has not yet been set.
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
fun RawContact.photoBitmap(context: Context): Bitmap? = photoInputStream(context)?.apply {
    BitmapFactory.decodeStream(it)
}

/**
 * Returns the full-sized photo as a [BitmapDrawable]. Returns null if a photo has not yet been set.
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
fun RawContact.photoBitmapDrawable(context: Context): BitmapDrawable? =
    photoInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

/**
 * See [RawContact.photoInputStream].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoInputStream(context: Context): InputStream? =
    photoInputStream(id, context)

/**
 * See [RawContact.photoBytes].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoBytes(context: Context): ByteArray? =
    photoInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * See [RawContact.photoBitmap].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoBitmap(context: Context): Bitmap? = photoInputStream(context)?.apply {
    BitmapFactory.decodeStream(it)
}

/**
 * See [RawContact.photoBitmapDrawable].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoBitmapDrawable(context: Context): BitmapDrawable? =
    photoInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

// GET PHOTO THUMBNAIL

/**
 * Returns the photo thumbnail as an [InputStream]. Returns null if a photo has not yet been set.
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
fun RawContact.photoThumbnailInputStream(context: Context): InputStream? =
    photoThumbnailInputStream(id, context)

/**
 * Returns the photo thumbnail as a [ByteArray]. Returns null if a photo has not yet been set.
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
fun RawContact.photoThumbnailBytes(context: Context): ByteArray? =
    photoThumbnailInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * Returns the photo thumbnail as a [Bitmap]. Returns null if a photo has not yet been set.
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
fun RawContact.photoThumbnailBitmap(context: Context): Bitmap? =
    photoThumbnailInputStream(context)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * Returns the photo thumbnail as a [BitmapDrawable]. Returns null if a photo has not yet been set.
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
fun RawContact.photoThumbnailBitmapDrawable(context: Context): BitmapDrawable? =
    photoThumbnailInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

/**
 * See [RawContact.photoThumbnailInputStream].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoThumbnailInputStream(context: Context): InputStream? =
    photoThumbnailInputStream(id, context)

/**
 * See [RawContact.photoThumbnailBytes].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoThumbnailBytes(context: Context): ByteArray? =
    photoThumbnailInputStream(context)?.apply {
        it.readBytes()
    }

/**
 * See [RawContact.photoThumbnailBitmap].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoThumbnailBitmap(context: Context): Bitmap? =
    photoThumbnailInputStream(context)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * See [RawContact.photoThumbnailBitmapDrawable].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.photoThumbnailBitmapDrawable(context: Context): BitmapDrawable? =
    photoThumbnailInputStream(context)?.apply {
        BitmapDrawable(context.resources, it)
    }

// HELPERS

/**
 * The function body is mostly taken from the sample code from the [RawContacts.DisplayPhoto] class
 * documentation.
 */
internal fun setRawContactPhoto(
    rawContactId: Long,
    photoBytes: ByteArray,
    context: Context
): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || rawContactId == INVALID_ID) {
        return false
    }

    var isSuccessful = false
    try {
        val photoUri = Uri.withAppendedPath(
            ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
            RawContacts.DisplayPhoto.CONTENT_DIRECTORY
        )

        val fd = context.contentResolver
            .openAssetFileDescriptor(photoUri, "rw")!!
        val os = fd.createOutputStream()

        os.write(photoBytes)

        os.close()
        fd.close()

        isSuccessful = true
    } finally {
        return isSuccessful
    }
}

// Removing the photo data row does the trick!
private fun removeRawContactPhoto(rawContactId: Long, context: Context): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || rawContactId == INVALID_ID) {
        return false
    }

    val deleteRawContactPhotoOperation = ContentProviderOperation.newDelete(Table.DATA.uri)
        .withSelection(
            "${(Fields.RawContactId equalTo rawContactId)
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
            arrayListOf(deleteRawContactPhotoOperation)
        )
    } catch (exception: Exception) {
        return false
    }

    return true
}

private fun photoInputStream(rawContactId: Long, context: Context): InputStream? {
    if (!ContactsPermissions(context).canQuery() || rawContactId == INVALID_ID) {
        return null
    }

    val cursor = context.contentResolver.query(
        Table.DATA.uri,
        arrayOf(Fields.Photo.PhotoFileId.columnName),
        "${(Fields.RawContactId equalTo rawContactId)
                and (Fields.MimeType equalTo MimeType.PHOTO)}",
        null,
        null
    )

    val photoFileId = if (cursor != null && cursor.moveToNext()) {
        PhotoCursor(cursor).photoFileId
    } else {
        null
    }

    cursor?.close()

    if (photoFileId != null) {
        val photoUri =
            ContentUris.withAppendedId(ContactsContract.DisplayPhoto.CONTENT_URI, photoFileId)

        var inputStream: InputStream? = null
        try {
            val fd = context.contentResolver.openAssetFileDescriptor(photoUri, "r")
            inputStream = fd?.createInputStream()
        } finally {
            return inputStream
        }
    }

    return null
}

private fun photoThumbnailInputStream(rawContactId: Long, context: Context): InputStream? {
    if (!ContactsPermissions(context).canQuery() || rawContactId == INVALID_ID) {
        return null
    }

    val cursor = context.contentResolver.query(
        Table.DATA.uri,
        arrayOf(Fields.Photo.PhotoThumbnail.columnName),
        "${(Fields.RawContactId equalTo rawContactId)
                and (Fields.MimeType equalTo MimeType.PHOTO)}",
        null,
        null
    )

    var photoThumbnail: ByteArray? = null
    if (cursor != null && cursor.moveToNext()) {
        photoThumbnail = PhotoCursor(cursor).photoThumbnail

        cursor.close()
    }

    return if (photoThumbnail != null) ByteArrayInputStream(photoThumbnail) else null
}

internal inline fun <T> InputStream.apply(block: (InputStream) -> T): T {
    val t = block(this)
    close()
    return t
}

internal fun Bitmap.bytes(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}